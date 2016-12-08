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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class ReviewActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 710;

    private ViewPager viewPager;
    private ReviewAdapter adapter;

    private Item mCurrentItem;
    private FloatingActionButton mFabAgain;
    private FloatingActionButton mFabLearned;
    private FloatingActionButton mFabFavorite;

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

        mFabAgain = (FloatingActionButton) findViewById(R.id.fab_again);
        mFabAgain.setOnClickListener(view -> reviewAgain());

        mFabFavorite = (FloatingActionButton) findViewById(R.id.fab_favorite);
        mFabFavorite.setOnClickListener(view -> onItemFavorite());

        mFabLearned = (FloatingActionButton) findViewById(R.id.fab_learned);
        mFabLearned.setOnClickListener(view -> onItemLearned());

        final boolean isJapaneseReviewed = getIntent().getExtras().getBoolean(ReviewParametersFragment.REVIEW_IS_JAPANESE);

        adapter = new ReviewAdapter(getFragmentManager(), isJapaneseReviewed);
        viewPager = (ViewPager) findViewById(R.id.review_viewpager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentItem = adapter.getItemAt(position);
                if (position + 1 == adapter.getCount()) {
                    if (mFabAgain.getVisibility() == View.GONE) {
                        mFabAgain.show();
                    }
                } else {
                    if (mFabAgain.getVisibility() == View.VISIBLE) {
                        mFabAgain.hide();
                    }
                }

                mFabFavorite.setImageResource(mCurrentItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);
                mFabLearned.setImageResource(mCurrentItem.isLearned() ? R.drawable.fab_bookmark_on : R.drawable.fab_bookmark_off);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        getLoaderManager().initLoader(LOADER_ID, getIntent().getExtras(), this);

        boolean doNotShow = PreferencesHelper.getInstance(getApplicationContext()).getBoolean(Preferences.HELP_REVIEW);
        if (!doNotShow) {
            HelpDialog.show(getFragmentManager(), R.layout.dialog_help_review, true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final int selectedQuantity = args.getInt(ReviewParametersFragment.REVIEW_SELECTED_QUANTITY);
        String count = getResources().getStringArray(R.array.param_quantities)[selectedQuantity];
        String limit = "";
        if (NumberUtils.isNumber(count)) {
            limit = " LIMIT " + Integer.parseInt(count);
        }
        final String[] tags = args.getStringArray(ReviewParametersFragment.REVIEW_TAGS);

        String selection = "1 = 1";
        String[] likes = null;
        if (ArrayUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                likes = ArrayUtils.add(likes, DicoContract.TAGS + " LIKE '%" + tag + "%'");
            }
            selection += " AND (" + StringUtils.join(likes, " OR ") + ")";
        }

        final boolean excludeLearned = args.getBoolean(ReviewParametersFragment.REVIEW_EXCLUDE_LEARNED);
        if (excludeLearned) {
            selection += " AND LEARNED = '0'";
        }

        final boolean onlyFavorite = args.getBoolean(ReviewParametersFragment.REVIEW_ONLY_FAVORITE);
        if (onlyFavorite) {
            selection += " AND FAVORITE = '1'";
        }

        String sortOrder;
        final int selectedSort = args.getInt(ReviewParametersFragment.REVIEW_SELECTED_SORT);

        switch (selectedSort) {
            case 0: // new -> old
                sortOrder = "_id ASC" + limit;
                break;
            case 1: // old -> new
                sortOrder = "_id DESC" + limit;
                break;
            case 2: // random
                sortOrder = "RANDOM()" + limit;
                break;
            default:
                sortOrder = "";
                break;
        }

        return new CursorLoader(this, NihonGoContentProvider.URI_WORD, DicoContract.COLUMNS, selection, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.setData(data);
        data.close();

        mCurrentItem = adapter.getItemAt(0);
        mFabFavorite.setImageResource(mCurrentItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);
        mFabLearned.setImageResource(mCurrentItem.isLearned() ? R.drawable.fab_bookmark_on : R.drawable.fab_bookmark_off);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub
    }

    private void onItemFavorite() {
        mCurrentItem.switchFavorite();
        final ContentValues values = new ContentValues();
        values.put(DicoContract.FAVORITE, mCurrentItem.favorite);

        updateItem(values);
        mFabFavorite.setImageResource(mCurrentItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);
    }

    private void onItemLearned() {
        mCurrentItem.switchLearned();
        final ContentValues values = new ContentValues();
        values.put(DicoContract.LEARNED, mCurrentItem.learned);

        updateItem(values);
        mFabLearned.setImageResource(mCurrentItem.isLearned() ? R.drawable.fab_bookmark_on : R.drawable.fab_bookmark_off);
    }

    private void updateItem(ContentValues values) {
        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {mCurrentItem.id};

        getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
    }

    private void reviewAgain() {
        mFabAgain.hide();
        adapter.clear();
        viewPager.setAdapter(adapter);
        getLoaderManager().restartLoader(LOADER_ID, getIntent().getExtras(), this);
    }

}
