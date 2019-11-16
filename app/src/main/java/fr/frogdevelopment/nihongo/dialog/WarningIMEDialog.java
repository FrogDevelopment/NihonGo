package fr.frogdevelopment.nihongo.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class WarningIMEDialog extends DialogFragment {

    public static void show(FragmentManager manager) {
        new WarningIMEDialog().show(manager, "warningIMEDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_warning_ime, null);

        return new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.warning_ime_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    final CheckBox remember = dialogView.findViewById(R.id.warningCB);

                    PreferencesHelper.getInstance(requireActivity()).saveBoolean(Preferences.REMEMBER_WARNING_IME, remember.isChecked());

                    requireDialog().dismiss();
                })
                .create();
    }
}
