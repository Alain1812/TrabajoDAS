package com.example.trabajorecetas;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageConverter {

    // Convierte un Bitmap en un ByteArray para almacenarlo en la base de datos
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // Convierte un ByteArray de la base de datos en un Bitmap para mostrarlo
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    // Carga un Bitmap por defecto desde un recurso
    public static Bitmap loadDefaultBitmap(Resources resources, int drawableId) {
        return BitmapFactory.decodeResource(resources, drawableId);
    }

    // Calcula el inSampleSize (factor de muestreo) que se usará para redimensionar la imagen.
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Incrementa el inSampleSize hasta que las dimensiones reducidas sean menores o iguales a las requeridas
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Decodifica y redimensiona un Bitmap desde una URI para evitar imágenes demasiado grandes.

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) throws IOException {
        // =btener solo las dimensiones de la imagen
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(is, null, options);
        is.close();

        // Calcular el factor de muestreo
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decodificar el Bitmap con el factor de muestreo calculado
        options.inJustDecodeBounds = false;
        is = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
        is.close();
        return bitmap;
    }
}
