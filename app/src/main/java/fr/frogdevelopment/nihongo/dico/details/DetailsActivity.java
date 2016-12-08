/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.List;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class DetailsActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener {

    public static final int RC_UPDATE_ITEM = 666;
    private List<Item> mItems;
    private Type mType;
    private DetailsAdapter mAdapter;
    private Item mCurrentItem;
    private FloatingActionButton mFabFavorite;
    private FloatingActionButton mFabLearned;

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

        setContentView(R.layout.activity_details);

        mFabFavorite = (FloatingActionButton) findViewById(R.id.fab_favorite);
        mFabFavorite.setOnClickListener(view -> setFavorite());

        mFabLearned = (FloatingActionButton) findViewById(R.id.fab_learned);
        mFabLearned.setOnClickListener(view -> setLearned());

        Bundle args = getIntent().getExtras();
        mType = (Type) args.getSerializable("type");
        mItems = args.getParcelableArrayList("items");

        mAdapter = new DetailsAdapter(getFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.details_viewpager);
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentItem = mItems.get(position);
                mFabFavorite.setImageResource(mCurrentItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);
                mFabLearned.setImageResource(mCurrentItem.isLearned() ? R.drawable.fab_bookmark_on : R.drawable.fab_bookmark_off);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        int position = args.getInt("position");
        viewPager.setCurrentItem(position);

        boolean doNotShow = PreferencesHelper.getInstance(this).getBoolean(Preferences.HELP_DETAILS);
        if (!doNotShow) {
            HelpDialog.show(getFragmentManager(), R.layout.dialog_help_details, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_UPDATE_ITEM && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            if (position > -1) {
                Item item = data.getParcelableExtra("item");
                mItems.set(position, item);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    // ************************************************************* \\
    @Override
    public void delete(final Item item) {
        // Ask the user if they want to delete
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning_black)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_detail)
                .setPositiveButton(R.string.positive_button_continue, (dialog, which) -> onDelete(item))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void onDelete(Item item) {
        Uri uri = Uri.parse(mType.uri + "/" + item.id);
        getContentResolver().delete(uri, null, null);
        Snackbar.make(findViewById(R.id.details_content), R.string.delete_done, Snackbar.LENGTH_LONG).show();
        back();
    }

    @Override
    public void update(int position, Item item) {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra("type", mType);
        intent.putExtra("position", position);
        intent.putExtra("item", item);

        startActivityForResult(intent, RC_UPDATE_ITEM);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setFavorite() {
        mCurrentItem.switchFavorite();
        mFabFavorite.setImageResource(mCurrentItem.isFavorite() ? R.drawable.fab_favorite_on : R.drawable.fab_favorite_off);

        final ContentValues values = new ContentValues();
        values.put(DicoContract.FAVORITE, mCurrentItem.favorite);
        updateItem(values);
    }

    public void setLearned() {
        mCurrentItem.switchLearned();
        mFabLearned.setImageResource(mCurrentItem.isLearned() ? R.drawable.fab_bookmark_on : R.drawable.fab_bookmark_off);

        final ContentValues values = new ContentValues();
        values.put(DicoContract.LEARNED, mCurrentItem.learned);
        updateItem(values);
    }

    private void updateItem(ContentValues values) {
        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {mCurrentItem.id};

        getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
    }

    // ************************************************************* \\
    private class DetailsAdapter extends FragmentStatePagerAdapter {

        private final int mCount;

        private DetailsAdapter(FragmentManager fm) {
            super(fm);
            mCount = mItems.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new DetailsFragment();
            Bundle args = new Bundle();
            args.putSerializable("type", mType);
            args.putInt("position", position);
            args.putParcelable("item", mItems.get(position));

            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "ITEM" + (position + 1);
        }

        public int getItemPosition(Object object) {
            // Causes adapter to reload all Fragments when // notifyDataSetChanged is called
            return POSITION_NONE;
        }
    }

}
