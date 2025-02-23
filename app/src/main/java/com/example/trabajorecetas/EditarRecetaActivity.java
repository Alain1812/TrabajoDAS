package com.example.trabajorecetas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajorecetas.BaseDeDatos.RecetaDatabase;
import com.example.trabajorecetas.BaseDeDatos.Receta;
import com.example.trabajorecetas.BaseDeDatos.ImageConverter;

import java.io.ByteArrayOutputStream;

public class EditarRecetaActivity extends AppCompatActivity {

    private EditText etNombre, etIngredientes, etPasos;
    private ImageView ivImagen;
    private Button btnGuardar, btnElegirImagen;
    private RecetaDatabase db;
    private Receta receta;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

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

        db = RecetaDatabase.getInstance(this);

        // Si se pasa un ID, es para editar una receta existente
        int recetaId = getIntent().getIntExtra("receta_id", -1);
        if (recetaId != -1) {
            new Thread(() -> {
                receta = db.recetaDao().obtenerPorId(recetaId);
                runOnUiThread(() -> {
                    etNombre.setText(receta.getNombre());
                    etIngredientes.setText(receta.getIngredientes());
                    etPasos.setText(receta.getPasos());

                    // Convertir byte[] a Bitmap
                    Bitmap imagen = ImageConverter.byteArrayToBitmap(receta.getImagen());
                    ivImagen.setImageBitmap(imagen);
                });
            }).start();
        } else {
            receta = new Receta("", "", "", null, false);
        }

        btnElegirImagen.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        btnGuardar.setOnClickListener(v -> guardarReceta());
    }

    private void guardarReceta() {
        receta.setNombre(etNombre.getText().toString());
        receta.setIngredientes(etIngredientes.getText().toString());
        receta.setPasos(etPasos.getText().toString());

        // Convertir imagen a byte[]
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
                Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show();

                // Enviar un Intent para indicar que la receta se ha guardado y actualizar la lista
                Intent intent = new Intent();
                intent.putExtra("receta_guardada", true);
                setResult(RESULT_OK, intent);
                finish(); // Volver a la actividad anterior
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
        }
    }
}
