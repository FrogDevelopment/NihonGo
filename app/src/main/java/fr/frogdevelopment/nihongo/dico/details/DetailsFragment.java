/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;

public class DetailsFragment extends Fragment {

	private Item mItem;

	private ImageView mRate0;
	private ImageView mRate1;
	private ImageView mRate2;
	private ImageView mBookmark;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// The last two arguments ensure LayoutParams are inflated properly.
		View rootView = inflater.inflate(R.layout.fragment_details, container, false);

		mBookmark = (ImageView) rootView.findViewById(R.id.details_bookmark);
		mBookmark.setOnClickListener(v -> bookmarkItem());

		mRate0 = (ImageView) rootView.findViewById(R.id.rate_0);
		mRate0.setOnClickListener(v -> setRate(0));
		mRate1 = (ImageView) rootView.findViewById(R.id.rate_1);
		mRate1.setOnClickListener(v -> setRate(1));
		mRate2 = (ImageView) rootView.findViewById(R.id.rate_2);
		mRate2.setOnClickListener(v -> setRate(2));

		populateView(rootView);

		return rootView;
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

		handleBookmark();
		handleRate(mItem.learned);

		kanjiView.setOnLongClickListener(v -> {
			ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("kanji", kanjiView.getText());
			clipboard.setPrimaryClip(clip);

			Toast.makeText(getActivity(), R.string.copy_kanji, Toast.LENGTH_LONG).show();
			return true;
		});

		kanaView.setOnLongClickListener(v -> {
			ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("kana", kanaView.getText());
			clipboard.setPrimaryClip(clip);

			Toast.makeText(getActivity(), R.string.copy_kana, Toast.LENGTH_LONG).show();
			return true;
		});
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
		handleRate(rate);

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
