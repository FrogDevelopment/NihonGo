package fr.frogdevelopment.nihongo.dico.details;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

class DetailsAdapter extends FragmentStateAdapter {

    private final int mCount;
    private final List<Integer> mIds;

    DetailsAdapter(FragmentActivity fragmentActivity, List<Integer> ids) {
        super(fragmentActivity);
        mCount = ids.size();
        mIds = ids;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt("item_id", mIds.get(position));

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    Integer getId(int position) {
        return mIds.get(position);
    }

    Integer remove(int position) {
        Integer removedId = mIds.remove(position);
        notifyDataSetChanged();
        return removedId;
    }

    void add(int position, Integer id) {
        mIds.add(position, id);
        notifyDataSetChanged();
    }

}
