package fr.frogdevelopment.nihongo.review;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Row;

class ReviewAdapter extends FragmentStatePagerAdapter {

    private int mCount = 0;
    private List<Row> mRows;
    private final boolean isJapaneseReviewed;
    private final SparseArray<ReviewFragment> mapFragments = new SparseArray<>();

    ReviewAdapter(FragmentManager fm, boolean isJapaneseType) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.isJapaneseReviewed = isJapaneseType;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        ReviewFragment fragment = new ReviewFragment();
        Row row = mRows.get(position);

        Bundle args = new Bundle();
//        args.putParcelable("item", item);
        args.putBoolean("isJapaneseReviewed", isJapaneseReviewed);
        args.putString("count", (position + 1) + "/" + mCount);

        fragment.setArguments(args);

        mapFragments.put(position, fragment);

        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mapFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "ITEM" + (position + 1);
    }

    public void setData(Cursor cursor) {
        mCount = cursor.getCount();
        mRows = new ArrayList<>(mCount);

        while (cursor.moveToNext()) {
//            mItems.add(new Item(cursor));
        }

        cursor.close();

        notifyDataSetChanged();
    }
}
