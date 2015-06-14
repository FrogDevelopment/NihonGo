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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnPageChange;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;

public class ReviewActivity extends FragmentActivity implements LoaderCallbacks<Cursor>, ReviewFragment.OnFragmentInteractionListener {

    private static final int LOADER_ID = 710;
    private ReviewAdapter adapter;

    @InjectView(R.id.review_viewpager)
    ViewPager viewPager;

    private int currentPage = 0;
    private int count;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentPage > 0) {
            getMenuInflater().inflate(R.menu.review, menu);

            MenuItem indexMenuItem = menu.findItem(R.id.menu_review_index);
            String title = currentPage + "/" + count;
            indexMenuItem.setTitle(title);

            return true;
        }

        return false;
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

        ButterKnife.inject(this);

        // Show the Up button in the action bar.
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        final boolean isJapaneseReviewed = getIntent().getExtras().getBoolean("isJapaneseReviewed");

        adapter = new ReviewAdapter(getSupportFragmentManager(), isJapaneseReviewed);
        viewPager.setAdapter(adapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Bundle options = getIntent().getExtras();


        final boolean isRandom = options.getBoolean("isRandom");
        final String count = options.getString("count");
        String limit = "";
        if (NumberUtils.isNumber(count)) {
            limit = " LIMIT " + Integer.parseInt(count);
        }
        final String[] tags = options.getStringArray("tags");

        String[] likes = null;
        if (ArrayUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                likes = ArrayUtils.add(likes, DicoContract.TAGS + " LIKE '%" + tag + "%'");
            }
        }
        String selection = StringUtils.join(likes, " OR ");

        String sortOrder;
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
        count = data.getCount();
        data.close();
        currentPage = 1;
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub
    }

    @Override
    public void favorite(Item item) {
        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {item.id};

        final ContentValues values = new ContentValues();
        values.put(DicoContract.FAVORITE, item.favorite);

        getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);

        invalidateOptionsMenu();
    }

    @OnPageChange(R.id.review_viewpager)
    void onPageSelected(int pageNum) {
        currentPage = pageNum + 1;
        supportInvalidateOptionsMenu();
    }

}
