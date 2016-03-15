/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import fr.frogdevelopment.nihongo.dialog.HelpDialog;

public class ReviewFragment extends Fragment {

	interface OnFragmentInteractionListener {
		void setFavorite(Item item);

		void setLearned(Item item);

		void reviewAgain();
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
	@Bind(R.id.review_info_title)
	TextView             mInfoTitle;
	@Bind(R.id.review_info)
	TextView             mInfo;
	@Bind(R.id.review_example_title)
	TextView             mExampleTitle;
	@Bind(R.id.review_example)
	TextView             mExample;
	@Bind(R.id.review_tags_title)
	TextView             mTagsTitle;
	@Bind(R.id.review_tags)
	TextView             mTags;
	@Bind(R.id.fab_again)
	FloatingActionButton mFabAgain;
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

		setHasOptionsMenu(true);

		populateView();

		initFabs();

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.review, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.review_help:
				HelpDialog.show(getFragmentManager(), R.layout.dialog_help_review);
				break;

			default:
				return false;
		}

		return true;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	private void initFabs() {
		mFabAgain.setOnClickListener(view -> mListener.get().reviewAgain());

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

		if (StringUtils.isNotBlank(mItem.details)) {
			mInfo.setText(mItem.details);
			mInfo.setVisibility(View.VISIBLE);
			mInfoTitle.setVisibility(View.VISIBLE);
		} else {
			mInfo.setText(null);
			mInfo.setVisibility(View.GONE);
			mInfoTitle.setVisibility(View.GONE);
		}

		if (StringUtils.isNotBlank(mItem.example)) {
			mExample.setText(mItem.example);
			mExample.setVisibility(View.VISIBLE);
			mExampleTitle.setVisibility(View.VISIBLE);
		} else {
			mExample.setText(null);
			mExample.setVisibility(View.GONE);
			mExampleTitle.setVisibility(View.GONE);
		}

		if (StringUtils.isNotBlank(mItem.tags)) {
			mTags.setText(mItem.tags);
			mTags.setVisibility(View.VISIBLE);
			mTagsTitle.setVisibility(View.VISIBLE);
		} else {
			mTags.setText(null);
			mTags.setVisibility(View.GONE);
			mTagsTitle.setVisibility(View.GONE);
		}
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
	}

}
