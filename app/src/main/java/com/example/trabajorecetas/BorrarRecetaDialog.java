package com.example.trabajorecetas;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class BorrarRecetaDialog extends DialogFragment {

    private OnBorrarRecetaListener listener;

    // Interfaz para comunicarse con la actividad que llama al diálogo
    public interface OnBorrarRecetaListener {
        void onBorrarRecetaConfirmado();
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String titulo = getString(R.string.titulo_borrar_receta);
        String mensaje = getString(R.string.mensaje_borrar_receta);
        String botonSi = getString(R.string.boton_si);
        String botonNo = getString(R.string.boton_no);

        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(botonSi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Llamamos al listener para que se borre la receta
                        if (listener != null) {
                            listener.onBorrarRecetaConfirmado();
                        }
                    }
                })
                .setNegativeButton(botonNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // No hace nada, simplemente cierra el diálogo
                        dialog.dismiss();
                    }
                });

        return builder.create(); // Devuelve el diálogo creado
    }

    public void setOnBorrarRecetaListener(OnBorrarRecetaListener listener) {
        this.listener = listener;
    }
}
