/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;

import fr.frogdevelopment.nihongo.R;

public class HelpDialog extends DialogFragment {

	public static void show(FragmentManager fragmentManager, int resId) {
		HelpDialog dialog = new HelpDialog();

		Bundle args = new Bundle();
		args.putInt("resId", resId);
		dialog.setArguments(args);

		dialog.show(fragmentManager, "helpDialog");
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		int resId = getArguments().getInt("resId");

		// Use the Builder class for convenient dialog construction
		return new AlertDialog.Builder(getContext())
				.setTitle(R.string.help_title)

				// Inflate and set the layout for the dialog
				// Pass null as the parent view because its going in the dialog layout
				.setView(inflater.inflate(resId, null))

				// Set the action buttons
				.setPositiveButton(android.R.string.ok, (dialog, id) -> getDialog().dismiss())
				.create();
	}

}
