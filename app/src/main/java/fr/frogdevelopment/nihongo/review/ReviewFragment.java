/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;

public class ReviewFragment extends Fragment {

	interface OnFragmentInteractionListener {
		void setFavorite(Item item);

		void setLearned(Item item);
	}

	private WeakReference<OnFragmentInteractionListener> mListener;


	@Bind(R.id.review_count)
	TextView             mCount;
	@Bind(R.id.review_reviewed)
	TextView             mReviewed;
	@Bind(R.id.review_textSwitcher_kana)
	TextSwitcher         mKana;
	@Bind(R.id.review_textSwitcher_test)
	TextSwitcher         mTest;
	@Bind(R.id.review_details)
	TextView             mDetails;
	@Bind(R.id.fab_favorite)
	FloatingActionButton mFabFavorite;
	@Bind(R.id.fab_learned)
	FloatingActionButton mFabLearned;

	private Item   mItem;
	private String test;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// The last two arguments ensure LayoutParams are inflated properly.
		View rootView = inflater.inflate(R.layout.fragment_review, container, false);

		ButterKnife.bind(this, rootView);

		populateView();

		initFabs();

		return rootView;
	}

	private void initFabs() {
		mFabFavorite.setOnClickListener(view -> {
			mItem.switchFavorite();
			mListener.get().setFavorite(mItem);
			mFabFavorite.setImageResource(mItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);
		});
		mFabFavorite.setImageResource(mItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);

		mFabLearned.setOnClickListener(view -> {
			mItem.switchLearned();
			mListener.get().setLearned(mItem);
			mFabLearned.setImageResource(mItem.isLearned() ? R.drawable.fab_bookmark_on : R.drawable.fab_bookmark_off);
		});
		mFabLearned.setImageResource(mItem.isLearned() ? R.drawable.fab_bookmark_on : R.drawable.fab_bookmark_off);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mListener = new WeakReference<>((OnFragmentInteractionListener) context);
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	private void populateView() {
		Bundle args = getArguments();

		String count = args.getString("count");
		mCount.setText(count);

		mItem = args.getParcelable("item");

		boolean isJapaneseReviewed = args.getBoolean("isJapaneseReviewed");

		boolean kanjiPst = false;
		if (StringUtils.isNoneBlank(mItem.kanji)) {
			kanjiPst = true;
			if (isJapaneseReviewed) {
				mReviewed.setText(mItem.kanji);
			} else {
				test = mItem.kanji;
				mTest.setText(getActivity().getString(R.string.review_switch_kanji));
			}
		}

		if (StringUtils.isNoneBlank(mItem.kana)) {
			if (!kanjiPst) {
				if (isJapaneseReviewed) {
					mReviewed.setText(mItem.kana);
				} else {
					test = mItem.kana;
					mTest.setText(getActivity().getString(R.string.review_switch_kana));
				}
				mKana.setVisibility(View.GONE);
			} else {
				mKana.setText(getActivity().getString(R.string.review_switch_kana));
			}
		} else {
			mKana.setVisibility(View.GONE);
		}

		if (isJapaneseReviewed) {
			test = mItem.input;
			mTest.setText(getActivity().getString(R.string.review_switch_input));
		} else {
			mReviewed.setText(mItem.input);
		}

		mDetails.setText(mItem.details);
	}

	@OnClick(R.id.review_textSwitcher_kana)
	void onClickKana() {
		mKana.setText(mItem.kana);
		mKana.setClickable(false);
	}

	@OnClick(R.id.review_textSwitcher_test)
	void onClickTest() {
		mTest.setText(test);
		mTest.setClickable(false);
		if (mDetails.getVisibility() == View.INVISIBLE) {
			mDetails.setVisibility(View.VISIBLE);
		}
	}

}
