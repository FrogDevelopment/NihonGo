package fr.frogdevelopment.nihongo.review;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;

class ReviewAdapter extends FragmentStateAdapter {

    private final int mCount;
    private final List<Details> mData;
    private final boolean mIsJapaneseReviewed;

    ReviewAdapter(FragmentActivity fragmentActivity, boolean isJapaneseType, List<Details> details) {
        super(fragmentActivity);
        mData = details;
        mCount = details.size();
        mIsJapaneseReviewed = isJapaneseType;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ReviewFragment fragment = new ReviewFragment();
        Details row = mData.get(position);

        Bundle args = new Bundle();
        args.putSerializable("item", row);
        args.putBoolean("isJapaneseReviewed", mIsJapaneseReviewed);
        args.putString("count", (position + 1) + "/" + mCount);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }
}
