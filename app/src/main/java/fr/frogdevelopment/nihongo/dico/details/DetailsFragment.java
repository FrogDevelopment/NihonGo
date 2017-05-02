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

		populateView(rootView);

		// after populateView() as we need to check mItem.isBookmarked()
		setHasOptionsMenu(true);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.details, menu);

		MenuItem bookmarkItem = menu.findItem(R.id.details_menu_bookmark);
		bookmarkItem.setIcon(mItem.isBookmarked() ? R.drawable.ic_bookmark_on : R.drawable.ic_bookmark_off);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.details_menu_bookmark:
				bookmarkItem();
				return true;

			case R.id.details_menu_help:
				HelpDialog.show(getFragmentManager(), R.layout.dialog_help_details);
				return true;

			default:
				return false;
		}
	}

	private void populateView(View rootView) {
		TextView inputView = (TextView) rootView.findViewById(R.id.details_word_input);
		TextView kanjiView = (TextView) rootView.findViewById(R.id.details_word_kanji);
		TextView kanaView = (TextView) rootView.findViewById(R.id.details_word_kana);
		TextView detailsTitleView = (TextView) rootView.findViewById(R.id.details_word_info_title);
		TextView detailsView = (TextView) rootView.findViewById(R.id.details_word_info);
		TextView exampleTitleView = (TextView) rootView.findViewById(R.id.details_word_example_title);
		TextView exampleView = (TextView) rootView.findViewById(R.id.details_word_example);
		TextView tagsView = (TextView) rootView.findViewById(R.id.details_word_tags);
		TextView successView = (TextView) rootView.findViewById(R.id.details_word_success);

		Bundle args = getArguments();

		mItem = args.getParcelable("item");

		if (mItem == null) {
			return;
		}

		inputView.setText(mItem.input);

		if (StringUtils.isNotEmpty(mItem.kanji)) {
			kanjiView.setText(mItem.kanji);
			kanjiView.setVisibility(View.VISIBLE);
		}

		if (StringUtils.isNotEmpty(mItem.kana)) {
			kanaView.setText(mItem.kana);
		}

		detailsView.setText(mItem.details);
		if (StringUtils.isNotEmpty(mItem.details)) {
			detailsView.setText(mItem.details);
			detailsView.setVisibility(View.VISIBLE);
			detailsTitleView.setVisibility(View.VISIBLE);
		}

		exampleView.setText(mItem.example);
		if (StringUtils.isNotEmpty(mItem.example)) {
			exampleView.setText(mItem.example);
			exampleView.setVisibility(View.VISIBLE);
			exampleTitleView.setVisibility(View.VISIBLE);
		}

		if (StringUtils.isNotEmpty(mItem.tags)) {
			tagsView.setText(getString(R.string.details_tags, mItem.tags));
			tagsView.setVisibility(View.VISIBLE);
		}

		int total = mItem.success + mItem.failed;
		successView.setText(getString(R.string.details_ratio, (total == 0) ? 0 : ((mItem.success / total) * 100)));

		handleRate(mItem.learned);
	}

	private void handleRate(int rate) {
		switch (rate) {

			case 1:
				mRate0.setImageResource(R.drawable.ic_star_black_24dp);
				mRate1.setImageResource(R.drawable.ic_star_black_24dp);
				mRate2.setImageResource(R.drawable.ic_star_border_black_24dp);

				break;
			case 2:
				mRate0.setImageResource(R.drawable.ic_star_black_24dp);
				mRate1.setImageResource(R.drawable.ic_star_black_24dp);
				mRate2.setImageResource(R.drawable.ic_star_black_24dp);

				break;

			default:
			case 0:
				mRate0.setImageResource(R.drawable.ic_star_black_24dp);
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

	private void bookmarkItem() {
		mItem.switchBookmark();
		getActivity().invalidateOptionsMenu();

		final ContentValues values = new ContentValues();
		values.put(DicoContract.BOOKMARK, mItem.bookmark);
		updateItem(values);
	}

	private void updateItem(ContentValues values) {
		final String where = DicoContract._ID + "=?";
		final String[] selectionArgs = {mItem.id};

		getActivity().getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
	}

}
