package fr.frogdevelopment.nihongo.dico.input;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.DETAILS;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.EXAMPLE;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.INPUT;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.KANA;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.KANJI;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.SORT_LETTER;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.TAGS;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract.TYPE;
import static fr.frogdevelopment.nihongo.contentprovider.DicoContract._ID;
import static org.apache.commons.lang3.StringUtils.capitalize;

public class InputActivity extends AppCompatActivity {

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
    private Item itemUpdate;
    private Type mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = (Type) getIntent().getSerializableExtra("type");
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

        switch (mType) {
            case WORD:
                setTitle(R.string.drawer_item_word);
                break;

            case EXPRESSION:
                setTitle(R.string.drawer_item_expression);
                break;

            default:
                setTitle("");
                break;
        }

        itemUpdate = getIntent().getParcelableExtra("item");

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.input, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_validate:
                validate();
                return true;

            case R.id.action_cancel:
                initData();
                return true;

            case android.R.id.home:
                back();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("position", getIntent().getIntExtra("position", -1));
        data.putExtra("item", itemUpdate);
        setResult(RESULT_OK, data);
        super.finish();
    }

    private void back() {
        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);
        super.finish();
    }

    private void initData() {
        mKanjiText.requestFocus();
        mKanjiText.setText(itemUpdate == null ? "" : itemUpdate.kanji);
        mKanjiWrapper.setError(null);

        mKanaText.setText(itemUpdate == null ? "" : itemUpdate.kana);
        mKanaWrapper.setError(null);

        mInputText.setText(itemUpdate == null ? "" : itemUpdate.input);
        mInputWrapper.setError(null);

        mDetailsText.setText(itemUpdate == null ? "" : itemUpdate.details);
        mDetailsWrapper.setError(null);

        mExampleText.setText(itemUpdate == null ? "" : itemUpdate.example);
        mExampleWrapper.setError(null);

        if (itemUpdate != null && itemUpdate.tags != null) {
            mTagsText.setText("");
            mTagsWrapper.setError(null);
            Stream.of(itemUpdate.tags.split(",")).forEach(this::addChipToGroup);
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

        String inputText = mInputText.getText().toString();
        if (inputText.isEmpty()) {
            isNoError = false;
            mInputWrapper.setError(getResources().getString(R.string.input_error_empty));
        } else if (InputUtils.containsJapanese(inputText)) {
            isNoError = false;
            mInputWrapper.setError(getResources().getString(R.string.input_error_input));
        } else {
            mInputWrapper.setError(null);
        }

        String kanjiText = mKanjiText.getText().toString();
        String kanaText = mKanaText.getText().toString();

        if (kanjiText.isEmpty() && kanaText.isEmpty()) {
            isNoError = false;
            mKanjiWrapper.setError(getResources().getString(R.string.input_error_all_empty));
            mKanaWrapper.setError(getResources().getString(R.string.input_error_all_empty));
        } else {
            if (InputUtils.isOnlyJapanese(kanjiText)) {
                mKanjiWrapper.setError(null);
            } else {
                isNoError = false;
                mKanjiWrapper.setError(getResources().getString(R.string.input_error_japanese));
            }

            if (InputUtils.isOnlyKana(kanaText)) {
                mKanaWrapper.setError(null);
            } else {
                isNoError = false;
                mKanaWrapper.setError(getResources().getString(R.string.input_error_kana));
            }
        }

        if (isNoError) {
            saveOrUpdate();
        } else {
            Toast.makeText(this, R.string.input_error_fields, Toast.LENGTH_LONG).show();
        }
    }

    private void saveOrUpdate() {
        if (itemUpdate != null) {
            update();
        } else {
            insert();
        }
    }

    private void update() {

        itemUpdate.input = capitalize(mInputText.getText().toString());
        itemUpdate.sort_letter = itemUpdate.input.substring(0, 1);
        itemUpdate.kanji = mKanjiText.getText().toString();
        itemUpdate.kana = mKanaText.getText().toString();
        itemUpdate.tags = StringUtils.join(mTags, ",");
        itemUpdate.details = mDetailsText.getText().toString();
        itemUpdate.example = mExampleText.getText().toString();

        final String where = _ID + "=?";
        final String[] selectionArgs = {String.valueOf(itemUpdate.id)};

        final ContentValues values = new ContentValues();
        values.put(INPUT, itemUpdate.input);
        values.put(SORT_LETTER, itemUpdate.sort_letter);
        values.put(KANJI, itemUpdate.kanji);
        values.put(KANA, itemUpdate.kana);
        values.put(TAGS, itemUpdate.tags);
        values.put(DETAILS, itemUpdate.details);
        values.put(EXAMPLE, itemUpdate.example);

        getContentResolver().update(mType.uri, values, where, selectionArgs);

        // TOAST
        Snackbar.make(findViewById(R.id.input_layout), R.string.input_update_OK, Snackbar.LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        finish();
                    }
                })
                .show();
    }

    private void insert() {
        final ContentValues values = new ContentValues();
        final String inputData = capitalize(mInputText.getText().toString());
        values.put(INPUT, inputData);
        values.put(SORT_LETTER, inputData.substring(0, 1));
        values.put(KANJI, mKanjiText.getText().toString());
        values.put(KANA, mKanaText.getText().toString());
        values.put(TAGS, StringUtils.join(mTags, ","));
        values.put(DETAILS, mDetailsText.getText().toString());
        values.put(EXAMPLE, mExampleText.getText().toString());
        values.put(TYPE, mType.code);

        getContentResolver().insert(mType.uri, values);

        // TOAST
        Snackbar.make(findViewById(R.id.input_layout), R.string.input_save_OK, Snackbar.LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        initData();
                    }
                })
                .show();
    }
}
