package fr.frogdevelopment.nihongo.dico.details;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;

public class DetailsActivity extends AppCompatActivity {

    public static final int RC_NEW_ITEM = 777;
    public static final int RC_UPDATE_ITEM = 666;
    private List<Item> mItems;
    private Type mType;
    private DetailsAdapter mAdapter;
    private int mCurrentPosition;
    private ViewPager mViewPager;


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

        FloatingActionMenu fam = findViewById(R.id.fab_menu);

        ImageView swapLeft = findViewById(R.id.swap_left);
        ImageView swapRight = findViewById(R.id.swap_right);

        Bundle args = getIntent().getExtras();
        mType = (Type) args.getSerializable("type");
        mItems = args.getParcelableArrayList("items");

        mAdapter = new DetailsAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.details_viewpager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                swapLeft.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
                swapRight.setVisibility(position + 1 == mAdapter.getCount() ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                fam.close(true);
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    fam.hideMenu(true);
                } else {
                    fam.showMenu(true);
                }
            }
        });
        final int position = args.getInt("position");
        mViewPager.setCurrentItem(position);

        swapLeft.setOnClickListener(v -> mViewPager.setCurrentItem(--mCurrentPosition));
        swapRight.setOnClickListener(v -> mViewPager.setCurrentItem(++mCurrentPosition));

        FloatingActionButton fabNew = findViewById(R.id.fab_new);
        fabNew.setOnClickListener(v -> newItem());

        FloatingActionButton fabDuplicate = findViewById(R.id.fab_duplicate);
        fabDuplicate.setOnClickListener(v -> duplicate());

        FloatingActionButton fabDelete = findViewById(R.id.fab_delete);
        fabDelete.setOnClickListener(v -> delete());

        FloatingActionButton mFabEdit = findViewById(R.id.fab_edit);
        mFabEdit.setOnClickListener(v -> update());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            back();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            if (position > -1) {
                Item item = data.getParcelableExtra("item");
                switch (requestCode) {
                    case RC_NEW_ITEM:
                        mItems.add(position, item);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case RC_UPDATE_ITEM:
                        mItems.set(position, item);
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    // ************************************************************* \\
    private void delete() {
        // Ask the user if they want to delete
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_detail)
                .setPositiveButton(R.string.positive_button_continue, (dialog, which) -> {

                    // delete only on UI
                    final Item itemToDelete = mItems.get(mCurrentPosition);
                    mItems.remove(mCurrentPosition);
                    mAdapter.notifyDataSetChanged();

                    Snackbar.make(findViewById(R.id.details_content), R.string.delete_done, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_cancel, v -> {
                                // keep empty action to display action ...
                            })
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                        // if canceled, un-delete on UI
                                        mItems.add(mCurrentPosition, itemToDelete);
                                        mAdapter.notifyDataSetChanged();
                                    } else {
                                        // or delete on base if not canceled
                                        getContentResolver().delete(Uri.parse(mType.uri + "/" + itemToDelete.id), null, null);
                                    }
                                }
                            })
                            .show();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void newItem() {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra("type", mType);
        intent.putExtra("position", mCurrentPosition);

        startActivityForResult(intent, RC_NEW_ITEM);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void update() {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra("type", mType);
        intent.putExtra("position", mCurrentPosition);
        intent.putExtra("item", mItems.get(mCurrentPosition));

        startActivityForResult(intent, RC_UPDATE_ITEM);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void duplicate() {
        Item item = mItems.get(mCurrentPosition);

        final ContentValues values = new ContentValues();
        values.put(DicoContract.INPUT, item.input);
        values.put(DicoContract.SORT_LETTER, item.sort_letter);
        values.put(DicoContract.KANJI, item.kanji);
        values.put(DicoContract.KANA, item.kana);
        values.put(DicoContract.TAGS, item.tags);
        values.put(DicoContract.DETAILS, item.details);
        values.put(DicoContract.EXAMPLE, item.example);
        values.put(DicoContract.TYPE, mType.code);

        Uri insert = getContentResolver().insert(mType.uri, values);
        if (insert != null) {
            item.id = String.valueOf(ContentUris.parseId(insert));
        }

        mItems.add(mCurrentPosition, item);
        mAdapter.notifyDataSetChanged();

        // TOAST
        Snackbar.make(findViewById(R.id.details_content), R.string.input_duplicated_OK, Snackbar.LENGTH_LONG).show();
    }

    // ************************************************************* \\
    private class DetailsAdapter extends FragmentStatePagerAdapter {

        private final int mCount;

        private DetailsAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            mCount = mItems.size();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new DetailsFragment();
            Bundle args = new Bundle();
            args.putSerializable("type", mType);
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

        public int getItemPosition(@NonNull Object object) {
            // Causes adapter to reload all Fragments when // notifyDataSetChanged is called
            return POSITION_NONE;
        }
    }

}
