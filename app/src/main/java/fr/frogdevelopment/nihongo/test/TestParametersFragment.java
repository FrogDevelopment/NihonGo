/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

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

public class TestParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

	private static final int LOADER_ID = 700;

	static final String TYPE_TEST     = "test_isJapanese";
	static final String QUANTITY      = "test_count";
	static final String NB_ANSWER     = "test_nbAnswer";
	static final String ONLY_LEARNED  = "test_onlyLearned";
	static final String DISPLAY_KANJI = "test_isDisplayKanji";

	@BindView(R.id.test_param_type_spinner)
	Spinner  mTypeSpinner;
	@BindView(R.id.test_param_method_spinner)
	Spinner  mMethodSpinner;
	@BindView(R.id.test_param_nb_answers)
	View     mNbAnswers;
	@BindView(R.id.test_param_nb_answers_spinner)
	Spinner  mNbAnswersSpinner;
	@BindView(R.id.test_param_quantity_spinner)
	Spinner  mQuantitySpinner;
	@BindView(R.id.test_switch_learned)
	Switch   mLearnedSwitch;
	@BindView(R.id.test_param_kanji)
	Switch   mKanjiSwitch;
	@BindView(R.id.test_param_tag_selection)
	TextView mTagSelection;

	@BindView(R.id.test_button_start)
	Button startButton;
	@BindView(R.id.test_switch_keep)
	Switch mSwitchKeepView;

	private int                selectedType      = -1;
	private int                selectedMethod    = -1;
	private int                selectedQuantity  = -1;
	private int                selectedNbAnswers = 0; // default value
	private List<CharSequence> items             = new ArrayList<>();
	private ArrayList<Integer> mSelectedItems;
	private String[]           mSelectedTags;
	private Unbinder           unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_test_parameters, container, false);

		unbinder = ButterKnife.bind(this, rootView);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		if (savedInstanceState != null) {
			mKanjiSwitch.setChecked(savedInstanceState.getBoolean("test_isJapanese"));
			mLearnedSwitch.setChecked(savedInstanceState.getBoolean("test_excludeLearned"));
			selectedType = savedInstanceState.getInt("test_type");
			mTypeSpinner.setSelection(selectedType);
			selectedQuantity = savedInstanceState.getInt("test_quantity");
			mQuantitySpinner.setSelection(selectedQuantity);
			selectedMethod = savedInstanceState.getInt("test_method");
			mMethodSpinner.setSelection(selectedMethod);
			selectedNbAnswers = savedInstanceState.getInt("test_nbAnswers");
			mNbAnswersSpinner.setSelection(selectedNbAnswers);
			mSelectedTags = savedInstanceState.getStringArray("test_tags");
			mTagSelection.setText(StringUtils.join(mSelectedTags, ", "));

			checkStartButtonEnabled();
		} else {
			PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
			if (preferencesHelper.getBoolean("test_keepConfig")) {
				mSwitchKeepView.setChecked(true);
				mKanjiSwitch.setChecked(preferencesHelper.getBoolean("test_isJapanese"));
				mLearnedSwitch.setChecked(preferencesHelper.getBoolean("test_excludeLearned"));
				selectedType = preferencesHelper.getInt("test_type");
				mTypeSpinner.setSelection(selectedType);
				selectedQuantity = preferencesHelper.getInt("test_quantity");
				mQuantitySpinner.setSelection(selectedQuantity);
				selectedMethod = preferencesHelper.getInt("test_method");
				mMethodSpinner.setSelection(selectedMethod);
				selectedNbAnswers = preferencesHelper.getInt("test_nbAnswers");
				mNbAnswersSpinner.setSelection(selectedNbAnswers);
				String test_tags = preferencesHelper.getString("test_tags");
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

		super.onDestroyView();
		unbinder.unbind();
	}

	private void saveConfigIfNeed() {
		PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
		if (mSwitchKeepView.isChecked()) {
			preferencesHelper.saveBoolean("test_keepConfig", true);
			preferencesHelper.saveBoolean("test_isJapanese", mKanjiSwitch.isChecked());
			preferencesHelper.saveBoolean("test_excludeLearned", mLearnedSwitch.isChecked());
			preferencesHelper.saveInt("test_type", selectedType);
			preferencesHelper.saveInt("test_quantity", selectedQuantity);
			preferencesHelper.saveInt("test_method", selectedMethod);
			preferencesHelper.saveInt("test_nbAnswers", selectedNbAnswers);
			preferencesHelper.saveString("test_tags", StringUtils.join(mSelectedTags, ", "));
		} else {
			preferencesHelper.saveBoolean("test_keepConfig", false);
			preferencesHelper.remove("test_isJapanese");
			preferencesHelper.remove("test_excludeLearned");
			preferencesHelper.remove("test_type");
			preferencesHelper.remove("test_quantity");
			preferencesHelper.remove("test_method");
			preferencesHelper.remove("test_nbAnswers");
			preferencesHelper.remove("test_tags");
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

	private void checkStartButtonEnabled() {
		startButton.setEnabled(selectedType > -1 && selectedMethod > -1 && selectedQuantity > -1);
		if (selectedType > 1) {
			mKanjiSwitch.setVisibility(View.VISIBLE);
			mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);
		} else {
			mKanjiSwitch.setVisibility(View.INVISIBLE);
		}
	}

	@OnItemSelected(R.id.test_param_type_spinner)
	public void onSelectType(int position) {
		selectedType = position;
		checkStartButtonEnabled();
	}

	@OnItemSelected(R.id.test_param_method_spinner)
	public void onSelectMethod(int position) {
		selectedMethod = position;
		mNbAnswers.setVisibility(selectedMethod == 0 ? View.VISIBLE : View.GONE); // display when QCM selected
		mNbAnswersSpinner.setSelection(selectedNbAnswers);
		mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);

		checkStartButtonEnabled();
	}

	@OnItemSelected(R.id.test_param_nb_answers_spinner)
	void onSelectNbAnswers(int position) {
		selectedNbAnswers = position;
	}

	@OnItemSelected(R.id.test_param_quantity_spinner)
	void onSelectQuantity(int position) {
		selectedQuantity = position;
		checkStartButtonEnabled();
	}

	@OnTouch(R.id.test_param_tag_spinner)
	boolean onClickTags(MotionEvent event) {
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

	@OnClick(R.id.test_button_start)
	void onClickButtonStart() {
		Intent intent;
		switch (selectedMethod) {
			case 0:
				intent = new Intent(getActivity(), TestSelectActivity.class);
				break;

			case 1:
				intent = new Intent(getActivity(), TestInputActivity.class);
				selectedNbAnswers = -1; // when input selected, only 1 answer
				break;

			default:
				// fixme
				throw new IllegalStateException("fixme");
		}

		Bundle options = new Bundle();
		populateUiSelection(options);

		intent.putExtras(options);

		startActivity(intent);
		getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void populateUiSelection(Bundle options) {
		options.putInt(TYPE_TEST, selectedType);
		options.putInt(QUANTITY, selectedQuantity);
		options.putInt(NB_ANSWER, selectedNbAnswers);
		options.putBoolean(ONLY_LEARNED, mLearnedSwitch.isChecked());
		options.putBoolean(DISPLAY_KANJI, mKanjiSwitch.isChecked());
		options.putStringArray("test_tags", mSelectedTags);
		options.putInt("test_method", selectedMethod);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Store UI state to the savedInstanceState.
		populateUiSelection(savedInstanceState);

		saveConfigIfNeed();

		super.onSaveInstanceState(savedInstanceState);
	}

}
