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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
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

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.dialog.TagsDialog;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class ReviewParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

	private static final int    LOADER_ID                = 700;
	static final         String REVIEW_IS_JAPANESE       = "review_is_japanese";
	static final         String REVIEW_ONLY_FAVORITE     = "review_only_favorite";
	static final         String REVIEW_SELECTED_RATE     = "review_selected_rate";
	static final         String REVIEW_SELECTED_SORT     = "review_selected_sort";
	static final         String REVIEW_SELECTED_QUANTITY = "review_selected_quantity";
	static final         String REVIEW_TAGS              = "review_tags";
	static final         String REVIEW_KEEP_CONFIG       = "review_keepConfig";

	private Switch   mSwitchLanguageView;
	private Switch   mSwitchFavorite;
	private TextView mTagSelection;
	private Button   mStartButton;
	private Switch   mSwitchKeepView;

	private int selectedRate     = -1;
	private int selectedSort     = -1;
	private int selectedQuantity = -1;
	private ArrayList<Integer> mSelectedItems;
	private String[]           mSelectedTags;
	private List<CharSequence> items;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_review_parameters, container, false);
	}

	@Override
	public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
		mSwitchLanguageView = rootView.findViewById(R.id.review_switch_language);
		mSwitchFavorite = rootView.findViewById(R.id.review_switch_favorite);
		Spinner rateSpinner = rootView.findViewById(R.id.review_param_learned_spinner);
		rateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
				selectedRate = position;
				checkStartButtonEnabled();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		Spinner sortSpinner = rootView.findViewById(R.id.review_param_sort_spinner);
		sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
				selectedSort = position;
				checkStartButtonEnabled();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		Spinner quantitySpinner = rootView.findViewById(R.id.review_param_quantity_spinner);
		quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
				selectedQuantity = position;
				checkStartButtonEnabled();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		Spinner mTagSpinner = rootView.findViewById(R.id.review_param_tag_spinner);
		mTagSpinner.setOnTouchListener((view, motionEvent) -> onClickTags(motionEvent));
		mTagSelection = rootView.findViewById(R.id.review_param_tag_selection);
		mStartButton = rootView.findViewById(R.id.review_button_start);
		mStartButton.setOnClickListener(view -> onClickButtonStart());
		mSwitchKeepView = rootView.findViewById(R.id.review_switch_keep);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		if (savedInstanceState != null) {
			mSwitchLanguageView.setChecked(savedInstanceState.getBoolean(REVIEW_IS_JAPANESE));
			mSwitchFavorite.setChecked(savedInstanceState.getBoolean(REVIEW_ONLY_FAVORITE));
			selectedRate = savedInstanceState.getInt(REVIEW_SELECTED_RATE);
			rateSpinner.setSelection(selectedRate);
			selectedSort = savedInstanceState.getInt(REVIEW_SELECTED_SORT);
			sortSpinner.setSelection(selectedSort);
			selectedQuantity = savedInstanceState.getInt(REVIEW_SELECTED_QUANTITY);
			quantitySpinner.setSelection(selectedQuantity);
			mSelectedTags = savedInstanceState.getStringArray(REVIEW_TAGS);
			mTagSelection.setText(StringUtils.join(mSelectedTags, ", "));
		} else {
			PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
			if (preferencesHelper.getBoolean(REVIEW_KEEP_CONFIG)) {
				mSwitchKeepView.setChecked(true);
				mSwitchLanguageView.setChecked(preferencesHelper.getBoolean(REVIEW_IS_JAPANESE));
				mSwitchFavorite.setChecked(preferencesHelper.getBoolean(REVIEW_ONLY_FAVORITE));
				selectedRate = preferencesHelper.getInt(REVIEW_SELECTED_RATE);
				rateSpinner.setSelection(selectedRate);
				selectedSort = preferencesHelper.getInt(REVIEW_SELECTED_SORT);
				sortSpinner.setSelection(selectedSort);
				selectedQuantity = preferencesHelper.getInt(REVIEW_SELECTED_QUANTITY);
				quantitySpinner.setSelection(selectedQuantity);
				String test_tags = preferencesHelper.getString(REVIEW_TAGS);
				mSelectedTags = test_tags.split(", ");
				mTagSelection.setText(test_tags);

			}
		}

		checkStartButtonEnabled();
	}

	@Override
	public void onDestroyView() {
		saveConfigIfNeed();

		super.onDestroyView();
	}

	private void saveConfigIfNeed() {
		PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
		if (mSwitchKeepView.isChecked()) {
			preferencesHelper.saveBoolean(REVIEW_KEEP_CONFIG, true);
			preferencesHelper.saveBoolean(REVIEW_IS_JAPANESE, mSwitchLanguageView.isChecked());
			preferencesHelper.saveBoolean(REVIEW_ONLY_FAVORITE, mSwitchFavorite.isChecked());
			preferencesHelper.saveInt(REVIEW_SELECTED_RATE, selectedRate);
			preferencesHelper.saveInt(REVIEW_SELECTED_SORT, selectedSort);
			preferencesHelper.saveInt(REVIEW_SELECTED_QUANTITY, selectedQuantity);
			preferencesHelper.saveString(REVIEW_TAGS, StringUtils.join(mSelectedTags, ", "));
		} else {
			preferencesHelper.saveBoolean(REVIEW_KEEP_CONFIG, false);
			preferencesHelper.remove(REVIEW_IS_JAPANESE);
			preferencesHelper.remove(REVIEW_ONLY_FAVORITE);
			preferencesHelper.remove(REVIEW_SELECTED_RATE);
			preferencesHelper.remove(REVIEW_SELECTED_SORT);
			preferencesHelper.remove(REVIEW_SELECTED_QUANTITY);
			preferencesHelper.remove(REVIEW_TAGS);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Uri.parse(NihonGoContentProvider.URI_WORD + "/TAGS");
		return new CursorLoader(getActivity(), uri, new String[]{DicoContract.TAGS}, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Set<CharSequence> uniqueItems = new HashSet<>();
		while (data.moveToNext()) {
			String row = data.getString(0);
			String[] tags = row.split(",");
			uniqueItems.addAll(Arrays.asList(tags));
		}

		items = new ArrayList<>(uniqueItems);

		Collections.sort(items, (o1, o2) -> o1.toString().compareTo(o2.toString()));

		data.close();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	private void checkStartButtonEnabled() {
		mStartButton.setEnabled(selectedSort > -1 && selectedQuantity > -1);
	}

	private boolean onClickTags(MotionEvent event) {
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

	private void onClickButtonStart() {
		Bundle options = new Bundle();
		populateUiSelection(options);

		Intent intent = new Intent(getActivity(), ReviewActivity.class);
		intent.putExtras(options);

		startActivity(intent);
		getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void populateUiSelection(Bundle options) {
		options.putBoolean(REVIEW_IS_JAPANESE, mSwitchLanguageView.isChecked());
		options.putBoolean(REVIEW_ONLY_FAVORITE, mSwitchFavorite.isChecked());
		options.putInt(REVIEW_SELECTED_RATE, selectedRate);
		options.putInt(REVIEW_SELECTED_SORT, selectedSort);
		options.putInt(REVIEW_SELECTED_QUANTITY, selectedQuantity);
		options.putStringArray(REVIEW_TAGS, mSelectedTags);

		saveConfigIfNeed();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Store UI state to the savedInstanceState.
		populateUiSelection(savedInstanceState);

		super.onSaveInstanceState(savedInstanceState);
	}

}