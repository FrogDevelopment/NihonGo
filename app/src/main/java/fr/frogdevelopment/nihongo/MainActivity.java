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
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Locale;

import fr.frogdevelopment.nihongo.about.AboutFragment;
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

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initIME();
        setupDrawerLayout();

        if (savedInstanceState == null) {
            selectItemAtIndex(R.id.navigation_word);
        }

        handleIntent(getIntent());
    }

    private void initIME() {
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean isNoJapanIME = true;
        List<InputMethodInfo> inputMethodInfos = mInputMethodManager.getInputMethodList();
        for (InputMethodInfo inputMethodInfo : inputMethodInfos) {
            for (int index = 0, count = inputMethodInfo.getSubtypeCount(); index < count; index++) {
                String languageTag = inputMethodInfo.getSubtypeAt(index).getLanguageTag();
                if (Locale.JAPAN.toLanguageTag().equals(languageTag) || Locale.JAPANESE.toLanguageTag().equals(languageTag)) {
                    isNoJapanIME = false;
                    break;
                }
            }
        }

        if (isNoJapanIME) {
            boolean rememberWarning = PreferencesHelper.getInstance(this).getBoolean(Preferences.REMEMBER_WARNING_IME);
            if (!rememberWarning) {
                WarningIMEDialog.show(getSupportFragmentManager());
            }
        }
    }

    private void setupDrawerLayout() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();

            selectItemAtIndex(menuItem.getItemId());

            return true;
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                // Close the soft-keyboard
                mInputMethodManager.hideSoftInputFromWindow(mDrawerLayout.getWindowToken(), 0);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();
    }

    private static int CURRENT_VIEW = -1;

    public void selectItemAtIndex(int id) {
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

            case R.id.navigation_about:
                mFragmentTitle = R.string.drawer_item_about;
                fragment = new AboutFragment();
                break;

            default:
                return;
        }

        // Insert the fragment by replacing any existing fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

        CURRENT_VIEW = id;
        setTitle(mFragmentTitle);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
            new AlertDialog.Builder(this)
                    .setTitle(R.string.closing_activity_title)
                    .setMessage(R.string.closing_activity_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        CURRENT_VIEW = -1;
        onSearch = false;
        super.onDestroy();
    }

    // ************************************************************ \\
    // ************************** SEARCH ************************** \\
    // ************************************************************ \\

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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

            setTitle(getString(R.string.search_current, query));

            onSearch = true;
        }
    }

}
