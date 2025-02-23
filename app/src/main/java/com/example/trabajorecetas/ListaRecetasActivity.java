package com.example.trabajorecetas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajorecetas.BaseDeDatos.Receta;
import com.example.trabajorecetas.BaseDeDatos.RecetaDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ListaRecetasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecetaAdapter adapter;
    private RecetaDatabase db;
    private List<Receta> listaRecetas;
    private FloatingActionButton fabAdd;

    private static final int EDITAR_RECETA_REQUEST = 1;
    private static final int AÑADIR_RECETA_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_recetas);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(view -> {
            // Abrir AddRecetaActivity para añadir una receta
            Intent intent = new Intent(ListaRecetasActivity.this, AddRecetaActivity.class);
            startActivityForResult(intent, AÑADIR_RECETA_REQUEST); // Cambié a startActivityForResult
        });

        db = RecetaDatabase.getInstance(this);
        cargarRecetas();
    }

    private void cargarRecetas() {
        new Thread(() -> {
            listaRecetas = db.recetaDao().obtenerTodas();
            runOnUiThread(() -> {
                adapter = new RecetaAdapter(listaRecetas, new RecetaAdapter.OnItemClickListener() {
                    @Override
                    public void onEditar(Receta receta) {
                        Intent intent = new Intent(ListaRecetasActivity.this, EditarRecetaActivity.class);
                        intent.putExtra("receta_id", receta.getId());
                        startActivityForResult(intent, EDITAR_RECETA_REQUEST); // Abrir EditarRecetaActivity
                    }

                    @Override
                    public void onBorrar(Receta receta) {
                        new Thread(() -> {
                            db.recetaDao().eliminar(receta);
                            runOnUiThread(() -> {
                                Toast.makeText(ListaRecetasActivity.this, "Receta eliminada", Toast.LENGTH_SHORT).show();
                                cargarRecetas(); // Recargar recetas después de eliminar
                            });
                        }).start();
                    }

                    @Override
                    public void onFavorito(Receta receta) {
                        new Thread(() -> {
                            receta.setFavorita(!receta.isFavorita());
                            db.recetaDao().actualizar(receta);
                            runOnUiThread(() -> {
                                adapter.notifyDataSetChanged(); // Actualizar la vista después de modificar
                                Toast.makeText(ListaRecetasActivity.this, "Favorito actualizado", Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    }
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == AÑADIR_RECETA_REQUEST || requestCode == EDITAR_RECETA_REQUEST) && resultCode == RESULT_OK) {
            // Si la receta fue añadida, recargamos la lista
            if (data != null && data.getBooleanExtra("receta_guardada", false)) {
                cargarRecetas();
                adapter.notifyDataSetChanged();
            }
        }
    }
}
