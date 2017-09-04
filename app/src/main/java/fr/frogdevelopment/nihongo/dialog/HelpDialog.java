/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

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
		LayoutInflater inflater = getActivity().getLayoutInflater();

		int resId = getArguments().getInt("resId");
		View view = inflater.inflate(resId, null);

		final CheckBox remember = view.findViewById(R.id.cb_remember);
		boolean showRemember = getArguments().getBoolean("showRemember");
		remember.setVisibility(showRemember ? View.VISIBLE : View.GONE);


		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.help_title)

				// Inflate and set the layout for the dialog
				// Pass null as the parent view because its going in the dialog layout
				.setView(view)

				// Set the action buttons
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {

					if (showRemember) {
						Preferences preferences;
						switch (resId) {
							case R.layout.dialog_help_dico:
								preferences = Preferences.HELP_DICO;
								break;
							default:
								throw new IllegalStateException("Unknow resId " + resId);
						}

						PreferencesHelper.getInstance(getActivity()).saveBoolean(preferences, remember.isChecked());
					}

					getDialog().dismiss();
				});

		return builder.create();
	}

}
