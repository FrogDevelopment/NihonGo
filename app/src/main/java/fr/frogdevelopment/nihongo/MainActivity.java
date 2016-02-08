/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.WarningIMEDialog;
import fr.frogdevelopment.nihongo.dico.DicoFragment;
import fr.frogdevelopment.nihongo.kana.KanaViewPage;
import fr.frogdevelopment.nihongo.lessons.LessonsFragment;
import fr.frogdevelopment.nihongo.options.ParametersFragment;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;
import fr.frogdevelopment.nihongo.review.ReviewParametersFragment;
import fr.frogdevelopment.nihongo.test.TestParametersFragment;

public class MainActivity extends AppCompatActivity {

	@Bind(R.id.drawer_layout)
	DrawerLayout mDrawerLayout;

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	@Bind(R.id.progress_spinner)
	ProgressBar progressBar;

	private ActionBarDrawerToggle mDrawerToggle;

	private InputMethodManager imm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);

		initIME();
		initToolbar();
		setupDrawerLayout();

		if (savedInstanceState == null) {
			selectItemAtIndex(R.id.navigation_word);
		}

		handleIntent(getIntent());

		// PUB
		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice("C0FB3332A7EBAEF3452C4C311F32D12B")
				.build();
		mAdView.loadAd(adRequest);
	}

	private void initIME() {
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		boolean isNoJapanIME = true;
		List<InputMethodInfo> inputMethodInfos = imm.getInputMethodList();
		for (InputMethodInfo inputMethodInfo : inputMethodInfos) {
			for (int index = 0, count = inputMethodInfo.getSubtypeCount(); index < count; index++) {
				String locale = inputMethodInfo.getSubtypeAt(index).getLocale();
				if (Locale.JAPAN.toString().equals(locale)) {
					isNoJapanIME = false;
					break;
				}
			}
		}

		if (isNoJapanIME) {
			boolean rememberWarning = PreferencesHelper.getInstance(this).getBoolean(Preferences.REMEMBER_WARNING_IME);
			if (!rememberWarning) {
				WarningIMEDialog.show(getFragmentManager());
			}
		}
	}

	private void initToolbar() {
		setSupportActionBar(toolbar);
		final ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}
	}

	private void setupDrawerLayout() {
		NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
		view.setNavigationItemSelectedListener(menuItem -> {
			menuItem.setChecked(true);
			mDrawerLayout.closeDrawers();

			selectItemAtIndex(menuItem.getItemId());

			return true;
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerOpened(View drawerView) {
				// Close the soft-keyboard
				imm.hideSoftInputFromWindow(mDrawerLayout.getWindowToken(), 0);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;

			default:
				// The action bar home/up action should open or close the drawer.
				// ActionBarDrawerToggle will take care of this.
				if (mDrawerToggle.onOptionsItemSelected(item)) {
					return true;
				}
		}

		return super.onOptionsItemSelected(item);
	}

	private static int CURRENT_VIEW = -1;

	private void selectItemAtIndex(int id) {
		if (!onSearch && CURRENT_VIEW == id) {
			return;
		}

		// Create a new fragment and specify the view to show based on index
		Bundle args;
		Fragment fragment;
		int mFragmentTitle;
		switch (id) {

			case R.id.navigation_word:
				mFragmentTitle = R.string.drawer_item_word;
				fragment = new DicoFragment();
				args = new Bundle();
				args.putSerializable("type", Type.WORD);
				fragment.setArguments(args);
				break;

			case R.id.navigation_expression:
				mFragmentTitle = R.string.drawer_item_expression;
				fragment = new DicoFragment();
				args = new Bundle();
				args.putSerializable("type", Type.EXPRESSION);
				fragment.setArguments(args);
				break;

			case R.id.navigation_review:
				mFragmentTitle = R.string.param_review;
				fragment = new ReviewParametersFragment();
				break;

			case R.id.navigation_test:
				mFragmentTitle = R.string.param_test;
				fragment = new TestParametersFragment();
				break;

			case R.id.navigation_hiragana:
				mFragmentTitle = R.string.global_hiragana;
				fragment = new KanaViewPage();
				args = new Bundle();
				args.putInt("imageSource", R.drawable.table_hiragana);
				fragment.setArguments(args);
				break;

			case R.id.navigation_katakana:
				mFragmentTitle = R.string.global_katakana;
				fragment = new KanaViewPage();
				args = new Bundle();
				args.putInt("imageSource", R.drawable.table_katakana);
				fragment.setArguments(args);
				break;

			case R.id.navigation_parameters:
				mFragmentTitle = R.string.drawer_item_parameters;
				fragment = new ParametersFragment();
				break;

			case R.id.navigation_lessons:
				mFragmentTitle = R.string.drawer_item_lessons;
				fragment = new LessonsFragment();
				break;

			default:
				return;
		}

		// Insert the fragment by replacing any existing fragment
		final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.content_frame, fragment);
		fragmentTransaction.commit();

		mDrawerLayout.closeDrawers();

		// todo voir utilité
		invalidateOptionsMenu();


		CURRENT_VIEW = id;
		setTitle(mFragmentTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		if (onSearch || CURRENT_VIEW != R.id.navigation_word) {
			selectItemAtIndex(R.id.navigation_word);
			onSearch = false;
		} else {
			// todo : un toast demandant un back une seconde fois pour sortir
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_warning_black)
					.setTitle(R.string.closing_activity_title)
					.setMessage(R.string.closing_activity_message)
					.setPositiveButton(getString(R.string.yes), (dialog, which) -> finish())
					.setNegativeButton(getString(R.string.no), null)
					.show();
		}
	}

	@Override
	protected void onDestroy() {
		CURRENT_VIEW = -1;
		onSearch = false;
		ButterKnife.unbind(this);
		super.onDestroy();
	}

	public void showLoading(boolean show) {
		progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	// ************************************************************ \\
	// ************************** SEARCH ************************** \\
	// ************************************************************ \\

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private boolean onSearch = false;

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// Searches the dictionary and displays results for the given query.
			final String query = intent.getStringExtra(SearchManager.QUERY);
			Bundle args = new Bundle();
			args.putString("query", query);
			args.putSerializable("type", CURRENT_VIEW == R.id.navigation_word ? Type.WORD : Type.EXPRESSION);

			final DicoFragment fragment = new DicoFragment();
			fragment.setArguments(args);

			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

			setTitle(CURRENT_VIEW == R.id.navigation_word ? R.string.search_word : R.string.search_expression);

			onSearch = true;
		}
	}

}
