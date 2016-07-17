/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class DetailsActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener {

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@BindView(R.id.details_viewpager)
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

		Bundle args = getIntent().getExtras();
		mType = (Type) args.getSerializable("type");
		List<Item> items = args.getParcelableArrayList("items");

		DetailsAdapter mAdapter = new DetailsAdapter(getFragmentManager(), items, mType);
		mViewPager.setAdapter(mAdapter);
		int position = args.getInt("position");
		mViewPager.setCurrentItem(position);

		initToolbar();

		boolean doNotshow = PreferencesHelper.getInstance(this).getBoolean(Preferences.HELP_DETAILS);
		if (!doNotshow) {
			HelpDialog.show(getFragmentManager(), R.layout.dialog_help_details, true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initToolbar() {
		setSupportActionBar(toolbar);
		final ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
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
				.setIcon(R.drawable.ic_warning_black)
				.setTitle(R.string.delete_title)
				.setMessage(R.string.delete_detail)
				.setPositiveButton(R.string.positive_button_continue, (dialog, which) -> onDelete(item))
				.setNegativeButton(android.R.string.no, null)
				.show();
	}

	private void onDelete(Item item) {
		Uri uri = Uri.parse(mType.uri + "/" + item.id);
		getContentResolver().delete(uri, null, null);
		Snackbar.make(findViewById(R.id.details_content), R.string.delete_done, Snackbar.LENGTH_LONG).show();
		back();
	}

	@Override
	public void update(Item item) {

		Intent intent = new Intent(this, InputActivity.class);
		intent.putExtra("type", mType);
		intent.putExtra("item", item);

		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public void setFavorite(Item item) {
		final ContentValues values = new ContentValues();
		values.put(DicoContract.FAVORITE, item.favorite);

		updateItem(item, values);
	}

	@Override
	public void setLearned(Item item) {
		final ContentValues values = new ContentValues();
		values.put(DicoContract.LEARNED, item.learned);

		updateItem(item, values);
	}

	private void updateItem(Item item, ContentValues values) {
		final String where = DicoContract._ID + "=?";
		final String[] selectionArgs = {item.id};

		getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
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
