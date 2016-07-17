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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.frogdevelopment.nihongo.R;

public class AboutFragment extends Fragment {

	@BindView(R.id.about_hiragana_link)
	TextView hiragana;
	@BindView(R.id.about_katakana_link)
	TextView katakana;
	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		unbinder = ButterKnife.bind(this, view);

		MovementMethod instance = LinkMovementMethod.getInstance();
		hiragana.setMovementMethod(instance);
		katakana.setMovementMethod(instance);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();
	}
}
