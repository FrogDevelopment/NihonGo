/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.options;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.Preferences;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;

public class ParametersFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_options_parameters, container, false);

		ButterKnife.bind(this, view);
		return view;
	}

	@OnClick(R.id.options_erase)
	void onClickErase() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_erase_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

					SharedPreferences settings = getActivity().getSharedPreferences(Preferences.PREFS_NAME.value, 0);
					final SharedPreferences.Editor editor = settings.edit();
					editor.putString(Preferences.PACKS.value, "");
					editor.apply();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}

	@OnClick(R.id.options_reset_favorite)
	void onClickReseteFavorite() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_erase_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

					SharedPreferences settings = getActivity().getSharedPreferences(Preferences.PREFS_NAME.value, 0);
					final SharedPreferences.Editor editor = settings.edit();
					editor.putString(Preferences.PACKS.value, "");
					editor.apply();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}

	@OnClick(R.id.options_reset_learned)
	void onClickReseteLearned() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_erase_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

					SharedPreferences settings = getActivity().getSharedPreferences(Preferences.PREFS_NAME.value, 0);
					final SharedPreferences.Editor editor = settings.edit();
					editor.putString(Preferences.PACKS.value, "");
					editor.apply();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}
}
