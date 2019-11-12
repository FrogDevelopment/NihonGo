/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.legacy.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.nihongo.data.Item;

class ReviewAdapter extends FragmentStatePagerAdapter {

	private int mCount = 0;
	private       List<Item> mItems;
	private final boolean    isJapaneseReviewed;
	private final SparseArray<ReviewFragment> mapFragments         = new SparseArray<>();

	public ReviewAdapter(FragmentManager fm, boolean isJapaneseType) {
		super(fm);
		this.isJapaneseReviewed = isJapaneseType;
	}

	@Override
	public Fragment getItem(int position) {
		ReviewFragment fragment = new ReviewFragment();
		Item item = mItems.get(position);

		Bundle args = new Bundle();
		args.putParcelable("item", item);
		args.putBoolean("isJapaneseReviewed", isJapaneseReviewed);
		args.putString("count", (position + 1) + "/" + mCount);

		fragment.setArguments(args);

		mapFragments.put(position, fragment);

		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
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
		mItems = new ArrayList<>(mCount);

		while (cursor.moveToNext()) {
			mItems.add(new Item(cursor));
		}

		notifyDataSetChanged();
	}

	Item getItemAt(int position) {
		return mItems.get(position);
	}

	void clear() {
		mItems.clear();
		mCount = 0;
		mapFragments.clear();
		notifyDataSetChanged();
	}
}
