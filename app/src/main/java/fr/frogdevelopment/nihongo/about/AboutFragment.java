/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;

public class AboutFragment extends Fragment  {

	@Bind(R.id.about_logos_link)
	TextView logos;
	@Bind(R.id.about_hiragana_link)
	TextView hiragana;
	@Bind(R.id.about_katakana_link)
	TextView katakana;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		ButterKnife.bind(this, view);

		MovementMethod instance = LinkMovementMethod.getInstance();
		logos.setMovementMethod(instance);
		hiragana.setMovementMethod(instance);
		katakana.setMovementMethod(instance);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}
}
