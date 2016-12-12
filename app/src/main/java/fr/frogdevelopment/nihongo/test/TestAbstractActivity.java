/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;

public abstract class TestAbstractActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    protected static final int LOADER_ID_ITEMS_TO_FIND = 710;

    private TextView mCount;
    private TextView mInfoTitle;
    private TextView mInfo;

    protected int typeTest;
    protected boolean isDisplayKanji;
    protected int quantityMax;
    protected int currentItemIndex = 0;
    protected String[] tags;
    protected int nbAnswer;
    private boolean onlyLearned;

    protected List<Item> itemsToFind = new ArrayList<>();
    protected ArrayList<Result> results;

    private final int mLayout;
    private View mView;

    protected TestAbstractActivity(int mLayout) {
        this.mLayout = mLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCount = (TextView) findViewById(R.id.test_count);
        mInfoTitle = (TextView) findViewById(R.id.test_info_title);
        mInfo = (TextView) findViewById(R.id.test_info);

        setContentView(R.layout.activity_test);
        ViewStub stub = (ViewStub) findViewById(R.id.test_layout_stub);
        stub.setLayoutResource(mLayout);
        mView = stub.inflate();

        Bundle bundle = getIntent().getExtras();
        int tmp = bundle.getInt(TestParametersFragment.TEST_SELECTED_QUANTITY);
        String count = getResources().getStringArray(R.array.param_quantities)[tmp];
        if (NumberUtils.isNumber(count)) {
            quantityMax = Integer.parseInt(count);
        }
        typeTest = bundle.getInt(TestParametersFragment.TEST_TYPE);
        isDisplayKanji = bundle.getBoolean(TestParametersFragment.TEST_DISPLAY_KANJI);
        tags = bundle.getStringArray(TestParametersFragment.TEST_TAGS);
        tmp = bundle.getInt(TestParametersFragment.TEST_SELECTED_NB_ANSWER);
        if (tmp == -1) {
            nbAnswer = 1;
        } else {
            nbAnswer = Integer.parseInt(getResources().getStringArray(R.array.param_quantities_answers)[tmp]);
        }
        onlyLearned = bundle.getBoolean(TestParametersFragment.TEST_ONLY_LEARNED);

        getLoaderManager().initLoader(LOADER_ID_ITEMS_TO_FIND, bundle, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle options) {
        String selection = "INPUT != '~'"; // fixme

        switch (typeTest) {

            case 0: // Kanji -> Hiragana
            case 1: // Hiragana -> Kanji
                // katakana exclude
                selection += " AND KANJI IS NOT NULL AND KANJI != ''";
                break;

            case 2: // Japanese -> French
            case 3: // French -> Japanese
                break;
        }

        if (onlyLearned) {
            selection += " AND LEARNED = '1'";
        }

        String[] likes = null;
        if (ArrayUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                likes = ArrayUtils.add(likes, DicoContract.TAGS + " LIKE '%" + tag + "%'");
            }
            selection += " AND (" + StringUtils.join(likes, " OR ") + ")";
        }

        String sortOrder = "RANDOM() LIMIT " + quantityMax;

        return new CursorLoader(this, NihonGoContentProvider.URI_WORD, DicoContract.COLUMNS, selection, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        quantityMax = data.getCount();
        results = new ArrayList<>(quantityMax);

        displayQuantity();

        while (data.moveToNext()) {
            itemsToFind.add(new Item(data));
        }

        data.close();

        next(itemsToFind.get(currentItemIndex));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onBackPressed() {
        Snackbar
                .make(mView, R.string.test_back_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.positive_button_continue, v -> {
                    results.remove(currentItemIndex);
                    finishTest();
                })
                .show();
    }

    private int successCounter = 0;

    protected void validate(CharSequence testAnswer) {
        Item item = itemsToFind.get(currentItemIndex);
        Result result = results.get(currentItemIndex);

        final ContentValues values = new ContentValues();
        if (result.setAnswerGiven(testAnswer)) {
            successCounter++;
            values.put(DicoContract.LEARNED, true);
            values.put(DicoContract.SUCCESS, ++item.success);
        } else {
            values.put(DicoContract.LEARNED, false);
            values.put(DicoContract.FAILED, ++item.failed);
        }

        result.nbSuccess = item.success;
        result.nbFailed = item.failed;

        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {item.id};

        getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);

        currentItemIndex++;

        if (currentItemIndex == quantityMax) {
            finishTest();
        } else {
            displayQuantity();
            Item nextItem = itemsToFind.get(currentItemIndex);
            next(nextItem);

            if (TextUtils.isEmpty(nextItem.details)) {
                mInfoTitle.setVisibility(View.GONE);
                mInfo.setVisibility(View.GONE);
                mInfo.setText(null);
            } else {
                mInfoTitle.setVisibility(View.VISIBLE);
                mInfo.setVisibility(View.VISIBLE);
                mInfo.setText(nextItem.details);
            }
        }
    }

    protected void displayQuantity() {
        String count = (currentItemIndex + 1) + "/" + quantityMax;
        mCount.setText(count);
    }

    abstract protected void next(Item item);

    private void finishTest() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        Intent intent = new Intent(this, TestResultActivity.class);
        intent.putParcelableArrayListExtra("results", results);
        intent.putExtra("successCounter", successCounter);
        intent.putExtra("quantity", currentItemIndex);

        startActivity(intent);
    }

}
