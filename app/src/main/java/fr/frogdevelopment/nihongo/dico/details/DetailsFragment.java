/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;

public class DetailsFragment extends Fragment {

	interface OnFragmentInteractionListener {
//		void update(int position, Item item);
	}

	private OnFragmentInteractionListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// The last two arguments ensure LayoutParams are inflated properly.
		View rootView = inflater.inflate(R.layout.fragment_details, container, false);

		setHasOptionsMenu(true);

		populateView(rootView);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.details, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.details_help) {
			HelpDialog.show(getFragmentManager(), R.layout.dialog_help_details);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mListener = (OnFragmentInteractionListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement " + OnFragmentInteractionListener.class.getSimpleName());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	private void populateView(View rootView) {
		TextView mInputView = (TextView) rootView.findViewById(R.id.details_word_input);
		TextView mKanjiView = (TextView) rootView.findViewById(R.id.details_word_kanji);
		TextView mKanaView = (TextView) rootView.findViewById(R.id.details_word_kana);
		TextView mDetailsTitleView = (TextView) rootView.findViewById(R.id.details_word_info_title);
		TextView mDetailsView = (TextView) rootView.findViewById(R.id.details_word_info);
		TextView mExampleTitleView = (TextView) rootView.findViewById(R.id.details_word_example_title);
		TextView mExampleView = (TextView) rootView.findViewById(R.id.details_word_example);
		TextView mTagsView = (TextView) rootView.findViewById(R.id.details_word_tags);
		TextView mRatio = (TextView) rootView.findViewById(R.id.details_word_ratio);

		Bundle args = getArguments();

		Item item = args.getParcelable("item");

		if (item == null) {
			return;
		}

		mInputView.setText(item.input);

		if (StringUtils.isNoneEmpty(item.kanji)) {
			mKanjiView.setText(item.kanji);
			mKanjiView.setVisibility(View.VISIBLE);
		}

		if (StringUtils.isNoneEmpty(item.kana)) {
			mKanaView.setText(item.kana);
			mKanaView.setVisibility(View.VISIBLE);
		}

		mDetailsView.setText(item.details);
		if (StringUtils.isNoneEmpty(item.details)) {
			mDetailsView.setText(item.details);
			mDetailsView.setVisibility(View.VISIBLE);
			mDetailsTitleView.setVisibility(View.VISIBLE);
		}

		mExampleView.setText(item.example);
		if (StringUtils.isNoneEmpty(item.example)) {
			mExampleView.setText(item.example);
			mExampleView.setVisibility(View.VISIBLE);
			mExampleTitleView.setVisibility(View.VISIBLE);
		}

		if (StringUtils.isNoneEmpty(item.tags)) {
			mTagsView.setText(item.tags);
			mTagsView.setVisibility(View.VISIBLE);
		}

		mRatio.setText(item.success + "/" + item.failed);
	}

}
