package fr.frogdevelopment.nihongo;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.WarningIMEDialog;
import fr.frogdevelopment.nihongo.dico.DicoFragment;
import fr.frogdevelopment.nihongo.drawer.DrawerItemAdapter;
import fr.frogdevelopment.nihongo.help.HelpKanaFragment;
import fr.frogdevelopment.nihongo.lessons.LessonsFragment;
import fr.frogdevelopment.nihongo.options.ParametersFragment;
import fr.frogdevelopment.nihongo.review.ReviewParametersFragment;
import fr.frogdevelopment.nihongo.test.TestParametersFragment;

public class MainActivity extends FragmentActivity {

//    private static final String LOG_TAG = "NIHON_GO";

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.left_drawer)
    RelativeLayout mDrawerRelativeLayout;
    @InjectView(R.id.list_view_drawer)
    ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private int mFragmentTitle;

    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.drawer);

        mDrawerTitle = getTitle();
        ButterKnife.inject(this);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        createMenuItemAdapter();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
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

        if (savedInstanceState == null) {
            selectItemAtIndex(INDEX_DICO_WORD, true);
        }

        handleIntent(getIntent());

        checkIME();
    }

    private void checkIME() {
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

    private void createMenuItemAdapter() {
        mFragmentTitle = R.string.menu_subitem_word;

        final DrawerItemAdapter mAdapter = new DrawerItemAdapter(this);
        mAdapter.addItem(R.string.menu_subitem_word, INDEX_DICO_WORD);
        mAdapter.addItem(R.string.menu_subitem_expression, INDEX_DICO_EXPRESSION);
        mAdapter.addItem(R.string.menu_subitem_kana, INDEX_KANA);

        mAdapter.addSeparator();
        mAdapter.addItem(R.string.menu_subitem_review, INDEX_REVIEW);
        mAdapter.addItem(R.string.menu_subitem_test, INDEX_TEST);

        mAdapter.addSeparator();
        mAdapter.addItem(R.string.menu_subitem_lessons, INDEX_LESSONS);
        mAdapter.addItem(R.string.menu_subitem_parameters, INDEX_PARAMETERS);

        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
//                view.setActivated(true);
                mAdapter.notifyDataSetChanged();
                selectItemAtIndex(mAdapter.getIndex(position), true);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                // onSearchRequested();
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

    static final int INDEX_DICO_WORD = 1;
    static final int INDEX_DICO_EXPRESSION = 2;
    static final int INDEX_KANA = 3;

    static final int INDEX_REVIEW = 4;
    static final int INDEX_TEST = 5;

    static final int INDEX_LESSONS = 6;
    static final int INDEX_PARAMETERS = 7;

    private void selectItemAtIndex(int index, boolean invalideOptionsMenu) {
        if (CURRENT_INDEX == index) {
            return;
        }

        // Create a new fragment and specify the view to show based on index
        Bundle args;
        Fragment fragment;
        switch (index) {

            case INDEX_DICO_WORD:
                mFragmentTitle = R.string.menu_subitem_word;
                fragment = new DicoFragment();
                args = new Bundle();
                args.putSerializable("type", Type.WORD);
                fragment.setArguments(args);
                break;

            case INDEX_DICO_EXPRESSION:
                mFragmentTitle = R.string.menu_subitem_expression;
                fragment = new DicoFragment();
                args = new Bundle();
                args.putSerializable("type", Type.EXPRESSION);
                fragment.setArguments(args);
                break;

            case INDEX_REVIEW:
                mFragmentTitle = R.string.param_review;
                fragment = new ReviewParametersFragment();
                break;

            case INDEX_TEST:
                mFragmentTitle = R.string.param_test;
                fragment = new TestParametersFragment();
                break;

            case INDEX_KANA:
                mFragmentTitle = R.string.menu_subitem_kana;
                fragment = new HelpKanaFragment();
                break;

            case INDEX_PARAMETERS:
                mFragmentTitle = R.string.menu_subitem_parameters;
                fragment = new ParametersFragment();
                break;

            case INDEX_LESSONS:
                mFragmentTitle = R.string.menu_subitem_lessons;
                fragment = new LessonsFragment();
                break;

            default:
                return;
        }

        // Insert the fragment by replacing any existing fragment
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

        mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
        if (invalideOptionsMenu) {
            invalidateOptionsMenu();
        }

        CURRENT_INDEX = index;
        setTitle(mFragmentTitle);
    }

    @Override
    public void setTitle(int title) {
//        mFragmentTitle = title;
        if (getActionBar() != null)
            getActionBar().setTitle(title);
    }

    @Override
    public void setTitle(CharSequence title) {
//        mFragmentTitle = title;
        if (getActionBar() != null)
            getActionBar().setTitle(title);
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
        args.putSerializable("type", Type.WORD);

        final DicoFragment fragment = new DicoFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

        setTitle(R.string.menu_subitem_word);

        mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
    }

    @Override
    public void onBackPressed() {
        if (CURRENT_INDEX != INDEX_DICO_WORD) {
            selectItemAtIndex(INDEX_DICO_WORD, true);
        } else {
            // todo : un toast demandant un back une seconde fois pour sortir
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.closing_activity_title)
                    .setMessage(R.string.closing_activity_message)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
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
