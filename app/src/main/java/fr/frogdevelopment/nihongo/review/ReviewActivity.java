/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;

public class ReviewActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, ReviewFragment.OnFragmentInteractionListener {

	private static final int LOADER_ID = 710;
	private ReviewAdapter adapter;

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.review_viewpager)
	ViewPager viewPager;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_review);

		ButterKnife.bind(this);

		initToolbar();

		final boolean isJapaneseReviewed = getIntent().getExtras().getBoolean("isJapaneseReviewed");

		adapter = new ReviewAdapter(getSupportFragmentManager(), isJapaneseReviewed);
		viewPager.setAdapter(adapter);

		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				ReviewFragment fragment = adapter.getItemAt(position);
				if (position + 1 == adapter.getCount()) {
					if (fragment.mFabAgain.getVisibility() == View.GONE) {
						fragment.mFabAgain.show();
					}
				} else {
					if (fragment.mFabAgain.getVisibility() == View.VISIBLE) {
						fragment.mFabAgain.hide();
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		getLoaderManager().initLoader(LOADER_ID, getIntent().getExtras(), this);
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
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		final String count = args.getString("count");
		String limit = "";
		if (NumberUtils.isNumber(count)) {
			limit = " LIMIT " + Integer.parseInt(count);
		}
		final String[] tags = args.getStringArray("tags");

		String selection = "1 = 1";
		String[] likes = null;
		if (ArrayUtils.isNotEmpty(tags)) {
			for (String tag : tags) {
				likes = ArrayUtils.add(likes, DicoContract.TAGS + " LIKE '%" + tag + "%'");
			}
			selection += " AND (" + StringUtils.join(likes, " OR ") + ")";
		}

		final boolean excludeLearned = args.getBoolean("excludeLearned");
		if (excludeLearned) {
			selection += " AND LEARNED = '0'";
		}

		final boolean onlyFavorite = args.getBoolean("onlyFavorite");
		if (onlyFavorite) {
			selection += " AND FAVORITE = '1'";
		}

		String sortOrder;
		final boolean isRandom = args.getBoolean("isRandom");
		if (isRandom) {
			sortOrder = "RANDOM()" + limit;
		} else {
			sortOrder = "_id DESC" + limit;
		}
		return new CursorLoader(this, NihonGoContentProvider.URI_WORD, DicoContract.COLUMNS, selection, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.setData(data);
		data.close();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFavorite(Item item) {
		final ContentValues values = new ContentValues();
		values.put(DicoContract.FAVORITE, item.favorite);

		updateItem(item, values);
	}

	@Override
	public void setLearned(Item item) {
		final ContentValues values = new ContentValues();
		values.put(DicoContract.LEARNED, item.learned);

		updateItem(item, values);
	}

	private void updateItem(Item item, ContentValues values) {
		final String where = DicoContract._ID + "=?";
		final String[] selectionArgs = {item.id};

		getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
	}

	@Override
	public void reviewAgain() {
		viewPager.setAdapter(adapter);
		getLoaderManager().restartLoader(LOADER_ID, getIntent().getExtras(), this);
	}

}
