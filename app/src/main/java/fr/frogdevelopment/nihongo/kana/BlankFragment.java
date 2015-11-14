/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.kana;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;

public class BlankFragment extends Fragment {

	@Bind(R.id.kana_grid)
	GridLayout gridLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_blank, container, false);

		ButterKnife.bind(this, view);

		setListeners();

		return view;
	}

	private void setListeners() {

		gridLayout.removeAllViews();

		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;

		for (Hiragana hiragana : Hiragana.values()) {

			ImageView imageView = new ImageView(getActivity());

			imageView.setBackgroundResource(hiragana.resource);
			imageView.setContentDescription(hiragana.name());
			imageView.setOnClickListener(v -> drawKana(hiragana));

			GridLayout.Spec rowSpan = GridLayout.spec(hiragana.row, 1);
			GridLayout.Spec colspan = GridLayout.spec(hiragana.col, 1);

			gridLayout.addView(imageView, new GridLayout.LayoutParams(rowSpan, colspan));

			imageView.getLayoutParams().width = width / 5;

			// for touch effect
			imageView.setOnTouchListener((v, event) -> {
						switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN: {
								ImageView view = (ImageView) v;
								//overlay is black with transparency of 0x77 (119)
								view.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
								view.invalidate();
								break;
							}
							case MotionEvent.ACTION_UP:
							case MotionEvent.ACTION_CANCEL: {
								ImageView view = (ImageView) v;
								//clear the overlay
								view.getBackground().clearColorFilter();
								view.invalidate();
								break;
							}
						}
						return false;
					}
			);
		}
	}

	private void drawKana(Hiragana hiragana) {
		Kana kana = new Kana(hiragana);
		Intent intent = new Intent(getActivity(), DrawingActivity.class);
		intent.putExtra("kana", kana);
		startActivity(intent);
		getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

}
