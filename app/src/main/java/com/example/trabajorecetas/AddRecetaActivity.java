package com.example.trabajorecetas;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trabajorecetas.BaseDeDatos.ImageConverter;
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

        etNombre = findViewById(R.id.et_nombre);
        etIngredientes = findViewById(R.id.et_ingredientes);
        etPasos = findViewById(R.id.et_pasos);
        ivImagen = findViewById(R.id.iv_imagen);
        Button btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen);
        Button btnGuardar = findViewById(R.id.btn_guardar);
        Button btnTomarFoto = findViewById(R.id.btn_tomar_foto);
        db = RecetaDatabase.getInstance(this);

        btnTomarFoto.setOnClickListener(v -> abrirCamara());

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());

        btnGuardar.setOnClickListener(v -> guardarReceta());

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            // Volver a MainActivity
            Intent intent = new Intent(AddRecetaActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Cierra la actividad actual
        });

    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }else{
            Toast.makeText(this, getString(R.string.error_camara), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                imagenBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivImagen.setImageBitmap(imagenBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.error_galeria), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivImagen.setImageBitmap(imageBitmap);
            this.imagenBitmap = imageBitmap;
        }
    }


    private void guardarReceta() {
        String nombre = etNombre.getText().toString().trim();
        String ingredientes = etIngredientes.getText().toString().trim();
        String pasos = etPasos.getText().toString().trim();

        if (nombre.isEmpty() || ingredientes.isEmpty() || pasos.isEmpty()) {
            Toast.makeText(this, getString(R.string.rellenar_campos), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imagenByteArray = null;
        if (imagenBitmap != null) {
            imagenByteArray = ImageConverter.bitmapToByteArray(imagenBitmap);
        } else {
            // Si no se seleccionó una imagen, se puede usar una imagen por defecto
            BitmapDrawable drawable = (BitmapDrawable) ivImagen.getDrawable();
            imagenByteArray = ImageConverter.bitmapToByteArray(drawable.getBitmap());
        }

        Receta nuevaReceta = new Receta(nombre, ingredientes, pasos, imagenByteArray, false);
        new Thread(() -> {
            db.recetaDao().insertar(nuevaReceta);
            runOnUiThread(() -> {
                Toast.makeText(AddRecetaActivity.this, "Receta guardada", Toast.LENGTH_SHORT).show();
                // Informar que la receta se ha guardado correctamente
                Intent resultIntent = new Intent();
                resultIntent.putExtra("receta_guardada", true); // Enviamos el estado de la receta guardada
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }).start();
    }
}
