/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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

public abstract class TestAbstractActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

	private static final int LOADER_ID = 710;

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.test_count)
	TextView mCount;

	protected int          typeTest;
	protected boolean      isDisplayKanji;
	protected int          quantityMax;
	protected int          quantity;
	protected String[]     tags;
	protected boolean      first;
	protected List<String> idsDone;
	protected int     limit;

	protected ArrayList<Result> results;

	protected String currentDetails;

	private final   int     layout;

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

		first = true;
		quantity = 0;
		Bundle bundle = getIntent().getExtras();

		quantityMax = bundle.getInt(TestParametersFragment.QUANTITY);
		idsDone = new ArrayList<>(quantityMax);
		results = new ArrayList<>(quantityMax);
		typeTest = bundle.getInt(TestParametersFragment.TYPE_TEST);
		isDisplayKanji = bundle.getBoolean(TestParametersFragment.DISPLAY_KANJI);
		tags = bundle.getStringArray("tags");
		limit = bundle.getInt(TestParametersFragment.NB_ANSWER);

		getLoaderManager().initLoader(LOADER_ID, bundle, this);

		initToolbar();

		displayQuantity();
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
	public boolean onCreateOptionsMenu(Menu menu) {
//		if (currentDetails != null) {
//			getMenuInflater().inflate(R.menu.test, menu);
//
//			MenuItem detailsMenuItem = menu.findItem(R.id.menu_test_detail);
//			detailsMenuItem.setVisible(StringUtils.isNoneBlank(currentDetails));
//
//			return true;
//		}

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case android.R.id.home:
				onBackPressed();
				return true;

//			case R.id.menu_test_pass:
//				validate("");
//				return true;

//			case R.id.menu_test_detail:
//				new AlertDialog.Builder(this)
//						.setTitle(R.string.input_textView_details)
//						.setMessage(currentDetails)
//						.create()
//						.show();
//
//				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle options) {
		String selection = "INPUT != '~'";

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

		String[] selectionArgs;
		if (first) {
			selectionArgs = null;
			first = false;
		} else {

			StringBuilder inList = new StringBuilder(quantity);
			selectionArgs = new String[quantity];
			int i = 0;
			for (String idDone : idsDone) {
				if (i > 0) {
					inList.append(",");
				}
				inList.append("?");

				selectionArgs[i] = idDone;
				i++;
			}

			selection += "AND _ID NOT IN (" + inList.toString() + ")";
		}

		String sortOrder = "RANDOM() LIMIT " + limit;

		return new CursorLoader(this, NihonGoContentProvider.URI_WORD, DicoContract.COLUMNS, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//		invalidateOptionsMenu();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void onBackPressed() {
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
		if (results.get(quantity).setAnswerGiven(testAnswer)) {
			successCounter++;
		}
		quantity++;

		if (quantity  == quantityMax) {
			finishTest();
		} else {
			displayQuantity();
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
	}

	private void displayQuantity() {
		String count = (quantity + 1) + "/" + quantityMax;
		mCount.setText(count);
	}

	protected void finishTest() {
		finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		Intent intent = new Intent(this, TestResultActivity.class);
		intent.putParcelableArrayListExtra("results", results);
		intent.putExtra("successCounter", successCounter);
		intent.putExtra("quantity", quantity);

		startActivity(intent);
	}

}
