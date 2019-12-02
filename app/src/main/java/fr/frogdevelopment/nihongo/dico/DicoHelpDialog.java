package fr.frogdevelopment.nihongo.dico;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import fr.frogdevelopment.nihongo.R;

public class DicoHelpDialog extends DialogFragment {

    static void show(FragmentManager fragmentManager) {
        DicoHelpDialog dialog = new DicoHelpDialog();
        dialog.show(fragmentManager, "dicoHelpDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.help_title)
                .setView(R.layout.dico_help_dialog)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.dismiss())
                .create();
    }

}
