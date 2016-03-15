/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.dialog.TagsDialog;

public class ReviewParametersFragment extends Fragment implements LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

	private static final int LOADER_ID = 700;

	@Bind(R.id.review_switch_language)
	Switch   mSwitchLanguageView;
	@Bind(R.id.review_switch_sort)
	Switch   mSwitchSortView;
	@Bind(R.id.review_switch_learned)
	Switch   mSwitchLearned;
	@Bind(R.id.review_switch_favorite)
	Switch   mSwitchFavorite;
	@Bind(R.id.review_param_quantity_selection)
	TextView mQuantitySelected;
	@Bind(R.id.review_param_tag_selection)
	TextView mTagSelected;
	@Bind(R.id.review_button_start)
	Button   startButton;

	private String[] quantities;
	private String selectedQuantity = null;
	private ArrayList<Integer> mSelectedItems;
	private String[]           mSelectedTags;
	private List<String>       items;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_review_parameters, container, false);

		ButterKnife.bind(this, rootView);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		quantities = getResources().getStringArray(R.array.param_quantities);
		quantities = ArrayUtils.add(quantities, getResources().getString(R.string.param_quantity_all));

		if (savedInstanceState != null) {
			mSwitchLanguageView.setChecked(savedInstanceState.getBoolean("isJapaneseReviewed"));
			mSwitchSortView.setChecked(savedInstanceState.getBoolean("isRandom"));
			savedInstanceState.getString("count");
			selectedQuantity = savedInstanceState.getString("count");
			mQuantitySelected.setText(selectedQuantity);
			mSelectedTags = savedInstanceState.getStringArray("tags");
			mTagSelected.setText(StringUtils.join(mSelectedTags, ", "));
			mSwitchLearned.setChecked(savedInstanceState.getBoolean("excludeLearned"));
			mSwitchFavorite.setChecked(savedInstanceState.getBoolean("onlyFavorite"));
		}

		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Uri.parse(NihonGoContentProvider.URI_WORD + "/TAGS");
		return new CursorLoader(getActivity(), uri, new String[]{DicoContract.TAGS}, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Set<String> uniqueItems = new HashSet<>();
		while (data.moveToNext()) {
			String row = data.getString(0);
			String[] tags = row.split(",");
			uniqueItems.addAll(Arrays.asList(tags));
		}

		items = new ArrayList<>(uniqueItems);

		Collections.sort(items);

		data.close();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@OnClick(R.id.review_param_quantity)
	void onClickQuantity(View v) {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.param_quantity_selection)
				.setItems(quantities, (dialog, which) -> {
					selectedQuantity = quantities[which];
					mQuantitySelected.setText(selectedQuantity);

					startButton.setEnabled(selectedQuantity != null);
				})
				.create()
				.show();
	}

	@OnClick(R.id.review_param_tag)
	void onClickTag() {
		TagsDialog.show(getFragmentManager(), this, items, mSelectedItems);
	}

	@Override
	public void onReturnValue(ArrayList<Integer> selectedItems) {
		mSelectedItems = selectedItems;
		mSelectedTags = null;

		for (Integer selectedIndex : mSelectedItems) {
			String selectedTag = items.get(selectedIndex);
			mSelectedTags = ArrayUtils.add(mSelectedTags, selectedTag);
		}

		mTagSelected.setText(StringUtils.join(mSelectedTags, ", "));
	}

	@OnClick(R.id.review_button_start)
	void onClickButtonStart() {
		Bundle options = new Bundle();
		populateUiSelection(options);

		Intent intent = new Intent(getActivity(), ReviewActivity.class);
		intent.putExtras(options);

		startActivity(intent);
		getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void populateUiSelection(Bundle options) {
		options.putBoolean("isJapaneseReviewed", mSwitchLanguageView.isChecked());
		options.putBoolean("isRandom", mSwitchSortView.isChecked());
		options.putString("count", selectedQuantity);
		options.putStringArray("tags", mSelectedTags);
		options.putBoolean("excludeLearned", mSwitchLearned.isChecked());
		options.putBoolean("onlyFavorite", mSwitchFavorite.isChecked());
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Store UI state to the savedInstanceState.
		populateUiSelection(savedInstanceState);

		super.onSaveInstanceState(savedInstanceState);
	}

}