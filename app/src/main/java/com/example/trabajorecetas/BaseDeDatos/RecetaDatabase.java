package com.example.trabajorecetas.BaseDeDatos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.trabajorecetas.ImageConverter;
import com.example.trabajorecetas.R;

@Database(entities = {Receta.class}, version = 1, exportSchema = false)
public abstract class RecetaDatabase extends RoomDatabase {
    public abstract RecetaDao recetaDao();

    private static volatile RecetaDatabase INSTANCE;

    public static RecetaDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RecetaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RecetaDatabase.class, "receta_db")
                            .allowMainThreadQueries()
                            // Añadir datos iniciales
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    new Thread(() -> {
                                        RecetaDao dao = INSTANCE.recetaDao();
                                        Context appContext = context.getApplicationContext();

                                        Receta receta1 = obtenerReceta(appContext,
                                                "Tortilla de Patatas",
                                                "Patatas, Huevos, Sal",
                                                "1. Pelar y cortar patatas. 2. Freír. 3. Batir huevos y mezclar. 4. Cuajar en sartén.",
                                                R.drawable.tortilla_patatas
                                        );

                                        Receta receta2 = obtenerReceta(appContext,
                                                "Ensalada César",
                                                "Lechuga, Pollo, Queso, Pan, Salsa César",
                                                "1. Cortar lechuga. 2. Cocinar pollo. 3. Mezclar ingredientes con salsa.",
                                                R.drawable.ensalada_cesar
                                        );

                                        Receta receta3 = obtenerReceta(appContext,
                                                "Paella Valenciana",
                                                "Arroz, Mariscos, Pollo, Verduras, Azafrán",
                                                "1. Sofreír mariscos y pollo. 2. Agregar arroz y caldo. 3. Cocinar a fuego lento. 4. Servir.",
                                                R.drawable.paella_valenciana
                                        );

                                        Receta receta4 = obtenerReceta(appContext,
                                                "Pizza Margarita",
                                                "Masa, Tomate, Mozzarella, Albahaca",
                                                "1. Preparar la masa. 2. Colocar el tomate y mozzarella. 3. Hornear a 220°C por 15 min.",
                                                R.drawable.pizza_margarita
                                        );

                                        Receta receta5 = obtenerReceta(appContext,
                                                "Gazpacho",
                                                "Tomates, Pepino, Pimiento, Aceite de oliva, Ajo, Vinagre",
                                                "1. Trocear los ingredientes. 2. Triturar hasta obtener una sopa fría.",
                                                R.drawable.gazpacho
                                        );

                                        Receta receta6 = obtenerReceta(appContext,
                                                "Croquetas de Jamón",
                                                "Jamón, Bechamel, Pan rallado, Huevo",
                                                "1. Preparar la bechamel. 2. Mezclar con el jamón picado. 3. Formar las croquetas y freír.",
                                                R.drawable.croquetas_jamon
                                        );

                                        dao.insertar(receta1);
                                        dao.insertar(receta2);
                                        dao.insertar(receta3);
                                        dao.insertar(receta4);
                                        dao.insertar(receta5);
                                        dao.insertar(receta6);
                                    }).start();
                                }

                                private Receta obtenerReceta(Context context, String nombre, String ingredientes, String pasos, int imagenResId) {
                                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imagenResId);
                                    byte[] imagenBytes = ImageConverter.bitmapToByteArray(bitmap);
                                    return new Receta(nombre, ingredientes, pasos, imagenBytes, false);
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
