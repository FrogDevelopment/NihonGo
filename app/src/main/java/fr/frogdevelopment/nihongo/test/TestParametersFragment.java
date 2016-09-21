/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

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

public class TestParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

    private static final int LOADER_ID = 700;

    static final String TYPE_TEST = "test_isJapanese";
    static final String QUANTITY = "test_count";
    static final String NB_ANSWER = "test_nbAnswer";
    static final String ONLY_LEARNED = "test_onlyLearned";
    static final String DISPLAY_KANJI = "test_isDisplayKanji";

    @BindView(R.id.test_param_type_selection)
    TextView mTypeSelected;

    @BindView(R.id.test_param_method_selection)
    TextView mMethodSelected;

    @BindView(R.id.test_param_nb_answers)
    View mNbAnswers;
    @BindView(R.id.test_param_nb_answers_selection)
    TextView mNbAnswersSelected;

    @BindView(R.id.test_param_quantity_selection)
    TextView mQuantitySelected;

    @BindView(R.id.test_switch_learned)
    Switch mLearnedSwitch;

    @BindView(R.id.test_param_kanji)
    Switch mKanjiSwitch;

    @BindView(R.id.test_param_tag_selection)
    TextView mTagSelected;

    @BindView(R.id.test_button_start)
    Button startButton;

    @BindView(R.id.test_switch_keep)
    Switch mSwitchKeepView;

    private int selectedType = -1;
    private int selectedMethod = -1;
    private String selectedQuantity = null;
    private String nbAnswers = "2"; // default value
    private List<CharSequence> items = new ArrayList<>();
    private ArrayList<Integer> mSelectedItems;
    private String[] mSelectedTags;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_parameters, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        getLoaderManager().initLoader(LOADER_ID, null, this);


        if (savedInstanceState != null) {
            mKanjiSwitch.setChecked(savedInstanceState.getBoolean("test_isJapanese"));
            mLearnedSwitch.setChecked(savedInstanceState.getBoolean("test_excludeLearned"));
            selectedType = savedInstanceState.getInt("test_type");
            mTypeSelected.setText(getResources().getStringArray(R.array.param_types)[selectedType]);
            selectedQuantity = savedInstanceState.getString("test_quantity");
            mQuantitySelected.setText(selectedQuantity);
            selectedMethod = savedInstanceState.getInt("test_method");
            mMethodSelected.setText(getResources().getStringArray(R.array.param_methods)[selectedMethod]);
            nbAnswers = savedInstanceState.getString("test_nbAnswers");
            mNbAnswersSelected.setText(nbAnswers);
            mSelectedTags = savedInstanceState.getStringArray("test_tags");
            mTagSelected.setText(StringUtils.join(mSelectedTags, ", "));

            checkStartButtonEnabled();
        } else if (PreferencesHelper.getInstance(getActivity()).getBoolean("test_keepConfig")) {
            mSwitchKeepView.setChecked(true);
            mKanjiSwitch.setChecked(PreferencesHelper.getInstance(getActivity()).getBoolean("test_isJapanese"));
            mLearnedSwitch.setChecked(PreferencesHelper.getInstance(getActivity()).getBoolean("test_excludeLearned"));
            selectedType = PreferencesHelper.getInstance(getActivity()).getInt("test_type");
            mTypeSelected.setText(getResources().getStringArray(R.array.param_types)[selectedType]);
            selectedQuantity = PreferencesHelper.getInstance(getActivity()).getString("test_quantity");
            mQuantitySelected.setText(selectedQuantity);
            selectedMethod = PreferencesHelper.getInstance(getActivity()).getInt("test_method");
            mMethodSelected.setText(getResources().getStringArray(R.array.param_methods)[selectedMethod]);
            nbAnswers = PreferencesHelper.getInstance(getActivity()).getString("test_nbAnswers");
            mNbAnswersSelected.setText(nbAnswers);
            String test_tags = PreferencesHelper.getInstance(getActivity()).getString("test_tags");
            mSelectedTags = test_tags.split(", ");
            mTagSelected.setText(test_tags);

            checkStartButtonEnabled();
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
        if (mSwitchKeepView.isChecked()) {
            PreferencesHelper.getInstance(getActivity()).saveBoolean("test_keepConfig", true);
            PreferencesHelper.getInstance(getActivity()).saveBoolean("test_isJapanese", mKanjiSwitch.isChecked());
            PreferencesHelper.getInstance(getActivity()).saveBoolean("test_excludeLearned", mLearnedSwitch.isChecked());
            PreferencesHelper.getInstance(getActivity()).saveInt("test_type", selectedType);
            PreferencesHelper.getInstance(getActivity()).saveString("test_quantity", selectedQuantity);
            PreferencesHelper.getInstance(getActivity()).saveInt("test_method", selectedMethod);
            PreferencesHelper.getInstance(getActivity()).saveString("test_nbAnswers", nbAnswers);
            PreferencesHelper.getInstance(getActivity()).saveString("test_tags", StringUtils.join(mSelectedTags, ", "));
        } else {
            PreferencesHelper.getInstance(getActivity()).saveBoolean("test_keepConfig", false);
            PreferencesHelper.getInstance(getActivity()).remove("test_isJapanese");
            PreferencesHelper.getInstance(getActivity()).remove("test_excludeLearned");
            PreferencesHelper.getInstance(getActivity()).remove("test_type");
            PreferencesHelper.getInstance(getActivity()).remove("test_quantity");
            PreferencesHelper.getInstance(getActivity()).remove("test_method");
            PreferencesHelper.getInstance(getActivity()).remove("test_nbAnswers");
            PreferencesHelper.getInstance(getActivity()).remove("test_tags");
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
        startButton.setEnabled(selectedType > -1 && selectedMethod > -1 && selectedQuantity != null);
    }

    @OnClick(R.id.test_param_type)
    public void onClickType(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_type_selection)
                .setItems(R.array.param_types, (dialog, which) -> {
                    selectedType = which;
                    mTypeSelected.setText(getResources().getStringArray(R.array.param_types)[which]);

                    boolean displayKanji = selectedType > 1;
                    if (displayKanji) {
                        mKanjiSwitch.setVisibility(View.VISIBLE);
                        mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);
                    } else {
                        mKanjiSwitch.setVisibility(View.INVISIBLE);
                    }
                    checkStartButtonEnabled();
                })
                .create()
                .show();
    }

    @OnClick(R.id.test_param_method)
    public void onClickMethod(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_method_selection)
                .setItems(R.array.param_methods, (dialog, which) -> {
                    selectedMethod = which;
                    mMethodSelected.setText(getResources().getStringArray(R.array.param_methods)[which]);
                    mNbAnswers.setVisibility(selectedMethod == 0 ? View.VISIBLE : View.GONE); // display when QCM selected
                    mNbAnswersSelected.setText(nbAnswers);
                    if (selectedMethod == 1) { // when input selected, only 1 answer
                        nbAnswers = "1";
                    }
                    mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);

                    checkStartButtonEnabled();
                })
                .create()
                .show();
    }

    @OnClick(R.id.test_param_nb_answers)
    void onClickNbAnswers(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_quantity_selection)
                .setItems(R.array.param_quantities_answers, (dialog, which) -> {
                    nbAnswers = getResources().getStringArray(R.array.param_quantities_answers)[which];
                    mNbAnswersSelected.setText(nbAnswers);
                })
                .create()
                .show();
    }

    @OnClick(R.id.test_param_quantity)
    void onClickQuantity(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_quantity_selection)
                .setItems(R.array.param_quantities, (dialog, which) -> {
                    selectedQuantity = getResources().getStringArray(R.array.param_quantities)[which];
                    mQuantitySelected.setText(selectedQuantity);

                    checkStartButtonEnabled();
                })
                .create()
                .show();
    }

    @OnClick(R.id.test_param_tag)
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

    @OnClick(R.id.test_button_start)
    void onClickButtonStart() {
        Intent intent;
        switch (selectedMethod) {
            case 0:
                intent = new Intent(getActivity(), TestSelectActivity.class);
                break;

            case 1:
                intent = new Intent(getActivity(), TestInputActivity.class);
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
        options.putInt(QUANTITY, Integer.valueOf(selectedQuantity));
        options.putInt(NB_ANSWER, Integer.valueOf(nbAnswers));
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
