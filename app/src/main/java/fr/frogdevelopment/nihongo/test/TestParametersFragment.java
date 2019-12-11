package fr.frogdevelopment.nihongo.test;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.dialog.TagsDialog;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class TestParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

    private static final int LOADER_ID = 700;

    static final String TEST_TYPE = "test_type";
    static final String TEST_SELECTED_QUANTITY = "test_count";
    static final String TEST_SELECTED_NB_ANSWER = "test_nbAnswer";
    static final String TEST_SELECTED_RATE = "test_rate";
    static final String TEST_DISPLAY_KANJI = "test_display_kanji";
    static final String TEST_TAGS = "test_tags";
    private static final String TEST_METHOD = "test_method";
    private static final String TEST_KEEP_CONFIG = "test_keep_config";

    private View mNbAnswers;
    private Spinner mNbAnswersSpinner;
    private Switch mKanjiSwitch;
    private TextView mTagSelection;
    private Button mStartButton;
    private Switch mSwitchKeepView;

    private int selectedType = -1;
    private int selectedMethod = -1;
    private int selectedQuantity = -1;
    private int selectedNbAnswers = 0; // default value
    private int selectedRate = -1;
    private List<CharSequence> items = new ArrayList<>();
    private ArrayList<Integer> mSelectedItems;
    private String[] mSelectedTags;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.test_parameters_fragment, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        Spinner mTypeSpinner = rootView.findViewById(R.id.test_param_type_spinner);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedType = position;
                checkStartButtonEnabled();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Spinner mMethodSpinner = rootView.findViewById(R.id.test_param_method_spinner);
        mMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedMethod = position;
                mNbAnswers.setVisibility(selectedMethod == 0 ? View.VISIBLE : View.GONE); // display when QCM selected
                mNbAnswersSpinner.setSelection(selectedNbAnswers);
                mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);

                checkStartButtonEnabled();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mNbAnswers = rootView.findViewById(R.id.test_param_nb_answers);
        mNbAnswersSpinner = rootView.findViewById(R.id.test_param_nb_answers_spinner);
        mNbAnswersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedNbAnswers = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Spinner mQuantitySpinner = rootView.findViewById(R.id.test_param_quantity_spinner);
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

        Spinner rateSpinner = rootView.findViewById(R.id.test_param_learned_spinner);
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

        Spinner mTagSpinner = rootView.findViewById(R.id.test_param_tag_spinner);
        mTagSpinner.setOnTouchListener(this::onClickTags);
        mKanjiSwitch = rootView.findViewById(R.id.test_param_kanji);
        mTagSelection = rootView.findViewById(R.id.test_param_tag_selection);
        mStartButton = rootView.findViewById(R.id.test_button_start);
        mStartButton.setOnClickListener(view -> onClickButtonStart());
        mSwitchKeepView = rootView.findViewById(R.id.test_switch_keep);

        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);

        if (savedInstanceState != null) {
            mKanjiSwitch.setChecked(savedInstanceState.getBoolean(TEST_DISPLAY_KANJI));
            selectedType = savedInstanceState.getInt(TEST_TYPE);
            mTypeSpinner.setSelection(selectedType);
            selectedQuantity = savedInstanceState.getInt(TEST_SELECTED_QUANTITY);
            mQuantitySpinner.setSelection(selectedQuantity);
            selectedMethod = savedInstanceState.getInt(TEST_METHOD);
            mMethodSpinner.setSelection(selectedMethod);
            selectedNbAnswers = savedInstanceState.getInt(TEST_SELECTED_NB_ANSWER);
            mNbAnswersSpinner.setSelection(selectedNbAnswers);
            selectedRate = savedInstanceState.getInt(TEST_SELECTED_RATE);
            rateSpinner.setSelection(selectedRate);
            mSelectedTags = savedInstanceState.getStringArray(TEST_TAGS);
            mTagSelection.setText(StringUtils.join(mSelectedTags, ", "));

            checkStartButtonEnabled();
        } else {
            PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
            if (preferencesHelper.getBoolean(TEST_KEEP_CONFIG)) {
                mSwitchKeepView.setChecked(true);
                mKanjiSwitch.setChecked(preferencesHelper.getBoolean(TEST_DISPLAY_KANJI));
                selectedType = preferencesHelper.getInt(TEST_TYPE);
                mTypeSpinner.setSelection(selectedType);
                selectedQuantity = preferencesHelper.getInt(TEST_SELECTED_QUANTITY);
                mQuantitySpinner.setSelection(selectedQuantity);
                selectedMethod = preferencesHelper.getInt(TEST_METHOD);
                mMethodSpinner.setSelection(selectedMethod);
                selectedNbAnswers = preferencesHelper.getInt(TEST_SELECTED_NB_ANSWER);
                mNbAnswersSpinner.setSelection(selectedNbAnswers);
                selectedRate = preferencesHelper.getInt(TEST_SELECTED_RATE);
                rateSpinner.setSelection(selectedRate);
                String test_tags = preferencesHelper.getString(TEST_TAGS);
                mSelectedTags = test_tags.split(", ");
                mTagSelection.setText(test_tags);

                checkStartButtonEnabled();
            }
        }
    }

    @Override
    public void onDestroyView() {
        saveConfigIfNeed();

        super.onDestroyView();
    }

    private void saveConfigIfNeed() {
        PreferencesHelper preferencesHelper = PreferencesHelper.getInstance(getActivity());
        if (mSwitchKeepView.isChecked()) {
            preferencesHelper.saveBoolean(TEST_KEEP_CONFIG, true);
            preferencesHelper.saveBoolean(TEST_DISPLAY_KANJI, mKanjiSwitch.isChecked());
            preferencesHelper.saveInt(TEST_TYPE, selectedType);
            preferencesHelper.saveInt(TEST_SELECTED_QUANTITY, selectedQuantity);
            preferencesHelper.saveInt(TEST_METHOD, selectedMethod);
            preferencesHelper.saveInt(TEST_SELECTED_NB_ANSWER, selectedNbAnswers);
            preferencesHelper.saveInt(TEST_SELECTED_RATE, selectedRate);
            preferencesHelper.saveString(TEST_TAGS, StringUtils.join(mSelectedTags, ", "));
        } else {
            preferencesHelper.saveBoolean(TEST_KEEP_CONFIG, false);
            preferencesHelper.remove(TEST_DISPLAY_KANJI);
            preferencesHelper.remove(TEST_TYPE);
            preferencesHelper.remove(TEST_SELECTED_QUANTITY);
            preferencesHelper.remove(TEST_METHOD);
            preferencesHelper.remove(TEST_SELECTED_NB_ANSWER);
            preferencesHelper.remove(TEST_SELECTED_RATE);
            preferencesHelper.remove(TEST_TAGS);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Uri uri = Uri.parse(NihonGoContentProvider.URI_WORD + "/TAGS");
//        return new CursorLoader(requireActivity(), uri, new String[]{"TAGS"}, null, null, null);
        return null;
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
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
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    private void checkStartButtonEnabled() {
        mStartButton.setEnabled(selectedType > -1 && selectedMethod > -1 && selectedQuantity > -1);
        if (selectedType > 1) {
            mKanjiSwitch.setVisibility(View.VISIBLE);
            mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);
        } else {
            mKanjiSwitch.setVisibility(View.INVISIBLE);
        }
    }

    private boolean onClickTags(View view, MotionEvent event) {
        view.performClick();
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
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void populateUiSelection(Bundle options) {
        options.putInt(TEST_TYPE, selectedType);
        options.putInt(TEST_SELECTED_QUANTITY, selectedQuantity);
        options.putInt(TEST_SELECTED_NB_ANSWER, selectedNbAnswers);
        options.putInt(TEST_SELECTED_RATE, selectedRate);
        options.putBoolean(TEST_DISPLAY_KANJI, mKanjiSwitch.isChecked());
        options.putStringArray(TEST_TAGS, mSelectedTags);
        options.putInt(TEST_METHOD, selectedMethod);

        saveConfigIfNeed();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        // Store UI state to the savedInstanceState.
        populateUiSelection(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

}
