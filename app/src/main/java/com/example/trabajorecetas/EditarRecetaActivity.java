package com.example.trabajorecetas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trabajorecetas.BaseDeDatos.RecetaDatabase;
import com.example.trabajorecetas.BaseDeDatos.Receta;
import com.example.trabajorecetas.BaseDeDatos.ImageConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditarRecetaActivity extends AppCompatActivity {

    private EditText etNombre, etIngredientes, etPasos;
    private ImageView ivImagen;
    private Button btnGuardar, btnElegirImagen, btnTomarImagen;
    private RecetaDatabase db;
    private Receta receta;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_receta);

        etNombre = findViewById(R.id.et_nombre);
        etIngredientes = findViewById(R.id.et_ingredientes);
        etPasos = findViewById(R.id.et_pasos);
        ivImagen = findViewById(R.id.iv_imagen);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnElegirImagen = findViewById(R.id.btn_elegir_imagen);
        btnTomarImagen = findViewById(R.id.btn_tomar_imagen);

        btnElegirImagen.setOnClickListener(v -> abrirGaleria());
        btnTomarImagen.setOnClickListener(v -> abrirCamara());

        db = RecetaDatabase.getInstance(this);

        // Obtener receta si se pasa un ID
        int recetaId = getIntent().getIntExtra("receta_id", -1);
        if (recetaId != -1) {
            new Thread(() -> {
                receta = db.recetaDao().obtenerPorId(recetaId);
                runOnUiThread(() -> {
                    etNombre.setText(receta.getNombre());
                    etIngredientes.setText(receta.getIngredientes());
                    etPasos.setText(receta.getPasos());

                    // Convertir byte[] a Bitmap y mostrar la imagen
                    Bitmap imagen = ImageConverter.byteArrayToBitmap(receta.getImagen());
                    ivImagen.setImageBitmap(imagen);
                });
            }).start();
        } else {
            receta = new Receta("", "", "", null, false);
        }

        btnGuardar.setOnClickListener(v -> guardarReceta());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarReceta() {
        receta.setNombre(etNombre.getText().toString());
        receta.setIngredientes(etIngredientes.getText().toString());
        receta.setPasos(etPasos.getText().toString());

        // Convertir la imagen a byte[]
        Bitmap bitmap = ((BitmapDrawable) ivImagen.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        receta.setImagen(stream.toByteArray());

        new Thread(() -> {
            if (receta.getId() == 0) {
                db.recetaDao().insertar(receta);
            } else {
                db.recetaDao().actualizar(receta);
            }
            runOnUiThread(() -> {
                Toast.makeText(EditarRecetaActivity.this, "Receta guardada", Toast.LENGTH_SHORT).show();
                // Informar que la receta se ha guardado correctamente
                Intent resultIntent = new Intent();
                resultIntent.putExtra("receta_guardada", true); // Enviamos el estado de la receta guardada
                setResult(RESULT_OK, resultIntent);
                finish();// Volver a la actividad anterior
            });
        }).start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivImagen.setImageBitmap(imageBitmap);
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivImagen.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen desde la galería", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
