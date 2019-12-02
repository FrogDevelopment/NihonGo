package fr.frogdevelopment.nihongo;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class ImeWarningDialog extends DialogFragment {

    static void show(FragmentManager manager) {
        new ImeWarningDialog().show(manager, "imeWarningDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.warning_ime_title)
                .setView(R.layout.ime_warning_dialog)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> dismiss(false))
                .setNeutralButton(R.string.cb_remember, (dialog, which) -> dismiss(true))
                .create();
    }

    private void dismiss(boolean skipImeWarning) {
        PreferencesHelper.getInstance(requireActivity()).saveBoolean(Preferences.SKIP_WARNING_IME, skipImeWarning);
        requireDialog().dismiss();
    }
}
