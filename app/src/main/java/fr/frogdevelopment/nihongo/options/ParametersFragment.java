/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.options;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

// fixme utiliser les préférences XML ? http://developer.android.com/guide/topics/ui/settings.html
public class ParametersFragment extends Fragment  {

	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_options_parameters, container, false);

		unbinder = ButterKnife.bind(this, view);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();
	}

	@OnClick(R.id.options_erase)
	void onClickErase() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_erase_data_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

					PreferencesHelper.getInstance(getContext()).saveString(Preferences.LESSONS, "");

					Snackbar.make(getActivity().findViewById(R.id.parameters_layout), R.string.options_erase_data_success, Snackbar.LENGTH_LONG).show();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}

	@OnClick(R.id.options_reset_favorite)
	void onClickResetFavorite() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_reset_favorite_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					final ContentValues values = new ContentValues();
					values.put(DicoContract.FAVORITE, "0");
					getActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_FAVORITE, values, null, null);

					Snackbar.make(getActivity().findViewById(R.id.parameters_layout), R.string.options_reset_favorite_success, Snackbar.LENGTH_LONG).show();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}

	@OnClick(R.id.options_reset_learned)
	void onClickResetLearned() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_reset_learned_erase_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					final ContentValues values = new ContentValues();
					values.put(DicoContract.LEARNED, "0");
					getActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_LEARNED, values, null, null);

					Snackbar.make(getActivity().findViewById(R.id.parameters_layout), R.string.options_reset_learned_erase_success, Snackbar.LENGTH_LONG).show();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}
}
