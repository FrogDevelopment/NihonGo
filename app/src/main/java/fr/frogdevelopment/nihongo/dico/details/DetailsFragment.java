/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.app.Fragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;

public class DetailsFragment extends Fragment {

	private Item mItem;

	private ImageView mFavorite;
	private ImageView mRate0;
	private ImageView mRate1;
	private ImageView mRate2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// The last two arguments ensure LayoutParams are inflated properly.
		View rootView = inflater.inflate(R.layout.fragment_details, container, false);

		mRate0 = (ImageView) rootView.findViewById(R.id.rate_0);
		mRate0.setOnClickListener(v -> setRate(0));
		mRate1 = (ImageView) rootView.findViewById(R.id.rate_1);
		mRate1.setOnClickListener(v -> setRate(1));
		mRate2 = (ImageView) rootView.findViewById(R.id.rate_2);
		mRate2.setOnClickListener(v -> setRate(2));

		mFavorite = (ImageView) rootView.findViewById(R.id.details_favorite);
		mFavorite.setOnClickListener(v -> setFavorite());

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

		mItem = args.getParcelable("item");

		if (mItem == null) {
			return;
		}

		mInputView.setText(mItem.input);

		if (StringUtils.isNoneEmpty(mItem.kanji)) {
			mKanjiView.setText(mItem.kanji);
			mKanjiView.setVisibility(View.VISIBLE);
		}

		if (StringUtils.isNoneEmpty(mItem.kana)) {
			mKanaView.setText(mItem.kana);
			mKanaView.setVisibility(View.VISIBLE);
		}

		mDetailsView.setText(mItem.details);
		if (StringUtils.isNoneEmpty(mItem.details)) {
			mDetailsView.setText(mItem.details);
			mDetailsView.setVisibility(View.VISIBLE);
			mDetailsTitleView.setVisibility(View.VISIBLE);
		}

		mExampleView.setText(mItem.example);
		if (StringUtils.isNoneEmpty(mItem.example)) {
			mExampleView.setText(mItem.example);
			mExampleView.setVisibility(View.VISIBLE);
			mExampleTitleView.setVisibility(View.VISIBLE);
		}

		if (StringUtils.isNoneEmpty(mItem.tags)) {
			mTagsView.setText(mItem.tags);
			mTagsView.setVisibility(View.VISIBLE);
		}

		int total = mItem.success + mItem.failed;
		mRatio.setText(getString(R.string.details_ratio, (total == 0) ? 0 : ((mItem.success / total) * 100)));

		handleRate(mItem.learned);
		handleFavorite();
	}

	private void handleFavorite() {
		mFavorite.setImageResource(mItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);
	}

	private void handleRate(int rate) {
		switch (rate) {

			case 1:
				mRate0.setImageResource(R.drawable.ic_star_black_48dp);
				mRate1.setImageResource(R.drawable.ic_star_black_48dp);
				mRate2.setImageResource(R.drawable.ic_star_border_black_24dp);

				break;
			case 2:
				mRate0.setImageResource(R.drawable.ic_star_black_48dp);
				mRate1.setImageResource(R.drawable.ic_star_black_48dp);
				mRate2.setImageResource(R.drawable.ic_star_black_48dp);

				break;

			default:
			case 0:
				mRate0.setImageResource(R.drawable.ic_star_black_48dp);
				mRate1.setImageResource(R.drawable.ic_star_border_black_24dp);
				mRate2.setImageResource(R.drawable.ic_star_border_black_24dp);

				break;
		}
	}

	private void setRate(int rate) {
		mItem.learned = rate;
		handleRate(rate);

		final ContentValues values = new ContentValues();
		values.put(DicoContract.LEARNED, mItem.learned);
		updateItem(values);
	}

	private void setFavorite() {
		mItem.switchFavorite();
		handleFavorite();

		final ContentValues values = new ContentValues();
		values.put(DicoContract.FAVORITE, mItem.favorite);
		updateItem(values);
	}

	private void updateItem(ContentValues values) {
		final String where = DicoContract._ID + "=?";
		final String[] selectionArgs = {mItem.id};

		getActivity().getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
	}

}
