/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.frogdevelopment.nihongo.data.Item;

class ReviewAdapter extends FragmentStatePagerAdapter {

	private int mCount = 0;
	private       List<Item> items;
	private final boolean    isJapaneseReviewed;
	private final Map<Integer, ReviewFragment> mapFragments = new HashMap<>();

	public ReviewAdapter(FragmentManager fm, boolean isJapaneseType) {
		super(fm);
		this.isJapaneseReviewed = isJapaneseType;
	}

	@Override
	public Fragment getItem(int position) {
		ReviewFragment fragment = new ReviewFragment();
		Item item = items.get(position);

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
		items = new ArrayList<>(mCount);

		while (cursor.moveToNext()) {
			items.add(new Item(cursor));
		}

		notifyDataSetChanged();
	}

	public ReviewFragment getItemAt(int position) {
		return mapFragments.get(position);
	}
}
