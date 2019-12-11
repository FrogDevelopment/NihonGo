package fr.frogdevelopment.nihongo.dico.update;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.dico.DetailsViewModel;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static fr.frogdevelopment.nihongo.R.string.input_error_all_empty;
import static fr.frogdevelopment.nihongo.R.string.input_error_empty;
import static fr.frogdevelopment.nihongo.R.string.input_error_fields;
import static fr.frogdevelopment.nihongo.R.string.input_error_input;
import static fr.frogdevelopment.nihongo.R.string.input_error_japanese;
import static fr.frogdevelopment.nihongo.R.string.input_error_kana;
import static fr.frogdevelopment.nihongo.R.string.input_error_kanji_field;
import static fr.frogdevelopment.nihongo.R.string.input_save_OK;
import static fr.frogdevelopment.nihongo.R.string.input_update_OK;
import static fr.frogdevelopment.nihongo.utils.InputUtils.containsJapanese;
import static fr.frogdevelopment.nihongo.utils.InputUtils.containsKanji;
import static fr.frogdevelopment.nihongo.utils.InputUtils.isOnlyJapanese;
import static fr.frogdevelopment.nihongo.utils.InputUtils.isOnlyKana;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.join;

public class UpdateFragment extends Fragment {

    private DetailsViewModel mDetailsViewModel;
    private Details mDetails;
    private Set<String> mTags = new HashSet<>();

    private TextInputLayout mKanjiWrapper;
    private TextInputEditText mKanjiText;
    private TextInputLayout mKanaWrapper;
    private TextInputEditText mKanaText;
    private TextInputLayout mInputWrapper;
    private TextInputEditText mInputText;
    private TextInputLayout mDetailsWrapper;
    private TextInputEditText mDetailsText;
    private TextInputLayout mExampleWrapper;
    private TextInputEditText mExampleText;
    private TextInputLayout mTagsWrapper;
    private ChipGroup mChipGroup;

    public static UpdateFragment newInstance(Bundle arguments) {
        UpdateFragment updateFragment = new UpdateFragment();
        updateFragment.setArguments(arguments);
        return updateFragment;
    }

