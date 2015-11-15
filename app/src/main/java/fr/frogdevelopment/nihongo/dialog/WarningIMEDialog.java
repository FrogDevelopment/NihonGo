/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import fr.frogdevelopment.nihongo.Preferences;
import fr.frogdevelopment.nihongo.R;

public class WarningIMEDialog extends DialogFragment {

	public static void show(FragmentManager manager) {
		new WarningIMEDialog().show(manager,"warningIMEDialog");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_warning_black);
		builder.setTitle(R.string.warning_ime_title);
		final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_warning_ime, null);
		builder.setView(dialogView);

		// Set the action buttons
		builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
			final CheckBox remember = (CheckBox) dialogView.findViewById(R.id.warningCB);

			SharedPreferences settings = getActivity().getSharedPreferences(Preferences.PREFS_NAME.value, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(Preferences.REMEMBER_WARNING_IME.value, remember.isChecked());
			// Commit the edits!
			editor.apply();

			getDialog().dismiss();
		});

		return builder.create();
	}
}
