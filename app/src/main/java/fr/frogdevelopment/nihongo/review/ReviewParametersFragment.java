package fr.frogdevelopment.nihongo.review;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

import static java.util.Arrays.asList;

public class ReviewParametersFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 700;

    static final String REVIEW_IS_JAPANESE = "review_is_japanese";
    static final String REVIEW_ONLY_FAVORITE = "review_only_favorite";
    static final String REVIEW_RATE = "review_selected_rate";
    static final String REVIEW_SORT = "review_selected_sort";
    static final String REVIEW_QUANTITY = "review_selected_quantity";
    static final String REVIEW_TAGS = "review_tags";
    private static final String REVIEW_KEEP_CONFIG = "review_keepConfig";

    private Switch mSwitchLanguageView;
    private Switch mSwitchFavorite;
    private Button mStartButton;
    private Switch mSwitchKeepView;

    private int mRate = -1;
    private int mSort = -1;
    private String mQuantity = null;
    private String[] mSelectedTags = new String[0];
    private List<CharSequence> mTags;
    private AutoCompleteTextView mTagsDropdown;
    private ChipGroup mChipGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_parameters, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        mSwitchLanguageView = rootView.findViewById(R.id.review_switch_language);
        mSwitchFavorite = rootView.findViewById(R.id.review_switch_favorite);

        AutoCompleteTextView sortDropdown = rootView.findViewById(R.id.review_param_sort_dropdown);
        sortDropdown.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.param_sorts)));
        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            mSort = position;
            checkStartButtonEnabled();
        });

        TextInputEditText quantityEditText = rootView.findViewById(R.id.review_param_quantity);
        quantityEditText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(rootView);
                return true;
            }
            return false;
        });
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0) {
                    mQuantity = null;
                } else {
                    mQuantity = s.toString();
                }
                checkStartButtonEnabled();
            }
        });

        AutoCompleteTextView rateDropdown = rootView.findViewById(R.id.review_param_learned_dropdown);
        rateDropdown.setAdapter(new RateAdapter(requireContext()));
        rateDropdown.setOnItemClickListener((parent, view, position, id) -> {
            mRate = position;
            checkStartButtonEnabled();
        });

        mTagsDropdown = rootView.findViewById(R.id.review_param_tags_dropdown);
        mTagsDropdown.setThreshold(2);
        mTagsDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String tag = (String) parent.getItemAtPosition(position);
            addChipToGroup(tag);
            mSelectedTags = ArrayUtils.add(mSelectedTags, tag);
            mTagsDropdown.setText(null);

            mTags.remove(tag);
            updateDropDownTags();

            hideKeyboard(rootView);
            mTagsDropdown.clearFocus();
        });

        mChipGroup = rootView.findViewById(R.id.group_tags);

        mStartButton = rootView.findViewById(R.id.review_button_start);
        mStartButton.setOnClickListener(view -> onClickButtonStart());
        mSwitchKeepView = rootView.findViewById(R.id.review_switch_keep);

        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);

        if (savedInstanceState != null) {
            mRate = savedInstanceState.getInt(REVIEW_RATE);
            mSort = savedInstanceState.getInt(REVIEW_SORT);
            mQuantity = savedInstanceState.getString(REVIEW_QUANTITY);
            mSelectedTags = savedInstanceState.getStringArray(REVIEW_TAGS);

            mSwitchLanguageView.setChecked(savedInstanceState.getBoolean(REVIEW_IS_JAPANESE));
            mSwitchFavorite.setChecked(savedInstanceState.getBoolean(REVIEW_ONLY_FAVORITE));
            sortDropdown.setText(sortDropdown.getAdapter().getItem(mSort).toString(), false);
            quantityEditText.setText(mQuantity);
            rateDropdown.setText(rateDropdown.getAdapter().getItem(mRate).toString(), false);
            Stream.of(mSelectedTags).forEach(this::addChipToGroup);
        } else {
            PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(requireActivity());
            if (preferencesHelper.getBoolean(REVIEW_KEEP_CONFIG)) {
                mRate = preferencesHelper.getInt(REVIEW_RATE);
                mSort = preferencesHelper.getInt(REVIEW_SORT);
                mQuantity = String.valueOf(preferencesHelper.getInt(REVIEW_QUANTITY));
                String test_tags = preferencesHelper.getString(REVIEW_TAGS);
                if (StringUtils.isNotBlank(test_tags)) {
                    mSelectedTags = test_tags.split(", ");
                }

                mSwitchLanguageView.setChecked(preferencesHelper.getBoolean(REVIEW_IS_JAPANESE));
                mSwitchFavorite.setChecked(preferencesHelper.getBoolean(REVIEW_ONLY_FAVORITE));
                sortDropdown.setText(sortDropdown.getAdapter().getItem(mSort).toString(), false);
                quantityEditText.setText(mQuantity);
                rateDropdown.setText(rateDropdown.getAdapter().getItem(mRate).toString(), false);
                Stream.of(mSelectedTags).forEach(this::addChipToGroup);
                mSwitchKeepView.setChecked(true);
            }
        }

        checkStartButtonEnabled();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addChipToGroup(String tag) {
        Chip chip = new Chip(requireContext());
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setTextColor(getResources().getColor(R.color.white, requireActivity().getTheme()));
        chip.setChipBackgroundColorResource(R.color.accent);
        chip.setCloseIconTintResource(R.color.white);

        // necessary to get single selection working
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> {
            mChipGroup.removeView(chip);
            mTags.add(tag);
            mSelectedTags = ArrayUtils.removeAllOccurences(mSelectedTags, tag);
            updateDropDownTags();
        });
        mChipGroup.addView(chip);
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
            preferencesHelper.saveInt(REVIEW_RATE, mRate);
            preferencesHelper.saveInt(REVIEW_SORT, mSort);
            preferencesHelper.saveInt(REVIEW_QUANTITY, Integer.parseInt(mQuantity));
            preferencesHelper.saveString(REVIEW_TAGS, StringUtils.join(mSelectedTags, ", "));
        } else {
            preferencesHelper.saveBoolean(REVIEW_KEEP_CONFIG, false);
            preferencesHelper.remove(REVIEW_IS_JAPANESE);
            preferencesHelper.remove(REVIEW_ONLY_FAVORITE);
            preferencesHelper.remove(REVIEW_RATE);
            preferencesHelper.remove(REVIEW_SORT);
            preferencesHelper.remove(REVIEW_QUANTITY);
            preferencesHelper.remove(REVIEW_TAGS);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(NihonGoContentProvider.URI_WORD + "/TAGS");
        return new CursorLoader(requireActivity(), uri, new String[]{DicoContract.TAGS}, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Set<CharSequence> uniqueItems = new HashSet<>();
        while (data.moveToNext()) {
            String row = data.getString(0);
            String[] tags = row.split(",");
            uniqueItems.addAll(asList(tags));
        }
        data.close();

        mTags = new ArrayList<>(uniqueItems);
        mTags.removeAll(asList(mSelectedTags));
        updateDropDownTags();
    }

    private void updateDropDownTags() {
        mTags.sort(Comparator.comparing(CharSequence::toString));
        mTagsDropdown.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, mTags));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    private void checkStartButtonEnabled() {
        mStartButton.setEnabled(mSort > -1 && mRate > -1 && StringUtils.isNotBlank(mQuantity));
    }

    private void onClickButtonStart() {
        Bundle options = new Bundle();
        populateUiSelection(options);

        Intent intent = new Intent(getActivity(), ReviewActivity.class);
        intent.putExtras(options);

        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void populateUiSelection(Bundle options) {
        options.putBoolean(REVIEW_IS_JAPANESE, mSwitchLanguageView.isChecked());
        options.putBoolean(REVIEW_ONLY_FAVORITE, mSwitchFavorite.isChecked());
        options.putInt(REVIEW_RATE, mRate);
        options.putInt(REVIEW_SORT, mSort);
        options.putString(REVIEW_QUANTITY, mQuantity);
        options.putStringArray(REVIEW_TAGS, mSelectedTags);

        saveConfigIfNeed();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        // Store UI state to the savedInstanceState.
        populateUiSelection(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

}