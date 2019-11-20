package fr.frogdevelopment.nihongo.dico.details;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import fr.frogdevelopment.nihongo.contentprovider.DicoContract;

class DetailsAdapter extends FragmentStatePagerAdapter {

    private int mCount;
    private DicoContract.Type mType;

    DetailsAdapter(FragmentManager fm, DicoContract.Type type) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mType = type;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("type", mType);
//            args.putParcelable("item", mItems.get(position));

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    public int getItemPosition(@NonNull Object object) {
        // Causes adapter to reload all Fragments when // notifyDataSetChanged is called
        return POSITION_NONE;
    }
}
