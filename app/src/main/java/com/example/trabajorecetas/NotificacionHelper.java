package com.example.trabajorecetas;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificacionHelper {

    private static final String CHANNEL_ID = "IdCanal";
    private static final int NOTIFICATION_ID = 1;

    // Solicitar permisos para mostrar notificaciones
    public static void solicitarPermisos(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }

    }

    // Mostrar notificación descarga completa
    public static void mostrarNotificacion(Context context) {
        NotificationManager elManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Para Android Oreo (API 26) o superior, es obligatorio crear un canal de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel(
                    CHANNEL_ID, "NombreCanal", NotificationManager.IMPORTANCE_HIGH);
            elCanal.setDescription("Canal de notificaciones para descargas");
            // Crear el canal en el sistema
            elManager.createNotificationChannel(elCanal);
        }

        // Construir la notificación con los parámetros necesarios
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                // Icono pequeño para la notificación
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                // Título de la notificación
                .setContentTitle(context.getString(R.string.descarga_completa))
                // Texto de la notificación
                .setContentText(context.getString(R.string.mensaje_descarga))
                // Prioridad alta
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // La notificación se cierra al hacer clic
                .setAutoCancel(true);

        // Si el NotificationManager no es nulo, mostrar la notificación
        if (elManager != null) {
            elManager.notify(NOTIFICATION_ID, elBuilder.build());
        }
    }
}
