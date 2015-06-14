/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.help;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnPageChange;
import fr.frogdevelopment.nihongo.R;

public class HelpKanaFragment extends Fragment implements ActionBar.TabListener {

    private static final int INDEX_HIRAGANA = 0;
    private static final int INDEX_KATAKANA = 1;

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    PagerAdapter mDemoCollectionPagerAdapter;

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help_pager, container, false);

        ButterKnife.inject(this, rootView);

        mDemoCollectionPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        final ActionBar actionBar = getActivity().getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Add 2 tabs, specifying the tab's text and TabListener
        int title = -1;
        int icon = -1;
        for (int i = 0; i < 2; i++) {
            switch (i) {
                case INDEX_HIRAGANA:
                    title = R.string.help_hiragana;
                    icon = R.drawable.ic_hiragana;
                    break;

                case INDEX_KATAKANA:
                    title = R.string.help_katakana;
                    icon = R.drawable.ic_katakana;
                    break;

                default:
                    break;
            }
            actionBar.addTab(actionBar.newTab()
                    .setText(title)
                    .setIcon(icon)
                    .setTabListener(this));
        }

        return rootView;
    }

    @OnPageChange(R.id.pager)
    void onPageChange(int position) {
        // When swiping between pages, select the corresponding tab.
        getActivity().getActionBar().setSelectedNavigationItem(position);
    }

    @Override
    public void onDestroyView() {

        final ActionBar actionBar = getActivity().getActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.removeAllTabs();

        super.onDestroyView();
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        HelpFragment mHiraganaFragment;
        HelpFragment mKanaFragment;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case INDEX_HIRAGANA:
                    if (mHiraganaFragment == null) {
                        mHiraganaFragment = createFragment(R.drawable.table_hiragana);
                    }

                    return mHiraganaFragment;

                case INDEX_KATAKANA:
                    if (mKanaFragment == null) {
                        mKanaFragment = createFragment(R.drawable.table_katakana);
                    }

                    return mKanaFragment;

                default:
                    throw new IllegalStateException();
            }
        }

        private HelpFragment createFragment(int resourceId) {
            HelpFragment mHelpFragment = new HelpFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("imageSource", resourceId);
            mHelpFragment.setArguments(bundle);

            return mHelpFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

}
