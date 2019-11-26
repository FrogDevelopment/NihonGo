package fr.frogdevelopment.nihongo.review.training;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

class TrainingAdapter extends FragmentStateAdapter {

    private final int mCount;

    TrainingAdapter(FragmentActivity fragmentActivity, int count) {
        super(fragmentActivity);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        TrainingFragment fragment = new TrainingFragment();

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
