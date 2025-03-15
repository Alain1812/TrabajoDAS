package com.example.trabajorecetas;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class SalirDialog extends DialogFragment {

    // Este metodo se llama para crear el diálogo de confirmación cuando se muestra el diálogo.
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String titulo = getString(R.string.titulo_salir);
        String mensaje = getString(R.string.mensaje_salir);
        String botonSi = getString(R.string.boton_si);
        String botonNo = getString(R.string.boton_no);

        builder.setTitle(titulo)
                .setMessage(mensaje)
                // Botón "Sí": al hacer clic, se cierra la actividad actual, lo que equivale a salir de la aplicación
                .setPositiveButton(botonSi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cierra la actividad y en consecuencia la aplicación
                        getActivity().finish();
                    }
                })
                // Botón "No": al hacer clic, simplemente se cierra el diálogo sin realizar ninguna acción
                .setNegativeButton(botonNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cierra el diálogo sin salir de la actividad
                        dialog.dismiss();
                    }
                });

        // Se crea y devuelve el AlertDialog configurado
        return builder.create();
    }
}
