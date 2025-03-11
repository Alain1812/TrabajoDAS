package com.example.trabajorecetas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajorecetas.BaseDeDatos.Receta;
import com.example.trabajorecetas.BaseDeDatos.RecetaDatabase;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecetaAdapter adapter;
    private RecetaDatabase db;
    private List<Receta> listaRecetas;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private static final int EDITAR_RECETA_REQUEST = 1;
    private static final int AÑADIR_RECETA_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar base de datos y cargar recetas
        db = RecetaDatabase.getInstance(this);
        cargarRecetas(false);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
        }

        // Configurar DrawerLayout y Toggle para el Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Configurar NavigationView
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_espanol) {
                cambiarIdioma("es");
            } else if (item.getItemId() == R.id.nav_ingles) {
                cambiarIdioma("en");
            } else if (item.getItemId() == R.id.nav_aleman) {
                cambiarIdioma("de");
            } else if (item.getItemId() == R.id.nav_frances) {
                cambiarIdioma("fr");
            } else if (item.getItemId() == R.id.nav_italiano) {
                cambiarIdioma("it");
            } else if (item.getItemId() == R.id.nav_euskera) {
                cambiarIdioma("eu");
            } else {
                return false;
            }
            drawerLayout.closeDrawers(); // Cierra el menú después de seleccionar
            return true;
        });

        // Configurar canal de notificación si la versión de Android lo requiere
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "descarga_receta_channel",
                    "Descarga de Receta",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Solicitar permisos de notificación
        NotificacionHelper.solicitarPermisos(this);
    }


    private void cargarRecetas(boolean soloFavoritas) {
        new Thread(() -> {
            if (soloFavoritas) {
                listaRecetas = db.recetaDao().obtenerFavoritas();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, getString(R.string.listado_recetas_favoritas), Toast.LENGTH_SHORT).show();
                });
            } else {
                listaRecetas = db.recetaDao().obtenerTodas();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, getString(R.string.listado_recetas), Toast.LENGTH_SHORT).show();
                });            }

            runOnUiThread(() -> {
                adapter = new RecetaAdapter(listaRecetas, new RecetaAdapter.OnItemClickListener() {
                    @Override
                    public void onEditar(Receta receta) {
                        Intent intent = new Intent(MainActivity.this, EditarRecetaActivity.class);
                        intent.putExtra("receta_id", receta.getId());
                        startActivityForResult(intent, EDITAR_RECETA_REQUEST);
                    }

                    @Override
                    public void onBorrar(Receta receta) {
                        // Crear y mostrar el diálogo para confirmar la eliminación de la receta
                        BorrarRecetaDialog borrarDialog = new BorrarRecetaDialog();
                        borrarDialog.setOnBorrarRecetaListener(new BorrarRecetaDialog.OnBorrarRecetaListener() {
                            @Override
                            public void onBorrarRecetaConfirmado() {
                                // Código para eliminar la receta de la base de datos
                                    new Thread(() -> {
                                    db.recetaDao().eliminar(receta);
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, getString(R.string.receta_eliminada), Toast.LENGTH_SHORT).show();
                                        cargarRecetas(false); // Recargar después de eliminar
                                    });
                                }).start();
                            }
                        });
                        borrarDialog.show(getSupportFragmentManager(), "BorrarRecetaDialog");
                    }


                    @Override
                    public void onFavorito(Receta receta) {
                        new Thread(() -> {
                            receta.setFavorita(!receta.isFavorita());
                            db.recetaDao().actualizar(receta);
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, getString(R.string.receta_favoritos), Toast.LENGTH_SHORT).show();
                                cargarRecetas(soloFavoritas); // Recargar después de modificar favorito
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
            if (data != null && data.getBooleanExtra("receta_guardada", false)) {
                cargarRecetas(false);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView(); // Asegúrate de usar la clase correcta aquí

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        return true;
    }


    private void anadirReceta() {
        Intent intent = new Intent(MainActivity.this, AddRecetaActivity.class);
        startActivityForResult(intent, AÑADIR_RECETA_REQUEST);
    }

    private void alternarModo() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, getString(R.string.Modo_claro), Toast.LENGTH_SHORT).show();

        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, getString(R.string.Modo_oscuro), Toast.LENGTH_SHORT).show();

        }
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void cambiarIdioma(String idioma) {
        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        Toast.makeText(this, getString(R.string.cambio_idioma), Toast.LENGTH_SHORT).show();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_todas) {
            cargarRecetas(false);
            return true;
        } else if (id == R.id.action_favoritas) {
            cargarRecetas(true);
            return true;
        } else if (id == R.id.action_agregar) {
            anadirReceta();
            return true;
        } else if (id == R.id.action_tema) {
            alternarModo();
            return true;
        } else if (id == R.id.action_salir) {
            SalirDialog salirDialogo = new SalirDialog();
            salirDialogo.show(getSupportFragmentManager(), "SalirDialogo");
            return true;
        } else if (id == android.R.id.home) {
            // Abrir o cerrar el Navigation Drawer
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(navigationView);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
