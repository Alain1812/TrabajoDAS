package com.example.trabajorecetas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

import com.example.trabajorecetas.BaseDeDatos.Receta;


public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.ViewHolder> {

    private List<Receta> recetas;
    private List<Receta> recetasOriginales;
    private OnItemClickListener listener;

    // Constructor: inicializa las listas y asigna el listener
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

    // Metodo para inflar el layout del item (CardView)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receta, parent, false);
        return new ViewHolder(view);
    }

    // Metodo para enlazar los datos de una receta con el item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener la receta en la posición actual
        Receta receta = recetas.get(position);
        // Establecer el nombre y los ingredientes en los TextViews
        holder.tvNombre.setText(receta.getNombre());
        holder.tvIngredientes.setText(receta.getIngredientes());

        // Convertir el byte array de la imagen a Bitmap usando la clase ImageConverter y mostrarla
        Bitmap imagen = ImageConverter.byteArrayToBitmap(receta.getImagen());
        holder.ivReceta.setImageBitmap(imagen);

        // Establecer el icono de favorito según el estado de la receta
        holder.btnFavorito.setImageResource(receta.isFavorita() ? R.drawable.ic_favorito_filled : R.drawable.ic_favorito);

        // Boton editar
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(receta));
        // Boton borrar
        holder.btnBorrar.setOnClickListener(v -> listener.onBorrar(receta));
        // Boton favorito
        holder.btnFavorito.setOnClickListener(v -> listener.onFavorito(receta));
        // Boton compartir
        holder.btnCompartir.setOnClickListener(v -> {
            String mensaje ="🍽️" + R.string.Nombre_receta + receta.getNombre() + "*\n\n" +
                    "📝" + R.string.Ingredientes + ":\n" + receta.getIngredientes() + "\n\n" +
                    "📖" + R.string.Pasos + ":\n" + receta.getPasos();
                    
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
        // Boton descargar
        holder.btnDescargar.setOnClickListener(v -> guardarArchivoTxt(receta, holder.itemView.getContext()));
        // Al hacer clic en el item, abrir la actividad DetalleRecetaActivity
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, DetalleRecetaActivity.class);
            intent.putExtra("receta_id", receta.getId());
            context.startActivity(intent);
        });


    }

    // Metodo que guarda la imagen en un archivo temporal y retorna su URI para compartirla
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

    // Metodo para guardar la receta en un archivo de texto y compartirla o abrirlo
    private void guardarArchivoTxt(Receta receta, Context context) {
        String nombreArchivo = "Receta_" + receta.getNombre().replace(" ", "_") + ".txt";
        // Crear el contenido del archivo
        String contenido = "Nombre: " + receta.getNombre() + "\n\n" +
                "Ingredientes:\n" + receta.getIngredientes() + "\n\n" +
                "Pasos:\n" + receta.getPasos();

        OutputStream fos = null;
        Uri fileUri = null;

        try {
            // Obtener el ContentResolver para insertar el archivo
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Guardar en la carpeta de Descargas
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (fileUri != null) {
                    fos = resolver.openOutputStream(fileUri);
                }
            } else {
                // Para versiones anteriores, se utiliza el directorio público
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
                fos = new FileOutputStream(file);
                fileUri = Uri.fromFile(file);
            }

            if (fos != null) {
                fos.write(contenido.getBytes());
                fos.close();

                // Mostrar notificación de descarga utilizando el metodo de NotificacionHelper
                NotificacionHelper.mostrarNotificacion(context);

                // Abrir el archivo después de guardarlo
                abrirArchivo(context, fileUri);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.error_guardar_archivo), Toast.LENGTH_SHORT).show();
        }
    }

    // Metodo para abrir el archivo de texto guardado
    private void abrirArchivo(Context context, Uri fileUri) {
        if (fileUri == null) {
            Toast.makeText(context, context.getString(R.string.error_abrir_archivo), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "text/plain");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.no_hay_aplicacion), Toast.LENGTH_SHORT).show();
        }
    }

    // Metodo para filtrar las recetas según una consulta de búsqueda
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
        notifyDataSetChanged();
    }
    // Clase ViewHolder para mantener las referencias a las vistas de cada item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivReceta;
        TextView tvNombre, tvIngredientes;
        ImageButton btnEditar, btnBorrar, btnFavorito, btnCompartir, btnDescargar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asignar las vistas usando findViewById
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