    public UpdateFragment() {
        super(R.layout.update_fragment);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDetailsViewModel = new ViewModelProvider(requireActivity()).get(DetailsViewModel.class);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        mKanjiWrapper = rootView.findViewById(R.id.wrapper_kanji);
        mKanjiText = rootView.findViewById(R.id.input_kanji);
        mKanaWrapper = rootView.findViewById(R.id.wrapper_kana);
        mKanaText = rootView.findViewById(R.id.input_kana);
        mInputWrapper = rootView.findViewById(R.id.wrapper_input);
        mInputText = rootView.findViewById(R.id.input_input);
        mDetailsWrapper = rootView.findViewById(R.id.wrapper_details);
        mDetailsText = rootView.findViewById(R.id.input_details);
        mExampleWrapper = rootView.findViewById(R.id.wrapper_example);
        mExampleText = rootView.findViewById(R.id.input_example);
        mTagsWrapper = rootView.findViewById(R.id.wrapper_tags);
        mChipGroup = rootView.findViewById(R.id.input_tags_group);

        TextInputEditText tagsEditText = rootView.findViewById(R.id.input_tags);
        tagsEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == IME_ACTION_DONE) {
                Stream.of(v.getText().toString().split(","))
                        .map(String::trim)
                        .map(StringUtils::capitalize)
                        .filter(StringUtils::isNotBlank)
                        .forEach(this::addChipToGroup);
                v.setText("");
                v.clearFocus();
                return true;
            }
            return false;
        });

        // update from dico
        if (getArguments() != null && getArguments().containsKey("item_id")) {
            int itemId = getArguments().getInt("item_id");
            mDetailsViewModel.getById(itemId)
                    .observe(getViewLifecycleOwner(), value -> {
                        mDetails = value;
                        initData();
                    });
        } else {
            // update from details
            mDetails = mDetailsViewModel.details();
            initData();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.input, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_validate:
                validate();
                return true;

            case R.id.action_cancel:
                initData();
                return true;

//            case android.R.id.home:
//                onBackPressed();
////                back();
//                return true;

            default:
                return false;
        }
    }

    private void initData() {
        // when new data
        if (mDetails == null) {
            mDetails = new Details();
        }

        mKanjiText.requestFocus();
        mKanjiText.setText(mDetails.kanji);
        mKanjiWrapper.setError(null);

        mKanaText.setText(mDetails.kana);
        mKanaWrapper.setError(null);

        mInputText.setText(mDetails.input);
        mInputWrapper.setError(null);

        mDetailsText.setText(mDetails.details);
        mDetailsWrapper.setError(null);

        mExampleText.setText(mDetails.example);
        mExampleWrapper.setError(null);

        if (mDetails.tags != null) {
            mTagsWrapper.setError(null);
            Stream.of(mDetails.tags.split(",")).forEach(this::addChipToGroup);
        }
    }

    private void addChipToGroup(String tag) {
        mTags.add(capitalize(tag));

        Chip chip = new Chip(requireContext());
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
        chip.setChipBackgroundColorResource(R.color.accent);
        chip.setCloseIconTintResource(R.color.white);

        // necessary to get single selection working
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> {
            mChipGroup.removeView(chip);
            mTags.remove(tag);
        });
        mChipGroup.addView(chip);
    }

    private void validate() {
        boolean isNoError = true;

        String kanjiText = mKanjiText.getText().toString();
        String kanaText = mKanaText.getText().toString();
        if (kanjiText.isEmpty() && kanaText.isEmpty()) {
            isNoError = false;
            mKanjiWrapper.setError(getResources().getString(input_error_all_empty));
            mKanaWrapper.setError(getResources().getString(input_error_all_empty));
        } else {
            if (!kanjiText.isEmpty()) {
                if (isOnlyJapanese(kanjiText)) {
                    if (containsKanji(kanjiText)) {
                        mKanjiWrapper.setError(null);
                    } else {
                        isNoError = false;
                        mKanjiWrapper.setError(getResources().getString(input_error_kanji_field));
                    }
                } else {
                    isNoError = false;
                    mKanjiWrapper.setError(getResources().getString(input_error_japanese));
                }
            }

            if (isOnlyKana(kanaText)) {
                mKanaWrapper.setError(null);
            } else {
                isNoError = false;
                mKanaWrapper.setError(getResources().getString(input_error_kana));
            }
        }

        String inputText = mInputText.getText().toString();
        if (inputText.isEmpty()) {
            isNoError = false;
            mInputWrapper.setError(getResources().getString(input_error_empty));
        } else if (containsJapanese(inputText)) {
            isNoError = false;
            mInputWrapper.setError(getResources().getString(input_error_input));
        } else {
            mInputWrapper.setError(null);
        }

        if (isNoError) {
            saveOrUpdate();
        } else {
            Toast.makeText(requireContext(), input_error_fields, Toast.LENGTH_LONG).show();
        }
    }

    private void saveOrUpdate() {
        mDetails.input = capitalize(mInputText.getText().toString());
        mDetails.sortLetter = mDetails.input.substring(0, 1);
        mDetails.kanji = mKanjiText.getText().toString();
        mDetails.kana = mKanaText.getText().toString();
        mDetails.tags = mTags.isEmpty() ? null : join(mTags, ",");
        mDetails.details = mDetailsText.getText().toString();
        mDetails.example = mExampleText.getText().toString();

        hideKeyboard();
        if (mDetails.id != null) {
            update();
        } else {
            insert();
        }
    }

    private void update() {
        mDetailsViewModel.update(mDetails);

        // TOAST
        Snackbar.make(requireView(), input_update_OK, Snackbar.LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
//                        finish();
//                        getParentFragmentManager()
//                                .beginTransaction()
//                                .remove(this)
//                                .commit();
                    }
                })
                .show();
    }

    private void insert() {
        mDetailsViewModel.insert(mDetails);

        // TOAST
        Snackbar.make(requireView(), input_save_OK, Snackbar.LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        mDetails = new Details();
                        initData();
                    }
                })
                .show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        }
    }
}