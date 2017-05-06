/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.app.Fragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;

import static fr.frogdevelopment.nihongo.R.id.review_count;

public class ReviewFragment extends Fragment {

	private Item   mItem;
	private String test;

	private ImageView    mBookmark;
	private ImageView    mRate0;
	private ImageView    mRate1;
	private ImageView    mRate2;
	private TextSwitcher mKanaSwitcher;
	private TextSwitcher mTestSwitcher;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_review, container, false);
	}

	@Override
	public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		TextView countView = (TextView) rootView.findViewById(review_count);
		TextView reviewedView = (TextView) rootView.findViewById(R.id.review_reviewed);
		TextView infoTitleView = (TextView) rootView.findViewById(R.id.review_info_title);
		TextView infoView = (TextView) rootView.findViewById(R.id.review_info);
		TextView exampleTitleView = (TextView) rootView.findViewById(R.id.review_example_title);
		TextView exampleView = (TextView) rootView.findViewById(R.id.review_example);
		TextView tagsView = (TextView) rootView.findViewById(R.id.review_tags);
		TextView successView = (TextView) rootView.findViewById(R.id.review_success);

		mKanaSwitcher = (TextSwitcher) rootView.findViewById(R.id.review_textSwitcher_kana);
		mKanaSwitcher.setOnClickListener(view -> {
			mKanaSwitcher.setText(mItem.kana);
			mKanaSwitcher.setClickable(false);

		});
		mTestSwitcher = (TextSwitcher) rootView.findViewById(R.id.review_textSwitcher_test);
		mTestSwitcher.setOnClickListener(view -> {
			mTestSwitcher.setText(test);
			mTestSwitcher.setClickable(false);

		});

		Bundle args = getArguments();
		String count = args.getString("count");
		countView.setText(count);
		mItem = args.getParcelable("item");

		boolean isJapaneseReviewed = args.getBoolean("isJapaneseReviewed");

		boolean kanjiPst = false;
		if (StringUtils.isNoneBlank(mItem.kanji)) {
			kanjiPst = true;
			if (isJapaneseReviewed) {
				reviewedView.setText(mItem.kanji);
			} else {
				test = mItem.kanji;
				mTestSwitcher.setText(getActivity().getString(R.string.review_switch_kanji));
			}
		}

		if (StringUtils.isNoneBlank(mItem.kana)) {
			if (!kanjiPst) {
				if (isJapaneseReviewed) {
					reviewedView.setText(mItem.kana);
				} else {
					test = mItem.kana;
					mTestSwitcher.setText(getActivity().getString(R.string.review_switch_kana));
				}
				mKanaSwitcher.setVisibility(View.GONE);
			} else {
				mKanaSwitcher.setText(getActivity().getString(R.string.review_switch_kana));
			}
		} else {
			mKanaSwitcher.setVisibility(View.GONE);
		}

		if (isJapaneseReviewed) {
			test = mItem.input;
			mTestSwitcher.setText(getActivity().getString(R.string.review_switch_input));
		} else {
			reviewedView.setText(mItem.input);
		}

		if (StringUtils.isNotBlank(mItem.details)) {
			infoView.setText(mItem.details);
			infoView.setVisibility(View.VISIBLE);
			infoTitleView.setVisibility(View.VISIBLE);
		} else {
			infoView.setText(null);
			infoView.setVisibility(View.GONE);
			infoTitleView.setVisibility(View.GONE);
		}

		if (StringUtils.isNotBlank(mItem.example)) {
			exampleView.setText(mItem.example);
			exampleView.setVisibility(View.VISIBLE);
			exampleTitleView.setVisibility(View.VISIBLE);
		} else {
			exampleView.setText(null);
			exampleView.setVisibility(View.GONE);
			exampleTitleView.setVisibility(View.GONE);
		}

		if (StringUtils.isNotBlank(mItem.tags)) {
			tagsView.setText(getString(R.string.details_tags, mItem.tags));
			tagsView.setVisibility(View.VISIBLE);
		} else {
			tagsView.setText(null);
			tagsView.setVisibility(View.GONE);
		}

		int total = mItem.success + mItem.failed;
		successView.setText(getString(R.string.details_ratio, (total == 0) ? 0 : ((mItem.success / total) * 100)));

		mBookmark = (ImageView) rootView.findViewById(R.id.bookmark);
		mBookmark.setOnClickListener(v -> bookmarkItem());
		handleBookmark();

		mRate0 = (ImageView) rootView.findViewById(R.id.rate_0);
		mRate0.setOnClickListener(v -> setRate(0));
		mRate1 = (ImageView) rootView.findViewById(R.id.rate_1);
		mRate1.setOnClickListener(v -> setRate(1));
		mRate2 = (ImageView) rootView.findViewById(R.id.rate_2);
		mRate2.setOnClickListener(v -> setRate(2));
		handleRate();
	}

	private void handleRate() {
		switch (mItem.learned) {
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

			case 0:
			default:
				mRate0.setImageResource(R.drawable.ic_star_black_24dp);
				mRate1.setImageResource(R.drawable.ic_star_border_black_24dp);
				mRate2.setImageResource(R.drawable.ic_star_border_black_24dp);

				break;
		}
	}

	private void handleBookmark() {
		mBookmark.setImageResource(mItem.bookmark ? R.drawable.ic_bookmark_on : R.drawable.ic_bookmark_off);
	}

	private void setRate(int rate) {
		mItem.learned = rate;
		handleRate();

		final ContentValues values = new ContentValues();
		values.put(DicoContract.LEARNED, mItem.learned);
		updateItem(values);

		int rateName;
		switch (rate) {
			case 1:
				rateName = R.string.rate_1;
				break;
			case 2:
				rateName = R.string.rate_2;
				break;
			case 0:
			default:
				rateName = R.string.rate_0;
				break;
		}

		Toast.makeText(getActivity(), getString(R.string.rate_done, getString(rateName)), Toast.LENGTH_SHORT).show();
	}

	private void bookmarkItem() {
		mItem.switchBookmark();
		handleBookmark();

		final ContentValues values = new ContentValues();
		values.put(DicoContract.BOOKMARK, mItem.bookmark);
		updateItem(values);

		Toast.makeText(getActivity(), getString(mItem.bookmark ? R.string.bookmark_add : R.string.bookmark_remove), Toast.LENGTH_SHORT).show();
	}

	private void updateItem(ContentValues values) {
		final String where = DicoContract._ID + "=?";
		final String[] selectionArgs = {mItem.id};

		getActivity().getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
	}

}
