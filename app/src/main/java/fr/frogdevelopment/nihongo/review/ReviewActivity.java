package fr.frogdevelopment.nihongo.review;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;

import com.github.clans.fab.FloatingActionButton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_IS_JAPANESE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_ONLY_FAVORITE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_QUANTITY;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_RATE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_SORT;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_TAGS;

public class ReviewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 710;

    private ReviewAdapter adapter;

    private int mCurrentPosition;
    private ViewPager mViewPager;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            back();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        ImageView swapLeft = findViewById(R.id.swap_left);
        swapLeft.setOnClickListener(v -> mViewPager.setCurrentItem(--mCurrentPosition));
        ImageView swapRight = findViewById(R.id.swap_right);
        swapRight.setOnClickListener(v -> mViewPager.setCurrentItem(++mCurrentPosition));

        FloatingActionButton fabAgain = findViewById(R.id.fab_again);
        fabAgain.setOnClickListener(view -> {
            fabAgain.hide(true);
            mViewPager.setCurrentItem(0);
        });

        Bundle extras = getIntent().getExtras();

        final boolean isJapaneseReviewed = extras.getBoolean(REVIEW_IS_JAPANESE);

        mViewPager = findViewById(R.id.review_viewpager);
        adapter = new ReviewAdapter(getSupportFragmentManager(), isJapaneseReviewed);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                swapLeft.setVisibility(position == 0 ? INVISIBLE : VISIBLE);
                boolean lastPosition = position + 1 == adapter.getCount();
                swapRight.setVisibility(lastPosition ? INVISIBLE : VISIBLE);
                if (lastPosition) {
                    fabAgain.show(true);
                } else {
                    fabAgain.hide(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        LoaderManager.getInstance(this).initLoader(LOADER_ID, extras, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String quantity = args.getString(REVIEW_QUANTITY);
        String limit = " LIMIT " + quantity;

        String selection = "1 = 1";
        String[] likes = null;
        String[] tags = args.getStringArray(REVIEW_TAGS);
        if (tags != null && ArrayUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                likes = ArrayUtils.add(likes, DicoContract.TAGS + " LIKE '%" + tag + "%'");
            }
            selection += " AND (" + StringUtils.join(likes, " OR ") + ")";
        }

        boolean onlyFavorite = args.getBoolean(REVIEW_ONLY_FAVORITE);
        if (onlyFavorite) {
            selection += " AND BOOKMARK = '1'";
        }

        int learnedRate = args.getInt(REVIEW_RATE);
        switch (learnedRate) {
            case 0:
            case 1:
            case 2:
                selection += String.format(" AND LEARNED = '%s'", learnedRate);
                break;
        }

        String sortOrder;
        final int selectedSort = args.getInt(REVIEW_SORT);
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
        LoaderManager.getInstance(this).destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // TODO Auto-generated method stub
    }

}
