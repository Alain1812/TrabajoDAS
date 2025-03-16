package com.example.trabajorecetas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajorecetas.BaseDeDatos.Receta;
import com.example.trabajorecetas.BaseDeDatos.RecetaDatabase;

import java.io.IOException;

public class AddRecetaActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private EditText etNombre, etIngredientes, etPasos;
    private ImageView ivImagen;
    private Bitmap imagenBitmap = null;
    private RecetaDatabase db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_receta);

        // Inicializar vistas
        etNombre = findViewById(R.id.et_nombre);
        etIngredientes = findViewById(R.id.et_ingredientes);
        etPasos = findViewById(R.id.et_pasos);
        ivImagen = findViewById(R.id.iv_imagen);
        Button btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen);
        Button btnGuardar = findViewById(R.id.btn_guardar);
        Button btnTomarFoto = findViewById(R.id.btn_tomar_foto);
        db = RecetaDatabase.getInstance(this);

        // Restaurar imagen si hay un estado guardado
        if (savedInstanceState != null) {
            byte[] imagenBytes = savedInstanceState.getByteArray("imagen");
            if (imagenBytes != null) {
                imagenBitmap = ImageConverter.byteArrayToBitmap(imagenBytes);
                ivImagen.setImageBitmap(imagenBitmap);
            }
        }

        // Configurar listeners de botones

        // Abrir la camara para tomar una foto
        btnTomarFoto.setOnClickListener(v -> abrirCamara());

        // Abrir la galeria para seleccionar una imagen
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());

        // Guardar la receta
        btnGuardar.setOnClickListener(v -> guardarReceta());

        // Volver a MainActivity
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            // Informar que la receta no se ha guardado
            Intent intent = new Intent(AddRecetaActivity.this, MainActivity.class);
            startActivity(intent);
            // Cierra la actividad actual
            finish();
        });

    }

    // Abrir la galeria para seleccionar una imagen
    private void abrirGaleria() {
        // Crear un intent para seleccionar una imagen de la galeria
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Abrir la camara para tomar una foto
    private void abrirCamara() {
        // Crear un intent para tomar una foto con la camara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Verificar si hay una app de cámara disponible
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }else{
            Toast.makeText(this, getString(R.string.error_camara), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Imagen en galeria
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                imagenBitmap = ImageConverter.decodeSampledBitmapFromUri(this, imageUri, 800, 800);
                ivImagen.setImageBitmap(imagenBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.error_galeria), Toast.LENGTH_SHORT).show();
            }
        // Imagen tomada con la camara
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            imagenBitmap = (Bitmap) extras.get("data");
            ivImagen.setImageBitmap(imagenBitmap);
        }
    }

    // Evita perder datos de la receta mientras se esta creando
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (imagenBitmap != null) {
            byte[] imagenBytes = ImageConverter.bitmapToByteArray(imagenBitmap);
            outState.putByteArray("imagen", imagenBytes);
        }
    }


    // Valida y guarda la receta en la base de datos
    private void guardarReceta() {
        String nombre = etNombre.getText().toString().trim();
        String ingredientes = etIngredientes.getText().toString().trim();
        String pasos = etPasos.getText().toString().trim();

        // Validar campos
        if (nombre.isEmpty() || ingredientes.isEmpty() || pasos.isEmpty()) {
            Toast.makeText(this, getString(R.string.rellenar_campos), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imagenByteArray = null;
        // Si se ha añadido una imagen se usa
        if (imagenBitmap != null) {
            imagenByteArray = ImageConverter.bitmapToByteArray(imagenBitmap);

            // Si no se ha añadido una imagen se usa la imagen por defecto
        } else {
            Bitmap defaultBitmap = ImageConverter.loadDefaultBitmap(
                    getResources(),
                    R.drawable.ic_placeholder
            );
            imagenByteArray = ImageConverter.bitmapToByteArray(defaultBitmap);
        }

        // Guardar la receta en la base de datos
        Receta nuevaReceta = new Receta(nombre, ingredientes, pasos, imagenByteArray, false);
        new Thread(() -> {
            db.recetaDao().insertar(nuevaReceta);
            runOnUiThread(() -> {
                Toast.makeText(AddRecetaActivity.this, "Receta guardada", Toast.LENGTH_SHORT).show();
                // Informar que la receta se ha guardado correctamente
                Intent resultIntent = new Intent();
                resultIntent.putExtra("receta_guardada", true);
                setResult(RESULT_OK, resultIntent);
                // Cierra la actividad actual
                finish();
            });
        }).start();
    }
}