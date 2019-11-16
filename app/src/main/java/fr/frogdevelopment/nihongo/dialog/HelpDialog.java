package fr.frogdevelopment.nihongo.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class HelpDialog extends DialogFragment {

    public static void show(FragmentManager fragmentManager, int resId) {
        show(fragmentManager, resId, false);
    }

    public static void show(FragmentManager fragmentManager, int resId, boolean showRemember) {
        HelpDialog dialog = new HelpDialog();

        Bundle args = new Bundle();
        args.putInt("resId", resId);
        args.putBoolean("showRemember", showRemember);
        dialog.setArguments(args);

        dialog.show(fragmentManager, "helpDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        Bundle bundle = requireArguments();
        int resId = bundle.getInt("resId");
        View view = inflater.inflate(resId, null);

        final CheckBox remember = view.findViewById(R.id.cb_remember);
        boolean showRemember = bundle.getBoolean("showRemember");
        remember.setVisibility(showRemember ? View.VISIBLE : View.GONE);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.help_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {

                    if (showRemember) {
                        Preferences preferences;
                        if (resId == R.layout.dialog_help_dico) {
                            preferences = Preferences.HELP_DICO;
                        } else {
                            throw new IllegalStateException("Unknow resId " + resId);
                        }

                        PreferencesHelper.getInstance(requireContext()).saveBoolean(preferences, remember.isChecked());
                    }

                    requireDialog().dismiss();
                })
                .create();
    }

}
