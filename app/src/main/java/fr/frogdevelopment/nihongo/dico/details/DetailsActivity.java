/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;

public class DetailsActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener {

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.details_viewpager)
	ViewPager mViewPager;

	private Type mType;

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

		setContentView(R.layout.activity_details);

		ButterKnife.bind(this);

		// Show the Up button in the action bar.
		if (getActionBar() != null)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle args = getIntent().getExtras();

		mType = (Type) args.getSerializable("type");

		List<Item> items = args.getParcelableArrayList("items");

		DetailsAdapter mAdapter = new DetailsAdapter(getSupportFragmentManager(), items, mType);
		mViewPager.setAdapter(mAdapter);
		int position = args.getInt("position");
		mViewPager.setCurrentItem(position);

		initToolbar();
	}

	private void initToolbar() {
		setSupportActionBar(toolbar);
		final ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
//			actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				back();
				return true;

			default:
				return false;
		}
	}

	// ************************************************************* \\
	@Override
	public void delete(final Item item) {
		// Ask the user if they want to delete
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.delete_title)
				.setMessage(R.string.delete_detail)
				.setPositiveButton(R.string.positive_button_continue, (dialog, which) -> onDelete(item))
				.setNegativeButton(android.R.string.no, null)
				.show();
	}

	private void onDelete(Item item) {
		Uri uri = Uri.parse(mType.uri + "/" + item.id);
		getContentResolver().delete(uri, null, null);
		Toast.makeText(this, R.string.delete_done, Toast.LENGTH_LONG).show();
		back();
	}

	@Override
	public void update(Item item) {
		startActivity(item.getUpdateIntent(this, mType));
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public void favorite(Item item) {
		final String where = DicoContract._ID + "=?";
		final String[] selectionArgs = {item.id};

		final ContentValues values = new ContentValues();
		values.put(DicoContract.FAVORITE, item.favorite);

		getContentResolver().update(mType.uri, values, where, selectionArgs);

		invalidateOptionsMenu();
	}

	// ************************************************************* \\
	private class DetailsAdapter extends FragmentStatePagerAdapter {

		private final int        mCount;
		private final List<Item> mItems;
		private final Type       mType;

		private DetailsAdapter(FragmentManager fm, List<Item> items, Type type) {
			super(fm);
			mItems = items;
			mCount = items.size();
			mType = type;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = new DetailsFragment();

			Bundle args = new Bundle();
			args.putSerializable("type", mType);
			args.putParcelable("item", mItems.get(position));

			fragment.setArguments(args);

			return fragment;
		}

		@Override
		public int getCount() {
			return mCount;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "ITEM" + (position + 1);
		}

	}

}
