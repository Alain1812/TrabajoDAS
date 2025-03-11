package com.example.trabajorecetas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
            finish();  // Cierra la actividad actual
        });

        int recetaId = getIntent().getIntExtra("receta_id", -1);

        if (recetaId != -1) {
            new Thread(() -> {
                Receta receta = RecetaDatabase.getInstance(getApplicationContext()).recetaDao().obtenerPorId(recetaId);
                runOnUiThread(() -> {
                    if (receta != null) {
                        ((TextView) findViewById(R.id.tvNombre)).setText(receta.getNombre());
                        ((TextView) findViewById(R.id.tvIngredientes)).setText(receta.getIngredientes());
                        ((TextView) findViewById(R.id.tvPasos)).setText(receta.getPasos());

                        if (receta.getImagen() != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(receta.getImagen(), 0, receta.getImagen().length);
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
