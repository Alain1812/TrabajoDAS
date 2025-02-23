package com.example.trabajorecetas.BaseDeDatos;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

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
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
