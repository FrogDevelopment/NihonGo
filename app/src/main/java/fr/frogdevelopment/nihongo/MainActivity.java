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

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.WarningIMEDialog;
import fr.frogdevelopment.nihongo.dico.DicoFragment;
import fr.frogdevelopment.nihongo.kana.BlankFragment;
import fr.frogdevelopment.nihongo.lessons.LessonsFragment;
import fr.frogdevelopment.nihongo.options.ParametersFragment;
import fr.frogdevelopment.nihongo.review.ReviewParametersFragment;
import fr.frogdevelopment.nihongo.test.TestParametersFragment;

public class MainActivity extends AppCompatActivity {

	@Bind(R.id.drawer_layout)
	DrawerLayout mDrawerLayout;

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private int          mFragmentTitle;

	private InputMethodManager imm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);
		mDrawerTitle = getTitle();

		initIME();
		initToolbar();
		setupDrawerLayout();

		if (savedInstanceState == null) {
			selectItemAtIndex(R.id.navigation_word, true);
		}

		handleIntent(getIntent());
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

	private void setupDrawerLayout() {
		NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
		view.setNavigationItemSelectedListener(menuItem -> {
			menuItem.setChecked(true);
			mDrawerLayout.closeDrawers();

			selectItemAtIndex(menuItem.getItemId(), true);

			return true;
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View view) {
				setTitle(mFragmentTitle);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				// Close the soft-keyboard
				imm.hideSoftInputFromWindow(mDrawerLayout.getWindowToken(), 0);

				setTitle(mDrawerTitle);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
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

		if (isNoJapanIME)
			WarningIMEDialog.newInstance().show(getFragmentManager(), "warningIMEDialog");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.menu_search:
				// onSearchRequested();
				return true;

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

	private static int CURRENT_INDEX = -1;

	private void selectItemAtIndex(int id, boolean invalideOptionsMenu) {
		if (CURRENT_INDEX == id) {
			return;
		}

		// Create a new fragment and specify the view to show based on index
		Bundle args;
		Fragment fragment;
		switch (id) {

			case R.id.navigation_word:
				mFragmentTitle = R.string.menu_subitem_word;
				fragment = new DicoFragment();
				args = new Bundle();
				args.putSerializable("type", Type.WORD);
				fragment.setArguments(args);
				break;

			case R.id.navigation_expression:
				mFragmentTitle = R.string.menu_subitem_expression;
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

			case R.id.navigation_kana:
				mFragmentTitle = R.string.menu_subitem_kana;
//				fragment = new HelpKanaFragment();
				fragment = new BlankFragment();
				break;

			case R.id.navigation_parameters:
				mFragmentTitle = R.string.menu_subitem_parameters;
				fragment = new ParametersFragment();
				break;

			case R.id.navigation_lessons:
				mFragmentTitle = R.string.menu_subitem_lessons;
				fragment = new LessonsFragment();
				break;

			default:
				return;
		}

		// Insert the fragment by replacing any existing fragment
		final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.content_frame, fragment);
//		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.commit();

		mDrawerLayout.closeDrawers();
		if (invalideOptionsMenu) {
			invalidateOptionsMenu();
		}

		CURRENT_INDEX = id;
		setTitle(mFragmentTitle);
	}

	@Override
	public void setTitle(int title) {
		if (toolbar != null) {
			toolbar.setTitle(title);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		if (toolbar != null) {
			toolbar.setTitle(title);
		}
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
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	// ************************** SEARCH ************************** \\

	@Override
	protected void onNewIntent(Intent intent) {
		// Because this activity has set launchMode="singleTop", the system calls this method
		// to deliver the intent if this activity is currently the foreground activity when
		// invoked again (when the user executes a search from this activity, we don't create
		// a new instance of this activity, so the system delivers the search intent here)
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			showResults(query);
		}
	}

	/**
	 * Searches the dictionary and displays results for the given query.
	 *
	 * @param query The search query
	 */
	private void showResults(String query) {
		Bundle args = new Bundle();
		args.putString("query", query);
		args.putSerializable("type", CURRENT_INDEX == R.id.navigation_word ? Type.WORD : Type.EXPRESSION);

		final DicoFragment fragment = new DicoFragment();
		fragment.setArguments(args);

		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

		setTitle(R.string.menu_subitem_word);

		mDrawerLayout.closeDrawers();
	}

	@Override
	public void onBackPressed() {
		if (CURRENT_INDEX != R.id.navigation_word) {
			selectItemAtIndex(R.id.navigation_word, true);
		} else {
			// todo : un toast demandant un back une seconde fois pour sortir
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.closing_activity_title)
					.setMessage(R.string.closing_activity_message)
					.setPositiveButton(getString(R.string.yes), (dialog, which) -> finish())
					.setNegativeButton(getString(R.string.no), null)
					.show();
		}
	}

	@Override
	protected void onDestroy() {
		CURRENT_INDEX = -1;
		super.onDestroy();
	}
}
