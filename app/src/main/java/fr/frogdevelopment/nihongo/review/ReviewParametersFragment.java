/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.app.AlertDialog;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import butterknife.Unbinder;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.dialog.TagsDialog;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class ReviewParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

    private static final int LOADER_ID = 700;

    @BindView(R.id.review_switch_language)
    Switch mSwitchLanguageView;
    @BindView(R.id.review_param_sort_selection)
    TextView mSortSelected;
    @BindView(R.id.review_switch_learned)
    Switch mSwitchLearned;
    @BindView(R.id.review_switch_favorite)
    Switch mSwitchFavorite;
    @BindView(R.id.review_param_quantity_selection)
    TextView mQuantitySelected;
    @BindView(R.id.review_param_tag_selection)
    TextView mTagSelected;
    @BindView(R.id.review_button_start)
    Button startButton;

    @BindView(R.id.review_switch_keep)
    Switch mSwitchKeepView;

    private int selectedSort = -1;
    private String[] quantities;
    private String selectedQuantity = null;
    private ArrayList<Integer> mSelectedItems;
    private String[] mSelectedTags;
    private List<CharSequence> items;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review_parameters, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        quantities = getResources().getStringArray(R.array.param_quantities);
        quantities = ArrayUtils.add(quantities, getResources().getString(R.string.param_quantity_all));

        if (savedInstanceState != null) {
            mSwitchLanguageView.setChecked(savedInstanceState.getBoolean("review_isJapanese"));
            mSwitchLearned.setChecked(savedInstanceState.getBoolean("review_excludeLearned"));
            mSwitchFavorite.setChecked(savedInstanceState.getBoolean("onlyFavorite"));
            selectedSort = savedInstanceState.getInt("review_sort");
            mSortSelected.setText(getResources().getStringArray(R.array.param_sorts)[selectedSort]);
            selectedQuantity = savedInstanceState.getString("review_count");
            mQuantitySelected.setText(selectedQuantity);
            mSelectedTags = savedInstanceState.getStringArray("review_tags");
            mTagSelected.setText(StringUtils.join(mSelectedTags, ", "));

            checkStartButtonEnabled();
        } else {
            if (PreferencesHelper.getInstance(getActivity()).getBoolean("review_keepConfig")) {
                mSwitchKeepView.setChecked(true);
                mSwitchLanguageView.setChecked(PreferencesHelper.getInstance(getActivity()).getBoolean("review_isJapanese"));
                mSwitchLearned.setChecked(PreferencesHelper.getInstance(getActivity()).getBoolean("review_excludeLearned"));
                mSwitchFavorite.setChecked(PreferencesHelper.getInstance(getActivity()).getBoolean("review_onlyFavorite"));
                selectedSort = PreferencesHelper.getInstance(getActivity()).getInt("review_sort");
                mSortSelected.setText(getResources().getStringArray(R.array.param_sorts)[selectedSort]);
                selectedQuantity = PreferencesHelper.getInstance(getActivity()).getString("review_count");
                mQuantitySelected.setText(selectedQuantity);
                String test_tags = PreferencesHelper.getInstance(getActivity()).getString("review_tags");
                mSelectedTags = test_tags.split(", ");
                mTagSelected.setText(test_tags);

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
        if (mSwitchKeepView.isChecked()) {
            PreferencesHelper.getInstance(getActivity()).saveBoolean("review_keepConfig", true);
            PreferencesHelper.getInstance(getActivity()).saveBoolean("review_isJapanese", mSwitchLanguageView.isChecked());
            PreferencesHelper.getInstance(getActivity()).saveBoolean("review_excludeLearned", mSwitchLearned.isChecked());
            PreferencesHelper.getInstance(getActivity()).saveBoolean("review_onlyFavorite", mSwitchFavorite.isChecked());
            PreferencesHelper.getInstance(getActivity()).saveInt("review_sort", selectedSort);
            PreferencesHelper.getInstance(getActivity()).saveString("review_count", selectedQuantity);
            PreferencesHelper.getInstance(getActivity()).saveString("review_tags", StringUtils.join(mSelectedTags, ", "));
        } else {
            PreferencesHelper.getInstance(getActivity()).saveBoolean("review_keepConfig", false);
            PreferencesHelper.getInstance(getActivity()).remove("review_isJapanese");
            PreferencesHelper.getInstance(getActivity()).remove("review_excludeLearned");
            PreferencesHelper.getInstance(getActivity()).remove("review_onlyFavorite");
            PreferencesHelper.getInstance(getActivity()).remove("review_sort");
            PreferencesHelper.getInstance(getActivity()).remove("review_count");
            PreferencesHelper.getInstance(getActivity()).remove("review_tags");
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

    @OnClick(R.id.review_param_sort)
    public void onClickType(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_type_selection)
                .setItems(R.array.param_sorts, (dialog, which) -> {
                    selectedSort = which;
                    mSortSelected.setText(getResources().getStringArray(R.array.param_sorts)[which]);

                    checkStartButtonEnabled();
                })
                .create()
                .show();
    }

    private void checkStartButtonEnabled() {
        startButton.setEnabled(selectedSort > -1 && selectedQuantity != null);
    }

    @OnClick(R.id.review_param_quantity)
    void onClickQuantity(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_sort_selection)
                .setItems(quantities, (dialog, which) -> {
                    selectedQuantity = quantities[which];
                    mQuantitySelected.setText(selectedQuantity);

                    checkStartButtonEnabled();
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
            CharSequence selectedTag = items.get(selectedIndex);
            mSelectedTags = ArrayUtils.add(mSelectedTags, selectedTag.toString().split(" - ")[0]);
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
        options.putBoolean("review_isJapanese", mSwitchLanguageView.isChecked());
        options.putBoolean("review_excludeLearned", mSwitchLearned.isChecked());
        options.putBoolean("review_onlyFavorite", mSwitchFavorite.isChecked());
        options.putInt("review_sort", selectedSort);
        options.putString("review_count", selectedQuantity);
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