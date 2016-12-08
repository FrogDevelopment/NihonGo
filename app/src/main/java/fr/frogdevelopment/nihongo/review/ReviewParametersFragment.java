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
import android.widget.AdapterView;
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

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.dialog.TagsDialog;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class ReviewParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

    private static final int LOADER_ID = 700;
    public static final String REVIEW_IS_JAPANESE = "review_isJapanese";
    public static final String REVIEW_EXCLUDE_LEARNED = "review_excludeLearned";
    public static final String REVIEW_ONLY_FAVORITE = "onlyFavorite";
    public static final String REVIEW_SELECTED_SORT = "selected_sort";
    public static final String REVIEW_SELECTED_QUANTITY = "selected_quantity";
    public static final String REVIEW_TAGS = "review_tags";
    public static final String REVIEW_KEEP_CONFIG = "review_keepConfig";

    private Switch mSwitchLanguageView;
    private Switch mSwitchLearned;
    private Switch mSwitchFavorite;
    private TextView mTagSelection;
    private Button mStartButton;
    private Switch mSwitchKeepView;

    private int selectedSort = -1;
    private int selectedQuantity = -1;
    private ArrayList<Integer> mSelectedItems;
    private String[] mSelectedTags;
    private List<CharSequence> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review_parameters, container, false);

        mSwitchLanguageView = (Switch) rootView.findViewById(R.id.review_switch_language);
        mSwitchLearned = (Switch) rootView.findViewById(R.id.review_switch_learned);
        mSwitchFavorite = (Switch) rootView.findViewById(R.id.review_switch_favorite);
        Spinner mSortSpinner = (Spinner) rootView.findViewById(R.id.review_param_sort_spinner);
        mSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedSort = position;
                checkStartButtonEnabled();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Spinner mQuantitySpinner = (Spinner) rootView.findViewById(R.id.review_param_quantity_spinner);
        mQuantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedQuantity = position;
                checkStartButtonEnabled();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Spinner mTagSpinner = (Spinner) rootView.findViewById(R.id.review_param_tag_spinner);
        mTagSpinner.setOnTouchListener((view, motionEvent) -> onClickTags(motionEvent));
        mTagSelection = (TextView) rootView.findViewById(R.id.review_param_tag_selection);
        mStartButton = (Button) rootView.findViewById(R.id.review_button_start);
        mStartButton.setOnClickListener(view -> onClickButtonStart());
        mSwitchKeepView = (Switch) rootView.findViewById(R.id.review_switch_keep);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (savedInstanceState != null) {
            mSwitchLanguageView.setChecked(savedInstanceState.getBoolean(REVIEW_IS_JAPANESE));
            mSwitchLearned.setChecked(savedInstanceState.getBoolean(REVIEW_EXCLUDE_LEARNED));
            mSwitchFavorite.setChecked(savedInstanceState.getBoolean(REVIEW_ONLY_FAVORITE));
            selectedSort = savedInstanceState.getInt(REVIEW_SELECTED_SORT);
            mSortSpinner.setSelection(selectedSort);
            selectedQuantity = savedInstanceState.getInt(REVIEW_SELECTED_QUANTITY);
            mQuantitySpinner.setSelection(selectedQuantity);
            mSelectedTags = savedInstanceState.getStringArray(REVIEW_TAGS);
            mTagSelection.setText(StringUtils.join(mSelectedTags, ", "));
        } else {
            PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
            if (preferencesHelper.getBoolean(REVIEW_KEEP_CONFIG)) {
                mSwitchKeepView.setChecked(true);
                mSwitchLanguageView.setChecked(preferencesHelper.getBoolean(REVIEW_IS_JAPANESE));
                mSwitchLearned.setChecked(preferencesHelper.getBoolean(REVIEW_EXCLUDE_LEARNED));
                mSwitchFavorite.setChecked(preferencesHelper.getBoolean(REVIEW_ONLY_FAVORITE));
                selectedSort = preferencesHelper.getInt(REVIEW_SELECTED_SORT);
                mSortSpinner.setSelection(selectedSort);
                selectedQuantity = preferencesHelper.getInt(REVIEW_SELECTED_QUANTITY);
                mQuantitySpinner.setSelection(selectedQuantity);
                String test_tags = preferencesHelper.getString(REVIEW_TAGS);
                mSelectedTags = test_tags.split(", ");
                mTagSelection.setText(test_tags);

            }
        }

        checkStartButtonEnabled();

        return rootView;
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
            preferencesHelper.saveBoolean(REVIEW_EXCLUDE_LEARNED, mSwitchLearned.isChecked());
            preferencesHelper.saveBoolean(REVIEW_ONLY_FAVORITE, mSwitchFavorite.isChecked());
            preferencesHelper.saveInt(REVIEW_SELECTED_SORT, selectedSort);
            preferencesHelper.saveInt(REVIEW_SELECTED_QUANTITY, selectedQuantity);
            preferencesHelper.saveString(REVIEW_TAGS, StringUtils.join(mSelectedTags, ", "));
        } else {
            preferencesHelper.saveBoolean(REVIEW_KEEP_CONFIG, false);
            preferencesHelper.remove(REVIEW_IS_JAPANESE);
            preferencesHelper.remove(REVIEW_EXCLUDE_LEARNED);
            preferencesHelper.remove(REVIEW_ONLY_FAVORITE);
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
        options.putBoolean(REVIEW_EXCLUDE_LEARNED, mSwitchLearned.isChecked());
        options.putBoolean(REVIEW_ONLY_FAVORITE, mSwitchFavorite.isChecked());
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