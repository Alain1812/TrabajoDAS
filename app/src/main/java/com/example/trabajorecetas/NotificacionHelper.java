package com.example.trabajorecetas;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificacionHelper {

    private static final String CHANNEL_ID = "IdCanal";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE_NOTIFICATION = 11;

    public static void solicitarPermisos(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION);
            }
        }
    }

    public static void mostrarNotificacion(Context context) {
        Log.d("Notificacion", "Intentando mostrar notificación...");

        NotificationManager elManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel(
                    CHANNEL_ID, "NombreCanal", NotificationManager.IMPORTANCE_HIGH);
            elCanal.setDescription("Canal de notificaciones para descargas");
            elManager.createNotificationChannel(elCanal);
        }

        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(context.getString(R.string.descarga_completa))
                .setContentText(context.getString(R.string.mensaje_descarga))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (elManager != null) {
            elManager.notify(NOTIFICATION_ID, elBuilder.build());
            Log.d("Notificacion", "Notificación enviada correctamente.");
        } else {
            Log.e("Notificacion", "NotificationManager es NULL.");
        }
    }
}
