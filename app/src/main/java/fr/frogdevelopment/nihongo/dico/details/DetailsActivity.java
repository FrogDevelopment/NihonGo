package fr.frogdevelopment.nihongo.dico.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class DetailsActivity extends AppCompatActivity {

    public static final int RC_NEW_ITEM = 777;
    public static final int RC_UPDATE_ITEM = 666;

    private DetailsViewModel mDetailsViewModel;

    private DetailsAdapter mAdapter;
    private int mCurrentPosition;
    private ViewPager2 mViewPager;
    private ImageView mSwapLeft;
    private ImageView mSwapRight;

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

        mDetailsViewModel = new ViewModelProvider(this).get(DetailsViewModel.class);

        setContentView(R.layout.activity_details);

        FloatingActionMenu fam = findViewById(R.id.fab_menu);

        mSwapLeft = findViewById(R.id.swap_left);
        mSwapRight = findViewById(R.id.swap_right);

        Bundle args = getIntent().getExtras();
        List<Integer> mIds = args.getIntegerArrayList("item_ids");
        mAdapter = new DetailsAdapter(this, mIds);

        mCurrentPosition = args.getInt("position");
        handleSwapsVisibility();

        mViewPager = findViewById(R.id.details_viewpager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                handleSwapsVisibility();
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

        mSwapLeft.setOnClickListener(v -> mViewPager.setCurrentItem(--mCurrentPosition));
        mSwapRight.setOnClickListener(v -> mViewPager.setCurrentItem(++mCurrentPosition));

        findViewById(R.id.fab_new).setOnClickListener(v -> newItem());
        findViewById(R.id.fab_delete).setOnClickListener(v -> delete());
        findViewById(R.id.fab_edit).setOnClickListener(v -> update());
    }

    private void handleSwapsVisibility() {
        mSwapLeft.setVisibility(mCurrentPosition == 0 ? INVISIBLE : VISIBLE);
        mSwapRight.setVisibility(mCurrentPosition + 1 == mAdapter.getItemCount() ? INVISIBLE : VISIBLE);
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
                if (requestCode == RC_NEW_ITEM) {
                    int id = data.getIntExtra("item_id", -1);
                    if (id > 0) {
                        mAdapter.add(position, id);
                    }
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

                    Snackbar.make(findViewById(R.id.details_content), R.string.delete_done, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_cancel, v -> {
                                // keep empty action to display action ...
                            })
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                        mDetailsViewModel.delete(mAdapter.remove(mCurrentPosition));
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
        intent.putExtra("position", mCurrentPosition);

        startActivityForResult(intent, RC_NEW_ITEM);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void update() {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra("position", mCurrentPosition);
        intent.putExtra("item_id", mAdapter.getId(mCurrentPosition));

        startActivityForResult(intent, RC_UPDATE_ITEM);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
