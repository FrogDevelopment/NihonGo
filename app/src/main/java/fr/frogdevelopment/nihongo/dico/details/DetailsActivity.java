/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class DetailsActivity extends AppCompatActivity {

	public static final int RC_NEW_ITEM    = 777;
	public static final int RC_UPDATE_ITEM = 666;
	private List<Item>     mItems;
	private Type           mType;
	private DetailsAdapter mAdapter;
	private int            mCurrentPosition;


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

		FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fab_menu);

		Bundle args = getIntent().getExtras();
		mType = (Type) args.getSerializable("type");
		mItems = args.getParcelableArrayList("items");

		mAdapter = new DetailsAdapter(getFragmentManager());
		ViewPager viewPager = (ViewPager) findViewById(R.id.details_viewpager);
		viewPager.setAdapter(mAdapter);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				mCurrentPosition = position;
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				fam.close(true);
			}
		});
		final int position = args.getInt("position");
		viewPager.setCurrentItem(position);

		boolean doNotShow = PreferencesHelper.getInstance(this).getBoolean(Preferences.HELP_DETAILS);
		if (!doNotShow) {
			HelpDialog.show(getFragmentManager(), R.layout.dialog_help_details, true);
		}


		ImageView swapLeft = (ImageView) findViewById(R.id.swap_left);
		swapLeft.setOnClickListener(v -> viewPager.setCurrentItem(--mCurrentPosition));
		ImageView swapRight = (ImageView) findViewById(R.id.swap_right);
		swapRight.setOnClickListener(v -> viewPager.setCurrentItem(++mCurrentPosition));

		FloatingActionButton fabNew = (FloatingActionButton) findViewById(R.id.fab_new);
		fabNew.setOnClickListener(v -> newItem());

		FloatingActionButton fabDuplicate = (FloatingActionButton) findViewById(R.id.fab_duplicate);
		fabDuplicate.setOnClickListener(v -> duplicate());

		FloatingActionButton fabDelete = (FloatingActionButton) findViewById(R.id.fab_delete);
		fabDelete.setOnClickListener(v -> delete());

		FloatingActionButton mFabEdit = (FloatingActionButton) findViewById(R.id.fab_edit);
		mFabEdit.setOnClickListener(v -> update());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			int position = data.getIntExtra("position", -1);
			if (position > -1) {
				Item item = data.getParcelableExtra("item");
				switch (requestCode) {
					case RC_NEW_ITEM:
						mItems.add(position, item);
						mAdapter.notifyDataSetChanged();
						break;
					case RC_UPDATE_ITEM:
						mItems.set(position, item);
						mAdapter.notifyDataSetChanged();
						break;
				}
			}
		}
	}

	// ************************************************************* \\
	private void delete() {
		// Ask the user if they want to delete
		new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_warning)
				.setTitle(R.string.delete_title)
				.setMessage(R.string.delete_detail)
				.setPositiveButton(R.string.positive_button_continue, (dialog, which) -> {
					Uri uri = Uri.parse(mType.uri + "/" + mItems.get(mCurrentPosition).id);
					getContentResolver().delete(uri, null, null);
					Snackbar.make(findViewById(R.id.details_content), R.string.delete_done, Snackbar.LENGTH_LONG).show();
					back();
				})
				.setNegativeButton(android.R.string.no, null)
				.show();
	}

	private void newItem() {
		Intent intent = new Intent(this, InputActivity.class);
		intent.putExtra("type", mType);
		intent.putExtra("position", mCurrentPosition);

		startActivityForResult(intent, RC_NEW_ITEM);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void update() {
		Intent intent = new Intent(this, InputActivity.class);
		intent.putExtra("type", mType);
		intent.putExtra("position", mCurrentPosition);
		intent.putExtra("item", mItems.get(mCurrentPosition));

		startActivityForResult(intent, RC_UPDATE_ITEM);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void duplicate() {
		Item item = mItems.get(mCurrentPosition);

		final ContentValues values = new ContentValues();
		values.put(DicoContract.INPUT, item.input);
		values.put(DicoContract.SORT_LETTER, item.sort_letter);
		values.put(DicoContract.KANJI, item.kanji);
		values.put(DicoContract.KANA, item.kana);
		values.put(DicoContract.TAGS, item.tags);
		values.put(DicoContract.DETAILS, item.details);
		values.put(DicoContract.EXAMPLE, item.example);
		values.put(DicoContract.TYPE, mType.code);

		Uri insert = getContentResolver().insert(mType.uri, values);
		item.id = String.valueOf(ContentUris.parseId(insert));

		mItems.add(mCurrentPosition, item);
		mAdapter.notifyDataSetChanged();

		// TOAST
		Snackbar.make(findViewById(R.id.details_content), R.string.input_duplicated_OK, Snackbar.LENGTH_LONG).show();
	}

	// ************************************************************* \\
	private class DetailsAdapter extends FragmentStatePagerAdapter {

		private final int mCount;

		private DetailsAdapter(FragmentManager fm) {
			super(fm);
			mCount = mItems.size();
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

		public int getItemPosition(Object object) {
			// Causes adapter to reload all Fragments when // notifyDataSetChanged is called
			return POSITION_NONE;
		}
	}

}
