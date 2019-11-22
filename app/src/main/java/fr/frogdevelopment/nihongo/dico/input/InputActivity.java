package fr.frogdevelopment.nihongo.dico.input;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import static fr.frogdevelopment.nihongo.dico.input.InputUtils.containsJapanese;
import static fr.frogdevelopment.nihongo.dico.input.InputUtils.containsKanji;
import static fr.frogdevelopment.nihongo.dico.input.InputUtils.isOnlyJapanese;
import static fr.frogdevelopment.nihongo.dico.input.InputUtils.isOnlyKana;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.join;

public class InputActivity extends AppCompatActivity {

    public static final String ITEM_ID = "item_id";

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
    private TextInputEditText mTagsText;
    private ChipGroup mChipGroup;

    private Set<String> mTags = new HashSet<>();

    // Initial Data
    private InputViewModel mInputViewModel;
    private Details mDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInputViewModel = new ViewModelProvider(this).get(InputViewModel.class);

        setContentView(R.layout.activity_input);

        mKanjiWrapper = findViewById(R.id.wrapper_kanji);
        mKanjiText = findViewById(R.id.input_kanji);
        mKanaWrapper = findViewById(R.id.wrapper_kana);
        mKanaText = findViewById(R.id.input_kana);
        mInputWrapper = findViewById(R.id.wrapper_input);
        mInputText = findViewById(R.id.input_input);
        mDetailsWrapper = findViewById(R.id.wrapper_details);
        mDetailsText = findViewById(R.id.input_details);
        mExampleWrapper = findViewById(R.id.wrapper_example);
        mExampleText = findViewById(R.id.input_example);
        mTagsWrapper = findViewById(R.id.wrapper_tags);
        mTagsText = findViewById(R.id.input_tags);
        mTagsText.setOnEditorActionListener((v, actionId, event) -> {
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
        mChipGroup = findViewById(R.id.input_tags_group);

//        setTitle(drawer_item_entries);

        if (getIntent().hasExtra(ITEM_ID)) {
            mInputViewModel.getById(getIntent().getIntExtra(ITEM_ID, -1)).observe(this, item -> {
                mDetails = item;
                initData();
            });
        } else {
            mDetails = new Details();
            initData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.input, menu);

        return true;
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

            case android.R.id.home:
                onBackPressed();
//                back();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    public void onBackPressed() {
//        back();
//    }
//
//    @Override
//    public void finish() {
//        Intent data = new Intent();
//        data.putExtra("position", getIntent().getIntExtra("position", -1));
//        data.putExtra("item", mDetails);
//        setResult(RESULT_OK, data);
//        super.finish();
//    }
//
//    private void back() {
//        Intent data = new Intent();
//        setResult(RESULT_CANCELED, data);
//        super.finish();
//    }

    private void initData() {
        mKanjiText.requestFocus();
        mKanjiText.setText(mDetails == null ? "" : mDetails.kanji);
        mKanjiWrapper.setError(null);

        mKanaText.setText(mDetails == null ? "" : mDetails.kana);
        mKanaWrapper.setError(null);

        mInputText.setText(mDetails == null ? "" : mDetails.input);
        mInputWrapper.setError(null);

        mDetailsText.setText(mDetails == null ? "" : mDetails.details);
        mDetailsWrapper.setError(null);

        mExampleText.setText(mDetails == null ? "" : mDetails.example);
        mExampleWrapper.setError(null);

        if (mDetails != null && mDetails.tags != null) {
            mTagsText.setText("");
            mTagsWrapper.setError(null);
            Stream.of(mDetails.tags.split(",")).forEach(this::addChipToGroup);
        }
    }

    private void addChipToGroup(String tag) {
        mTags.add(capitalize(tag));

        Chip chip = new Chip(this);
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setTextColor(getResources().getColor(R.color.white, getTheme()));
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
            Toast.makeText(this, input_error_fields, Toast.LENGTH_LONG).show();
        }
    }

    private void saveOrUpdate() {
        mDetails.input = capitalize(mInputText.getText().toString());
        mDetails.sort_letter = mDetails.input.substring(0, 1);
        mDetails.kanji = mKanjiText.getText().toString();
        mDetails.kana = mKanaText.getText().toString();
        mDetails.tags = mTags.isEmpty() ? null : join(mTags, ",");
        mDetails.details = mDetailsText.getText().toString();
        mDetails.example = mExampleText.getText().toString();

        hideKeyboard();
        if (mDetails.id != null) {
            update();
        } else {
            mDetails.type = "w";
            insert();
        }
    }

    private void update() {
        mInputViewModel.update(mDetails);

        // TOAST
        Snackbar.make(findViewById(R.id.input_layout), input_update_OK, Snackbar.LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        finish();
                    }
                })
                .show();
    }

    private void insert() {
        mInputViewModel.insert(mDetails);

        // TOAST
        Snackbar.make(findViewById(R.id.input_layout), input_save_OK, Snackbar.LENGTH_SHORT)
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
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}
