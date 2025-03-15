package com.example.trabajorecetas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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
            // Manejar eventos de clic en los elementos del menú
            // Idioma español
            if (item.getItemId() == R.id.nav_espanol) {
                cambiarIdioma("es");
            // Idioma inglés
            } else if (item.getItemId() == R.id.nav_ingles) {
                cambiarIdioma("en");
            // Idioma alemán
            } else if (item.getItemId() == R.id.nav_aleman) {
                cambiarIdioma("de");
            // Idioma francés
            } else if (item.getItemId() == R.id.nav_frances) {
                cambiarIdioma("fr");
            // Idioma italiano
            } else if (item.getItemId() == R.id.nav_italiano) {
                cambiarIdioma("it");
            // Idioma euskera
            } else if (item.getItemId() == R.id.nav_euskera) {
                cambiarIdioma("eu");
            } else {
                return false;
            }
            // Cierra el menú después de seleccionar
            drawerLayout.closeDrawers();
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


    // Metodo para cargar las recetas
    private void cargarRecetas(boolean soloFavoritas) {
        new Thread(() -> {
            if (soloFavoritas) {
                listaRecetas = db.recetaDao().obtenerFavoritas();
                runOnUiThread(() -> {
                    TextView tituloLista = findViewById(R.id.textTituloLista);
                    if (tituloLista != null) {
                        tituloLista.setText(R.string.listado_recetas_favoritas);
                    }
                });
            } else {
                listaRecetas = db.recetaDao().obtenerTodas();
                runOnUiThread(() -> {
                    TextView tituloLista = findViewById(R.id.textTituloLista);
                    if (tituloLista != null) {
                        tituloLista.setText(R.string.listado_recetas);
                    }
                });
            }

            runOnUiThread(() -> {
                // Configuración del adaptador con listeners
                adapter = new RecetaAdapter(listaRecetas, new RecetaAdapter.OnItemClickListener() {
                    // Accion editar receta
                    @Override
                    public void onEditar(Receta receta) {
                        Intent intent = new Intent(MainActivity.this, EditarRecetaActivity.class);
                        intent.putExtra("receta_id", receta.getId());
                        startActivityForResult(intent, EDITAR_RECETA_REQUEST);
                    }
                    // Accion borrar receta
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

                    // Accion marcar como favoirita
                    @Override
                    public void onFavorito(Receta receta) {
                        new Thread(() -> {
                            boolean nuevoEstado = !receta.isFavorita();
                            receta.setFavorita(!receta.isFavorita());
                            db.recetaDao().actualizar(receta);
                            runOnUiThread(() -> {
                                // Recargar después de modificar favorito
                                cargarRecetas(soloFavoritas);
                            });
                        }).start();
                    }
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    // Metodo para actualizar la lista de recetas al añadir o editar una receta
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

    // Metodo para crear el menú de la toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        actualizarIconosMenu(menu);
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


    // Metodo para añadir una receta
    private void anadirReceta() {
        Intent intent = new Intent(MainActivity.this, AddRecetaActivity.class);
        startActivityForResult(intent, AÑADIR_RECETA_REQUEST);
    }

    // Metodo para alternar entre modo claro y oscuro
    private void alternarModo() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    // Metodo para cambiar el idioma de la aplicación
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

    // Metodo para actualizar los iconos del menú en función de modo claro u oscuro
    private void actualizarIconosMenu(Menu menu) {
        boolean isNightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        // Ícono de "Todas las recetas"
        MenuItem itemTodas = menu.findItem(R.id.action_todas);
        itemTodas.setIcon(isNightMode ? R.drawable.ic_todas : R.drawable.ic_todas_dia);

        // Ícono de "Favoritas"
        MenuItem itemFavoritas = menu.findItem(R.id.action_favoritas);
        itemFavoritas.setIcon(isNightMode ? R.drawable.ic_favorito : R.drawable.ic_favorito_dia);

        // Ícono de "Añadir receta"
        MenuItem itemAgregar = menu.findItem(R.id.action_agregar);
        itemAgregar.setIcon(isNightMode ? R.drawable.ic_anadir : R.drawable.ic_anadir_dia);

        // Ícono del botón de tema (cambia a su opuesto)
        MenuItem itemTema = menu.findItem(R.id.action_tema);
        itemTema.setIcon(isNightMode ? R.drawable.ic_claro : R.drawable.ic_oscuro);

        // Ícono de "Salir"
        MenuItem itemSalir = menu.findItem(R.id.action_salir);
        itemSalir.setIcon(isNightMode ? R.drawable.ic_salir : R.drawable.ic_salir_dia);
    }

    // Metodo para manejar las acciones de los elementos del toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Opcion toolbar todas las recetas
        if (id == R.id.action_todas) {
            cargarRecetas(false);
            return true;
        // Opcion toolbar recetas favoritas
        } else if (id == R.id.action_favoritas) {
            cargarRecetas(true);
            return true;
        // Opcion toolbar añadir receta
        } else if (id == R.id.action_agregar) {
            anadirReceta();
            return true;
        // Opcion toolbar tema
        } else if (id == R.id.action_tema) {
            alternarModo();
            return true;
        // Opcion toolbar salir
        } else if (id == R.id.action_salir) {
            SalirDialog salirDialogo = new SalirDialog();
            salirDialogo.show(getSupportFragmentManager(), "SalirDialogo");
            return true;
        // Opcion toolbar salir
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
