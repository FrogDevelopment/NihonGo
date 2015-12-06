/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.kana;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;

public class KanaViewPage extends Fragment {

	@Bind(R.id.help_imageView)
	ImageView imageView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View rootView = inflater.inflate(R.layout.fragment_kana, container, false);
		ButterKnife.bind(this, rootView);

		final Bundle arguments = getArguments();
		imageView.setImageResource(arguments.getInt("imageSource"));

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// fixme to true when KanaTrainer is available
		setHasOptionsMenu(false);

		return rootView;
	}

	@Override
	public void onDestroyView() {
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		super.onDestroyView();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the options menu from XML
		inflater.inflate(R.menu.kana, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_drawing:

				// http://developer.android.com/training/basics/intents/sending.html
				Intent drawingIntent = new Intent("fr.frogdevelopment.kanatrainer.DRAWING");

				// http://developer.android.com/guide/components/intents-filters.html#ExampleSend
				PackageManager packageManager = getActivity().getPackageManager();
				if (drawingIntent.resolveActivity(packageManager) != null) {
					openDrawingApp();
				} else {
					fetchDrawingApp();
				}

				return true;

			default:
				return false;
		}

	}

	private void openDrawingApp() {
		// // FIXME: 06/12/2015
	}

	// http://developer.android.com/distribute/tools/promote/linking.html
	private void fetchDrawingApp() {
		// fixme ajouter popup information / confirmation

		Intent fetchAppIntent = new Intent(Intent.ACTION_VIEW);
		// fixme link to KanaDrawingTrainer in Google Play Store
		fetchAppIntent.setData(Uri.parse("market://details?id=com.google.android.apps.maps"));
		startActivity(fetchAppIntent);
	}
}
