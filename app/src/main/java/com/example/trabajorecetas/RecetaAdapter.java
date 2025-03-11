package com.example.trabajorecetas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;

import com.example.trabajorecetas.BaseDeDatos.ImageConverter;
import com.example.trabajorecetas.BaseDeDatos.Receta;


public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.ViewHolder> {

    private List<Receta> recetas;
    private List<Receta> recetasOriginales;
    private OnItemClickListener listener;

    public RecetaAdapter(List<Receta> recetas, OnItemClickListener listener) {
        this.recetas = recetas;
        this.recetasOriginales = new ArrayList<>(recetas);
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

        Bitmap imagen = ImageConverter.byteArrayToBitmap(receta.getImagen());
        holder.ivReceta.setImageBitmap(imagen);

        holder.btnFavorito.setImageResource(receta.isFavorita() ? R.drawable.ic_favorito : R.drawable.ic_favorito);

        // Eventos de los botones
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(receta));
        holder.btnBorrar.setOnClickListener(v -> listener.onBorrar(receta));
        holder.btnFavorito.setOnClickListener(v -> listener.onFavorito(receta));
        holder.btnCompartir.setOnClickListener(v -> {
            String mensaje = "¡Mira esta receta!\n\n" +
                    "🍽️ *" + receta.getNombre() + "*\n\n" +
                    "📝 Ingredientes:\n" + receta.getIngredientes() + "\n\n" +
                    "📖 Pasos:\n" + receta.getPasos();

            Uri imagenUri = guardarImagenTemporal(v.getContext(), imagen);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);
            intent.putExtra(Intent.EXTRA_STREAM, imagenUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(intent, "Compartir receta");
            if (intent.resolveActivity(v.getContext().getPackageManager()) != null) {
                v.getContext().startActivity(chooser);
            }
        });
        holder.btnDescargar.setOnClickListener(v -> guardarArchivoTxt(receta, holder.itemView.getContext()));
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, DetalleRecetaActivity.class);
            intent.putExtra("receta_id", receta.getId());
            context.startActivity(intent);
        });


    }

    private Uri guardarImagenTemporal(Context context, Bitmap bitmap) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();

            File file = new File(cachePath, "receta_compartir.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public int getItemCount() {
        return recetas.size();
    }

    private void guardarArchivoTxt(Receta receta, Context context) {
        String nombreArchivo = "Receta_" + receta.getNombre().replace(" ", "_") + ".txt";
        String contenido = "Nombre: " + receta.getNombre() + "\n\n" +
                "Ingredientes:\n" + receta.getIngredientes() + "\n\n" +
                "Pasos:\n" + receta.getPasos();

        OutputStream fos = null;
        Uri fileUri = null;

        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (fileUri != null) {
                    fos = resolver.openOutputStream(fileUri);
                }
            } else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
                fos = new FileOutputStream(file);
                fileUri = Uri.fromFile(file);
            }

            if (fos != null) {
                fos.write(contenido.getBytes());
                fos.close();

                // Mostrar notificación de descarga utilizando el método de NotificacionHelper
                NotificacionHelper.mostrarNotificacion(context);

                // Abrir el archivo después de guardarlo
                abrirArchivo(context, fileUri);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirArchivo(Context context, Uri fileUri) {
        if (fileUri == null) {
            Toast.makeText(context, "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "text/plain");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No hay aplicación para abrir archivos de texto", Toast.LENGTH_SHORT).show();
        }
    }


    public void filter(String query) {
        recetas.clear();
        if (query.isEmpty()) {
            recetas.addAll(recetasOriginales);
        } else {
            for (Receta receta : recetasOriginales) {
                if (receta.getNombre().toLowerCase().contains(query.toLowerCase())) {
                    recetas.add(receta);
                }
            }
        }
        notifyDataSetChanged(); // Actualizar el RecyclerView con los resultados filtrados
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivReceta;
        TextView tvNombre, tvIngredientes;
        ImageButton btnEditar, btnBorrar, btnFavorito, btnCompartir, btnDescargar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReceta = itemView.findViewById(R.id.iv_receta);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvIngredientes = itemView.findViewById(R.id.tv_ingredientes);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnBorrar = itemView.findViewById(R.id.btn_borrar);
            btnFavorito = itemView.findViewById(R.id.btn_favorito);
            btnCompartir = itemView.findViewById(R.id.btn_compartir);
            btnDescargar = itemView.findViewById(R.id.btn_descargar);
        }
    }
}
