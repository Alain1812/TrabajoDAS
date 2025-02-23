package com.example.trabajorecetas;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajorecetas.BaseDeDatos.ImageConverter;
import com.example.trabajorecetas.BaseDeDatos.Receta;

import java.util.List;

public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.ViewHolder> {

    private List<Receta> recetas;
    private OnItemClickListener listener;

    public RecetaAdapter(List<Receta> recetas, OnItemClickListener listener) {
        this.recetas = recetas;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onEditar(Receta receta);
        void onBorrar(Receta receta);
        void onFavorito(Receta receta);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receta receta = recetas.get(position);
        holder.tvNombre.setText(receta.getNombre());
        holder.tvIngredientes.setText(receta.getIngredientes());

        // Convertir byte[] a Bitmap y mostrarlo
        Bitmap imagen = ImageConverter.byteArrayToBitmap(receta.getImagen());
        holder.ivReceta.setImageBitmap(imagen);

        // Cambiar el ícono de favorito
        holder.btnFavorito.setImageResource(receta.isFavorita() ? R.drawable.ic_favorite : R.drawable.ic_favorite);

        // Eventos de los botones
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(receta));
        holder.btnBorrar.setOnClickListener(v -> listener.onBorrar(receta));
        holder.btnFavorito.setOnClickListener(v -> listener.onFavorito(receta));
    }

    @Override
    public int getItemCount() {
        return recetas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivReceta;
        TextView tvNombre, tvIngredientes;
        ImageButton btnEditar, btnBorrar, btnFavorito;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReceta = itemView.findViewById(R.id.iv_receta);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvIngredientes = itemView.findViewById(R.id.tv_ingredientes);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnBorrar = itemView.findViewById(R.id.btn_borrar);
            btnFavorito = itemView.findViewById(R.id.btn_favorito);
        }
    }
}
