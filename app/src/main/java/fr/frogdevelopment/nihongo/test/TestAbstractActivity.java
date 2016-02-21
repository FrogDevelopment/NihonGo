/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;

public abstract class TestAbstractActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

	protected static final int LOADER_ID_ITEMS_TO_FIND = 710;

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.test_count)
	TextView mCount;

	protected int     typeTest;
	protected boolean isDisplayKanji;
	protected int     quantityMax;
	protected int currentItemIndex = 0;
	protected String[] tags;
	protected int      nbAnswer;

	protected List<Item> itemsToFind = new ArrayList<>();
	protected ArrayList<Result> results;

	private final int      layout;

	protected TestAbstractActivity(int layout) {
		this.layout = layout;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_test);
		ViewStub stub = (ViewStub) findViewById(R.id.test_layout_stub);
		stub.setLayoutResource(layout);
		stub.inflate();

		ButterKnife.bind(this);

		Bundle bundle = getIntent().getExtras();

		quantityMax = bundle.getInt(TestParametersFragment.QUANTITY);
		typeTest = bundle.getInt(TestParametersFragment.TYPE_TEST);
		isDisplayKanji = bundle.getBoolean(TestParametersFragment.DISPLAY_KANJI);
		tags = bundle.getStringArray("tags");
		nbAnswer = bundle.getInt(TestParametersFragment.NB_ANSWER);

		getLoaderManager().initLoader(LOADER_ID_ITEMS_TO_FIND, bundle, this);

		initToolbar();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ButterKnife.unbind(this);
	}

	private void initToolbar() {
		setSupportActionBar(toolbar);
		final ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}
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

		String[] likes = null;
		if (ArrayUtils.isNotEmpty(tags)) {
			for (String tag : tags) {
				likes = ArrayUtils.add(likes, DicoContract.TAGS + " LIKE '%" + tag + "%'");
			}
			selection += "AND (" + StringUtils.join(likes, " OR ") + ")";
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
//		Snackbar
//				.make(stub.getRootView(), R.string.test_back_message, Snackbar.LENGTH_LONG)
//				.setAction(R.string.positive_button_continue, v -> finish())
//				.show();
		new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_warning_black)
				.setTitle(R.string.test_back_title)
				.setMessage(R.string.test_back_message)
				.setPositiveButton(R.string.positive_button_continue, (dialog, which) -> finishTest())
				.setNegativeButton(android.R.string.no, null)
				.show();
	}

	private int successCounter = 0;

	protected void validate(CharSequence testAnswer) {
		if (results.get(currentItemIndex).setAnswerGiven(testAnswer)) {
			successCounter++;
		}
		currentItemIndex++;

		if (currentItemIndex == quantityMax) {
			finishTest();
		} else {
			displayQuantity();
			next(itemsToFind.get(currentItemIndex));
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
