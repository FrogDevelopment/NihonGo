/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.about;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.frogdevelopment.nihongo.R;

public class AboutFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_about, container, false);

		MovementMethod instance = LinkMovementMethod.getInstance();

		TextView hiragana = (TextView) rootView.findViewById(R.id.about_hiragana_link);
		hiragana.setMovementMethod(instance);

		TextView katakana= (TextView) rootView.findViewById(R.id.about_katakana_link);
		katakana.setMovementMethod(instance);

		return rootView;
	}

}
