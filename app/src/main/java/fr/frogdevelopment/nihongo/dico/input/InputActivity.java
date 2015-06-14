/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.input;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.Type;

public class InputActivity extends Activity {

    // Components
    @InjectView(R.id.input_kanji)
    EditText mKanjiView;
    @InjectView(R.id.input_kana)
    EditText mKanaView;
    @InjectView(R.id.input_input)
    EditText mInputView;
    @InjectView(R.id.input_tags)
    EditText mTagsView;
    @InjectView(R.id.input_details)
    EditText mDetailsView;

    // Initial Data
    protected String idUpdate;
    private String kanjiSave;
    private String kanaSave;
    protected String inputSave;
    private String tagsSave;
    private String detailsSave;

    protected boolean isUpdate;

    private Type mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = (Type) getIntent().getSerializableExtra("type");
        setContentView(R.layout.fragment_input);

        switch (mType) {
            case WORD:
                setTitle(R.string.menu_subitem_word);
                break;

            case EXPRESSION:
                setTitle(R.string.menu_subitem_expression);
                break;

            default:
                setTitle("");
                break;
        }

        ButterKnife.inject(this);

        chekUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
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
                reset();
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


    private void back() {
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    private void chekUpdate() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(DicoContract._ID)) {
            fillDataToUpdate();
        } else {
            emptyData();
        }
    }

    private void fillDataToUpdate() {
        Bundle bundle = getIntent().getExtras();

        isUpdate = true;
        idUpdate = bundle.getString(DicoContract._ID);

        kanjiSave = bundle.getString(DicoContract.KANJI);
        mKanjiView.setText(kanjiSave);

        kanaSave = bundle.getString(DicoContract.KANA);
        mKanaView.setText(kanaSave);

        inputSave = bundle.getString(DicoContract.INPUT);
        mInputView.setText(inputSave);

        detailsSave = bundle.getString(DicoContract.DETAILS);
        mDetailsView.setText(detailsSave);

        tagsSave = bundle.getString(DicoContract.TAGS);
        mTagsView.setText(tagsSave);
    }


    private void emptyData() {
        isUpdate = false;
        idUpdate = "";
        kanjiSave = "";
        kanaSave = "";
        inputSave = "";
        detailsSave = "";
        tagsSave = "";
    }

    private void reset() {
        mKanjiView.setText(kanjiSave);
        mKanjiView.setError(null);

        mKanaView.setText(kanaSave);
        mKanaView.setError(null);

        mInputView.setText(inputSave);
        mInputView.setError(null);

        mDetailsView.setText(detailsSave);
        mDetailsView.setError(null);

        mTagsView.setText(tagsSave);
        mTagsView.setError(null);
    }

    private void validate() {
        boolean isNoError = true;

        String inputText = mInputView.getText().toString();
        if (inputText.isEmpty()) {
            isNoError = false;
            mInputView.setError(getResources().getString(R.string.input_error_empty));
        } else if (InputUtils.containsJapanese(inputText)) {
            isNoError = false;
            mInputView.setError(getResources().getString(R.string.input_error_input));
        } else {
            mInputView.setError(null);
        }

        String kanjiText = mKanjiView.getText().toString();
        String kanaText = mKanaView.getText().toString();

        if (kanjiText.isEmpty() && kanaText.isEmpty()) {
            isNoError = false;
            mKanjiView.setError(getResources().getString(R.string.input_error_all_empty));
            mKanaView.setError(getResources().getString(R.string.input_error_all_empty));
        } else {
            if (InputUtils.isOnlyJapanese(kanjiText)) {
                mKanjiView.setError(null);
            } else {
                isNoError = false;
                mKanjiView.setError(getResources().getString(R.string.input_error_japanese));
            }

            if (InputUtils.isOnlyKana(kanaText)) {
                mKanaView.setError(null);
            } else {
                isNoError = false;
                mKanaView.setError(getResources().getString(R.string.input_error_kana));
            }
        }

        if (isNoError) {
            saveOrUpdate();
        } else {
            Toast.makeText(this, R.string.input_error_fields, Toast.LENGTH_LONG).show();
        }
    }

    private void saveOrUpdate() {
        if (isUpdate) {
            updateById();
        } else {
            insert();
            reset();
        }

        back();
    }

    private void updateById() {
        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {idUpdate};

        final ContentValues values = new ContentValues();
        final String inputData = StringUtils.capitalize(mInputView.getText().toString());
        values.put(DicoContract.INPUT, inputData);
        values.put(DicoContract.SORT_LETTER, inputData.substring(0, 1));
        values.put(DicoContract.KANJI, mKanjiView.getText().toString());
        values.put(DicoContract.KANA, mKanaView.getText().toString());
        values.put(DicoContract.TAGS, mTagsView.getText().toString());
        values.put(DicoContract.DETAILS, mDetailsView.getText().toString());

        getContentResolver().update(mType.uri, values, where, selectionArgs);

        // TOAST
        Toast.makeText(this, R.string.input_update_OK, Toast.LENGTH_LONG).show();
    }

    private void insert() {
        final ContentValues values = new ContentValues();
        final String inputData = StringUtils.capitalize(mInputView.getText().toString());
        values.put(DicoContract.INPUT, inputData);
        values.put(DicoContract.SORT_LETTER, inputData.substring(0, 1));
        values.put(DicoContract.KANJI, mKanjiView.getText().toString());
        values.put(DicoContract.KANA, mKanaView.getText().toString());
        values.put(DicoContract.TAGS, mTagsView.getText().toString());
        values.put(DicoContract.DETAILS, mDetailsView.getText().toString());
        values.put(DicoContract.TYPE, mType.code);

        getContentResolver().insert(mType.uri, values);

        // TOAST
        Toast.makeText(this, R.string.input_save_OK, Toast.LENGTH_LONG).show();
    }
}
