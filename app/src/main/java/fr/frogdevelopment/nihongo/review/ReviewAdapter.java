package fr.frogdevelopment.nihongo.review;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

class ReviewAdapter extends FragmentStateAdapter {

    private final int mCount;
    private final ReviewViewModel mReviewViewModel;

    ReviewAdapter(FragmentActivity fragmentActivity, int count) {
        super(fragmentActivity);
        mReviewViewModel = new ViewModelProvider(fragmentActivity).get(ReviewViewModel.class);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ReviewFragment fragment = new ReviewFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("count", (position + 1) + "/" + mCount);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }
}
