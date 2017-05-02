/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class WarningIMEDialog extends DialogFragment {

	public static void show(FragmentManager manager) {
		new WarningIMEDialog().show(manager,"warningIMEDialog");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_warning);
		builder.setTitle(R.string.warning_ime_title);
		final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_warning_ime, null);
		builder.setView(dialogView);

		// Set the action buttons
		builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
			final CheckBox remember = (CheckBox) dialogView.findViewById(R.id.warningCB);

			PreferencesHelper.getInstance(getActivity()).saveBoolean(Preferences.REMEMBER_WARNING_IME, remember.isChecked());

			getDialog().dismiss();
		});

		return builder.create();
	}
}
