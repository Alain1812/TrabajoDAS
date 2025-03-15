package com.example.trabajorecetas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajorecetas.BaseDeDatos.Receta;
import com.example.trabajorecetas.BaseDeDatos.RecetaDatabase;

public class DetalleRecetaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_receta);

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            // Volver a MainActivity
            Intent intent = new Intent(DetalleRecetaActivity.this, MainActivity.class);
            startActivity(intent);
            // Cierra la actividad actual
            finish();
        });

        // Obtener ID de la receta del Intent
        int recetaId = getIntent().getIntExtra("receta_id", -1);

        // Validar ID de receta y cargar datos
        if (recetaId != -1) {
            new Thread(() -> {
                // Acceder a la base de datos Room
                Receta receta = RecetaDatabase.getInstance(getApplicationContext()).recetaDao().obtenerPorId(recetaId);
                runOnUiThread(() -> {
                    if (receta != null) {
                        // Mostrar datos de la receta
                        ((TextView) findViewById(R.id.tvNombre)).setText(receta.getNombre());
                        ((TextView) findViewById(R.id.tvIngredientes)).setText(receta.getIngredientes());
                        ((TextView) findViewById(R.id.tvPasos)).setText(receta.getPasos());

                        if (receta.getImagen() != null) {
                            Bitmap bitmap = ImageConverter.byteArrayToBitmap(receta.getImagen());
                            ((ImageView) findViewById(R.id.ivImagen)).setImageBitmap(bitmap);
                        }
                    }
                });
            }).start();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
