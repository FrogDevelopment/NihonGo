package fr.frogdevelopment.nihongo.review;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.github.clans.fab.FloatingActionButton;

import java.util.List;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_IS_JAPANESE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_ONLY_FAVORITE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_QUANTITY;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_RATE;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_SORT;
import static fr.frogdevelopment.nihongo.review.ReviewParametersFragment.REVIEW_TAGS;

public class ReviewActivity extends AppCompatActivity implements Observer<List<Details>> {

    private int mCurrentPosition;

    private ViewPager2 mViewPager;
    private ReviewAdapter mAdapter;
    private ImageView mSwapLeft;
    private ImageView mSwapRight;
    private FloatingActionButton mFabAgain;

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

        ReviewViewModel reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        setContentView(R.layout.activity_review);

        mSwapLeft = findViewById(R.id.swap_left);
        mSwapLeft.setOnClickListener(v -> mViewPager.setCurrentItem(--mCurrentPosition));
        mSwapRight = findViewById(R.id.swap_right);
        mSwapRight.setOnClickListener(v -> mViewPager.setCurrentItem(++mCurrentPosition));

        mFabAgain = findViewById(R.id.fab_again);
        mFabAgain.setOnClickListener(view -> {
            mFabAgain.hide(true);
            mViewPager.setCurrentItem(0);
        });

        Bundle args = getIntent().getExtras();

        boolean isJapaneseReviewed = args.getBoolean(REVIEW_IS_JAPANESE);
        boolean onlyFavorite = args.getBoolean(REVIEW_ONLY_FAVORITE);
        final int selectedSort = args.getInt(REVIEW_SORT);
        String quantity = args.getString(REVIEW_QUANTITY);
        int learnedRate = args.getInt(REVIEW_RATE);
        String[] tags = args.getStringArray(REVIEW_TAGS);

        mViewPager = findViewById(R.id.review_viewpager);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                handleSwaps(position);
            }
        });

        reviewViewModel.isJapaneseReview(isJapaneseReviewed);
        reviewViewModel.reviews(onlyFavorite, selectedSort, quantity, learnedRate, tags)
                .observe(this, this);
    }

    private void handleSwaps(int position) {
        mCurrentPosition = position;

        mSwapLeft.setVisibility(position == 0 ? INVISIBLE : VISIBLE);
        boolean lastPosition = position + 1 == mAdapter.getItemCount();
        mSwapRight.setVisibility(lastPosition ? INVISIBLE : VISIBLE);
        if (lastPosition) {
            mFabAgain.show(true);
        } else {
            mFabAgain.hide(true);
        }
    }

    @Override
    public void onChanged(List<Details> data) {
        mAdapter = new ReviewAdapter(this, data.size());
        mViewPager.setAdapter(mAdapter);
    }
}
