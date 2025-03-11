package com.example.trabajorecetas.BaseDeDatos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "receta")
public class Receta implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String ingredientes;
    private String pasos;
    private byte[] imagen;
    private boolean favorita;

    // Constructor
    public Receta(String nombre, String ingredientes, String pasos, byte[] imagen, boolean favorita) {
        this.nombre = nombre;
        this.ingredientes = ingredientes;
        this.pasos = pasos;
        this.imagen = imagen;
        this.favorita = favorita;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }

    public String getPasos() { return pasos; }
    public void setPasos(String pasos) { this.pasos = pasos; }

    public byte[] getImagen() { return imagen; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }

    public boolean isFavorita() { return favorita; }
    public void setFavorita(boolean favorita) { this.favorita = favorita; }
}
