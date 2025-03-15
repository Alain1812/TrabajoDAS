package com.example.trabajorecetas.BaseDeDatos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecetaDao {

    // Insertar una receta
    @Insert
    void insertar(Receta receta);

    // Actualizar una receta
    @Update
    void actualizar(Receta receta);

    // Eliminar una receta
    @Delete
    void eliminar(Receta receta);

    // Obtener todas las recetas
    @Query("SELECT * FROM receta ORDER BY nombre ASC")
    List<Receta> obtenerTodas();

    // Obtener las recetas favoritas
    @Query("SELECT * FROM receta WHERE favorita = 1 ORDER BY nombre ASC")
    List<Receta> obtenerFavoritas();

    // Obtener una receta por su id
    @Query("SELECT * FROM receta WHERE id = :id")
    Receta obtenerPorId(int id);

}
