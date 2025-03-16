package com.example.trabajorecetas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
        // Constructor del diálogo usando AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Cargar textos desde recursos para facilitar la localización
        String titulo = getString(R.string.titulo_borrar_receta);
        String mensaje = getString(R.string.mensaje_borrar_receta);
        String botonSi = getString(R.string.boton_si);
        String botonNo = getString(R.string.boton_no);

        // Configurar el diálogo
        builder.setTitle(titulo)
                .setMessage(mensaje)
                // Botón de confirmación: ejecuta la acción de borrado
                .setPositiveButton(botonSi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Llamamos al listener para que se borre la receta
                        if (listener != null) {
                            listener.onBorrarRecetaConfirmado();
                        }
                    }
                })
                // Botón de cancelación: cierra el diálogo sin acciones
                .setNegativeButton(botonNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // No hace nada, simplemente cierra el diálogo
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();

        // Cambiar el color de los botones cuando el diálogo se muestre
        dialog.setOnShowListener(dialogInterface -> {
            Button botonSiView = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button botonNoView = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Obtener color desde colors.xml
            int colorTexto = ContextCompat.getColor(requireContext(), R.color.textLight);

            // Aplicar color a los botones
            botonSiView.setTextColor(colorTexto);
            botonNoView.setTextColor(colorTexto);
        });

        return dialog;
    }

    // Setter para el listener
    public void setOnBorrarRecetaListener(OnBorrarRecetaListener listener) {
        this.listener = listener;
    }
}
