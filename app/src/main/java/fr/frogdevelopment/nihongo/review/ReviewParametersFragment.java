/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTouch;
import butterknife.Unbinder;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.dialog.TagsDialog;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class ReviewParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

	private static final int LOADER_ID = 700;

	@BindView(R.id.review_switch_language)
	Switch   mSwitchLanguageView;
	@BindView(R.id.review_switch_learned)
	Switch   mSwitchLearned;
	@BindView(R.id.review_switch_favorite)
	Switch   mSwitchFavorite;
	@BindView(R.id.review_param_sort_spinner)
	Spinner  mSortSpinner;
	@BindView(R.id.review_param_quantity_spinner)
	Spinner  mQuantitySpinner;
	@BindView(R.id.review_param_tag_selection)
	TextView mTagSelection;
	@BindView(R.id.review_button_start)
	Button   startButton;

	@BindView(R.id.review_switch_keep)
	Switch mSwitchKeepView;

	private int selectedSort = -1;
	private int selectedQuantity = -1;
	private ArrayList<Integer> mSelectedItems;
	private String[]           mSelectedTags;
	private List<CharSequence> items;
	private Unbinder           unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_review_parameters, container, false);

		unbinder = ButterKnife.bind(this, rootView);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		if (savedInstanceState != null) {
			mSwitchLanguageView.setChecked(savedInstanceState.getBoolean("review_isJapanese"));
			mSwitchLearned.setChecked(savedInstanceState.getBoolean("review_excludeLearned"));
			mSwitchFavorite.setChecked(savedInstanceState.getBoolean("onlyFavorite"));
			selectedSort = savedInstanceState.getInt("review_sort");
			mSortSpinner.setSelection(selectedSort);
			selectedQuantity = savedInstanceState.getInt("review_count");
			mQuantitySpinner.setSelection(selectedQuantity);
			mSelectedTags = savedInstanceState.getStringArray("review_tags");
			mTagSelection.setText(StringUtils.join(mSelectedTags, ", "));

			checkStartButtonEnabled();
		} else {
			PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
			if (preferencesHelper.getBoolean("review_keepConfig")) {
				mSwitchKeepView.setChecked(true);
				mSwitchLanguageView.setChecked(preferencesHelper.getBoolean("review_isJapanese"));
				mSwitchLearned.setChecked(preferencesHelper.getBoolean("review_excludeLearned"));
				mSwitchFavorite.setChecked(preferencesHelper.getBoolean("review_onlyFavorite"));
				selectedSort = preferencesHelper.getInt("review_sort");
				mSortSpinner.setSelection(selectedSort);
				selectedQuantity = preferencesHelper.getInt("review_count");
				mQuantitySpinner.setSelection(selectedQuantity);
				String test_tags = preferencesHelper.getString("review_tags");
				mSelectedTags = test_tags.split(", ");
				mTagSelection.setText(test_tags);

				checkStartButtonEnabled();
			}
		}

		return rootView;
	}

	@Override
	public void onDestroyView() {
		saveConfigIfNeed();

		unbinder.unbind();
		super.onDestroyView();
	}

	private void saveConfigIfNeed() {
		PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
		if (mSwitchKeepView.isChecked()) {
			preferencesHelper.saveBoolean("review_keepConfig", true);
			preferencesHelper.saveBoolean("review_isJapanese", mSwitchLanguageView.isChecked());
			preferencesHelper.saveBoolean("review_excludeLearned", mSwitchLearned.isChecked());
			preferencesHelper.saveBoolean("review_onlyFavorite", mSwitchFavorite.isChecked());
			preferencesHelper.saveInt("review_sort", selectedSort);
			preferencesHelper.saveInt("review_count", selectedQuantity);
			preferencesHelper.saveString("review_tags", StringUtils.join(mSelectedTags, ", "));
		} else {
			preferencesHelper.saveBoolean("review_keepConfig", false);
			preferencesHelper.remove("review_isJapanese");
			preferencesHelper.remove("review_excludeLearned");
			preferencesHelper.remove("review_onlyFavorite");
			preferencesHelper.remove("review_sort");
			preferencesHelper.remove("review_count");
			preferencesHelper.remove("review_tags");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Uri.parse(NihonGoContentProvider.URI_WORD + "/TAGS");
		return new CursorLoader(getActivity(), uri, new String[]{DicoContract.TAGS}, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		NumberFormat percentInstance = NumberFormat.getPercentInstance();
		Set<CharSequence> uniqueItems = new HashSet<>();
		while (data.moveToNext()) {
			String row = data.getString(0);
			double count = data.getDouble(1);
			double sum = data.getDouble(2);
			String percent = " - " + String.valueOf(percentInstance.format(count / sum));
			String[] tags = row.split(",");
			for (String tag : Arrays.asList(tags)) {
				String text = tag + percent;
				int start = tag.length();
				int end = text.length();
				SpannableStringBuilder str = new SpannableStringBuilder(text);
				str.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				str.setSpan(new RelativeSizeSpan(0.7f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				uniqueItems.add(str);
			}
		}

		items = new ArrayList<>(uniqueItems);

		Collections.sort(items, (o1, o2) -> o1.toString().compareTo(o2.toString()));

		data.close();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@OnItemSelected(R.id.review_param_sort_spinner)
	public void onSortSelected(int position) {
		selectedSort = position;
		checkStartButtonEnabled();
	}

	private void checkStartButtonEnabled() {
		startButton.setEnabled(selectedSort > -1 && selectedQuantity > -1);
	}

	@OnItemSelected(R.id.review_param_quantity_spinner)
	public void onQuantitySelected(int position) {
		selectedQuantity = position;
		checkStartButtonEnabled();
	}

	@OnTouch(R.id.review_param_tag_spinner)
	public boolean onClickTags(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			TagsDialog.show(getFragmentManager(), this, items, mSelectedItems);
			return true;
		}

		return false;
	}

	@Override
	public void onReturnValue(ArrayList<Integer> selectedItems) {
		mSelectedItems = selectedItems;
		mSelectedTags = null;

		for (Integer selectedIndex : mSelectedItems) {
			CharSequence selectedTag = items.get(selectedIndex);
			mSelectedTags = ArrayUtils.add(mSelectedTags, selectedTag.toString().split(" - ")[0]);
		}

		mTagSelection.setText(StringUtils.join(mSelectedTags, ", "));
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
		options.putBoolean("review_isJapanese", mSwitchLanguageView.isChecked());
		options.putBoolean("review_excludeLearned", mSwitchLearned.isChecked());
		options.putBoolean("review_onlyFavorite", mSwitchFavorite.isChecked());
		options.putInt("review_sort", selectedSort);
		options.putInt("review_count", selectedQuantity);
		options.putStringArray("review_tags", mSelectedTags);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Store UI state to the savedInstanceState.
		populateUiSelection(savedInstanceState);

		saveConfigIfNeed();

		super.onSaveInstanceState(savedInstanceState);
	}

}