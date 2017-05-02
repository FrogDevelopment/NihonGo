/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.input;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;

public class InputActivity extends AppCompatActivity {

    private TextInputLayout mKanjiWrapper;
    private EditText mKanjiText;
    private TextInputLayout mKanaWrapper;
    private EditText mKanaText;
    private TextInputLayout mInputWrapper;
    private EditText mInputText;
    private TextInputLayout mTagsWrapper;
    private EditText mTagsText;
    private TextInputLayout mDetailsWrapper;
    private EditText mDetailsText;
    private TextInputLayout mExampleWrapper;
    private EditText mExampleText;

    // Initial Data
    private Item itemUpdate;
    private Type mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = (Type) getIntent().getSerializableExtra("type");
        setContentView(R.layout.activity_input);

        mKanjiWrapper = (TextInputLayout) findViewById(R.id.wrapper_kanji);
        mKanjiText = (EditText) findViewById(R.id.input_kanji);
        mKanaWrapper = (TextInputLayout) findViewById(R.id.wrapper_kana);
        mKanaText = (EditText) findViewById(R.id.input_kana);
        mInputWrapper = (TextInputLayout) findViewById(R.id.wrapper_input);
        mInputText = (EditText) findViewById(R.id.input_input);
        mTagsWrapper = (TextInputLayout) findViewById(R.id.wrapper_tags);
        mTagsText = (EditText) findViewById(R.id.input_tags);
        mDetailsWrapper = (TextInputLayout) findViewById(R.id.wrapper_details);
        mDetailsText = (EditText) findViewById(R.id.input_details);
        mExampleWrapper = (TextInputLayout) findViewById(R.id.wrapper_example);
        mExampleText = (EditText) findViewById(R.id.input_example);

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

        mTagsText.setText(itemUpdate == null ? "" : itemUpdate.tags);
        mTagsWrapper.setError(null);
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

        itemUpdate.input = StringUtils.capitalize(mInputText.getText().toString());
        itemUpdate.sort_letter = itemUpdate.input.substring(0, 1);
        itemUpdate.kanji = mKanjiText.getText().toString();
        itemUpdate.kana = mKanaText.getText().toString();
        itemUpdate.tags = mTagsText.getText().toString();
        itemUpdate.details = mDetailsText.getText().toString();
        itemUpdate.example = mExampleText.getText().toString();

        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {itemUpdate.id};

        final ContentValues values = new ContentValues();
        values.put(DicoContract.INPUT, itemUpdate.input);
        values.put(DicoContract.SORT_LETTER, itemUpdate.sort_letter);
        values.put(DicoContract.KANJI, itemUpdate.kanji);
        values.put(DicoContract.KANA, itemUpdate.kana);
        values.put(DicoContract.TAGS, itemUpdate.tags);
        values.put(DicoContract.DETAILS, itemUpdate.details);
        values.put(DicoContract.EXAMPLE, itemUpdate.example);

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
        final String inputData = StringUtils.capitalize(mInputText.getText().toString());
        values.put(DicoContract.INPUT, inputData);
        values.put(DicoContract.SORT_LETTER, inputData.substring(0, 1));
        values.put(DicoContract.KANJI, mKanjiText.getText().toString());
        values.put(DicoContract.KANA, mKanaText.getText().toString());
        values.put(DicoContract.TAGS, mTagsText.getText().toString());
        values.put(DicoContract.DETAILS, mDetailsText.getText().toString());
        values.put(DicoContract.EXAMPLE, mExampleText.getText().toString());
        values.put(DicoContract.TYPE, mType.code);

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
