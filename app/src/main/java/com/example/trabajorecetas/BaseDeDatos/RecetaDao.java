package com.example.trabajorecetas.BaseDeDatos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecetaDao {

    @Insert
    void insertar(Receta receta);

    @Update
    void actualizar(Receta receta);

    @Delete
    void eliminar(Receta receta);

    @Query("SELECT * FROM receta ORDER BY nombre ASC")
    List<Receta> obtenerTodas();

    @Query("SELECT * FROM receta WHERE favorita = 1 ORDER BY nombre ASC")
    List<Receta> obtenerFavoritas();

    @Query("SELECT * FROM receta WHERE id = :id")
    Receta obtenerPorId(int id);

}
