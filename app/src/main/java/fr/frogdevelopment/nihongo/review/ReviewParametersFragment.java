package fr.frogdevelopment.nihongo.review;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    static final String REVIEW_SELECTED_RATE = "review_selected_rate";
    static final String REVIEW_SELECTED_SORT = "review_selected_sort";
    static final String REVIEW_SELECTED_QUANTITY = "review_selected_quantity";
    static final String REVIEW_TAGS = "review_tags";
    static final String REVIEW_KEEP_CONFIG = "review_keepConfig";

    private Switch mSwitchLanguageView;
    private Switch mSwitchFavorite;
    private Button mStartButton;
    private Switch mSwitchKeepView;

    private int selectedRate = -1;
    private int selectedSort = -1;
    private int selectedQuantity = -1;
    private ArrayList<Integer> mSelectedItems;
    private String[] mSelectedTags;
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
            selectedSort = position;
            checkStartButtonEnabled();
        });

        AutoCompleteTextView quantityDropdown = rootView.findViewById(R.id.review_param_quantity_dropdown);
        quantityDropdown.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.param_quantities)));
        quantityDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedQuantity = position;
            checkStartButtonEnabled();
        });

        AutoCompleteTextView rateDropdown = rootView.findViewById(R.id.review_param_learned_dropdown);
        rateDropdown.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.param_learned)));
        rateDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedRate = position;
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
        });

        mChipGroup = rootView.findViewById(R.id.group_tags);

        mStartButton = rootView.findViewById(R.id.review_button_start);
        mStartButton.setOnClickListener(view -> onClickButtonStart());
        mSwitchKeepView = rootView.findViewById(R.id.review_switch_keep);

        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);

        if (savedInstanceState != null) {
            selectedRate = savedInstanceState.getInt(REVIEW_SELECTED_RATE);
            selectedSort = savedInstanceState.getInt(REVIEW_SELECTED_SORT);
            selectedQuantity = savedInstanceState.getInt(REVIEW_SELECTED_QUANTITY);
            mSelectedTags = savedInstanceState.getStringArray(REVIEW_TAGS);

            mSwitchLanguageView.setChecked(savedInstanceState.getBoolean(REVIEW_IS_JAPANESE));
            mSwitchFavorite.setChecked(savedInstanceState.getBoolean(REVIEW_ONLY_FAVORITE));
            sortDropdown.setText(sortDropdown.getAdapter().getItem(selectedSort).toString(), false);
            quantityDropdown.setText(quantityDropdown.getAdapter().getItem(selectedQuantity).toString(), false);
            rateDropdown.setText(rateDropdown.getAdapter().getItem(selectedRate).toString(), false);
            Stream.of(mSelectedTags).forEach(this::addChipToGroup);
        } else {
            PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(requireActivity());
            if (preferencesHelper.getBoolean(REVIEW_KEEP_CONFIG)) {
                selectedRate = preferencesHelper.getInt(REVIEW_SELECTED_RATE);
                selectedSort = preferencesHelper.getInt(REVIEW_SELECTED_SORT);
                selectedQuantity = preferencesHelper.getInt(REVIEW_SELECTED_QUANTITY);
                String test_tags = preferencesHelper.getString(REVIEW_TAGS);
                if (StringUtils.isNotBlank(test_tags)) {
                    mSelectedTags = test_tags.split(", ");
                } else {
                    mSelectedTags = new String[0];
                }

                mSwitchLanguageView.setChecked(preferencesHelper.getBoolean(REVIEW_IS_JAPANESE));
                mSwitchFavorite.setChecked(preferencesHelper.getBoolean(REVIEW_ONLY_FAVORITE));
                sortDropdown.setText(sortDropdown.getAdapter().getItem(selectedSort).toString(), false);
                quantityDropdown.setText(quantityDropdown.getAdapter().getItem(selectedQuantity).toString(), false);
                rateDropdown.setText(rateDropdown.getAdapter().getItem(selectedRate).toString(), false);
                Stream.of(mSelectedTags).forEach(this::addChipToGroup);
                mSwitchKeepView.setChecked(true);
            }
        }

        checkStartButtonEnabled();
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
        mStartButton.setEnabled(selectedSort > -1 && selectedQuantity > -1);
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
        options.putInt(REVIEW_SELECTED_RATE, selectedRate);
        options.putInt(REVIEW_SELECTED_SORT, selectedSort);
        options.putInt(REVIEW_SELECTED_QUANTITY, selectedQuantity);
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