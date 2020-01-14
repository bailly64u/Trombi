package com.ufrst.app.trombi;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

// Représente un AlertDialog, pour l'import par liste de noms
public class ImportAlertDialog extends DialogFragment {

    // Interface qui permettra de déporter la gestion du clique
    public interface ImportDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

    private ImportDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ImportDialogListener) context;
        } catch (ClassCastException e) {
            // La classe utilisant le dialog n'implémente pas l'interface de gestion des cliques
            throw new ClassCastException(getActivity().toString()
                    + " doit implémenter ImportDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_import, null))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(ImportAlertDialog.this);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }
}
