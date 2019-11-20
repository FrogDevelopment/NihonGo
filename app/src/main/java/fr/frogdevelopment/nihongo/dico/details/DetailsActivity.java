package fr.frogdevelopment.nihongo.dico.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.viewpager.widget.ViewPager;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.model.Row;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;

public class DetailsActivity extends AppCompatActivity {

    public static final int RC_NEW_ITEM = 777;
    public static final int RC_UPDATE_ITEM = 666;
    private List<Row> mRows;
    private DicoContract.Type mType;
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
        mType = (DicoContract.Type) args.getSerializable("type");
//        mItems = args.getParcelableArrayList("items");

        mAdapter = new DetailsAdapter(getSupportFragmentManager(), mType);
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
                Row row = data.getParcelableExtra("item");
                switch (requestCode) {
                    case RC_NEW_ITEM:
                        mRows.add(position, row);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case RC_UPDATE_ITEM:
                        mRows.set(position, row);
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    // ************************************************************* \\
    private void delete() {
        // Ask the user if they want to delete
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_detail)
                .setPositiveButton(R.string.positive_button_continue, (dialog, which) -> {

                    // delete only on UI
                    final Row rowToDelete = mRows.get(mCurrentPosition);
                    mRows.remove(mCurrentPosition);
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
                                        mRows.add(mCurrentPosition, rowToDelete);
                                        mAdapter.notifyDataSetChanged();
                                    } else {
                                        // or delete on base if not canceled
//                                        getContentResolver().delete(Uri.parse(mType.uri + "/" + rowToDelete.id), null, null);
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
        intent.putExtra("item", mRows.get(mCurrentPosition));

        startActivityForResult(intent, RC_UPDATE_ITEM);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void duplicate() {
//        Row row = mRows.get(mCurrentPosition);
//
//        final ContentValues values = new ContentValues();
//        values.put(DicoContract.INPUT, row.input);
//        values.put(DicoContract.SORT_LETTER, row.sort_letter);
//        values.put(DicoContract.KANJI, row.kanji);
//        values.put(DicoContract.KANA, row.kana);
//        values.put(DicoContract.TAGS, row.tags);
//        values.put(DicoContract.DETAILS, row.details);
//        values.put(DicoContract.EXAMPLE, row.example);
//        values.put(DicoContract.TYPE, mType.code);
//
//        Uri insert = getContentResolver().insert(mType.uri, values);
//        if (insert != null) {
//            row.id = Math.toIntExact(ContentUris.parseId(insert));
//        }
//
//        mRows.add(mCurrentPosition, row);
//        mAdapter.notifyDataSetChanged();
//
//        // TOAST
//        Snackbar.make(findViewById(R.id.details_content), R.string.input_duplicated_OK, Snackbar.LENGTH_LONG).show();
    }

}
