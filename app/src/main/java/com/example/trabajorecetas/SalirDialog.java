package com.example.trabajorecetas;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class SalirDialog extends DialogFragment {

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
                .setPositiveButton(botonSi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Confirmar salida
                        getActivity().finish(); // Esto cierra la actividad (la aplicación)
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
}
