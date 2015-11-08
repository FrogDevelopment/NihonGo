/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;

public class ReviewFragment extends Fragment {

    interface OnFragmentInteractionListener {
        void favorite(Item item);
    }

    private WeakReference<OnFragmentInteractionListener> mListener;


    @InjectView(R.id.review_reviewed)
    TextView reviewedTV;
    @InjectView(R.id.review_textSwitcher_kana)
    TextSwitcher kanaTS;
    @InjectView(R.id.review_textSwitcher_test)
    TextSwitcher testTS;
    @InjectView(R.id.review_details)
    TextView detailsView;

    private Item mItem;
    private String test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated properly.
        View rootView = inflater.inflate(R.layout.page_review_word, container, false);

        ButterKnife.inject(this, rootView);

        setHasOptionsMenu(true);

        populateView();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.review, menu);

        MenuItem favoriteMenuItem = menu.findItem(R.id.menu_review_favorite);
        favoriteMenuItem.setIcon("1".equals(mItem.favorite) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_review_favorite:
                mItem.favorite = "1".equals(mItem.favorite) ? "0" : "1";
                mListener.get().favorite(mItem);

                return true;

            default:
                return false;
        }
    }

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
        try {
            mListener = new WeakReference<>((OnFragmentInteractionListener) context);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
        }
	}

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void populateView() {
        Bundle args = getArguments();

        mItem = args.getParcelable("item");

        boolean isJapaneseReviewed = args.getBoolean("isJapaneseReviewed");

        boolean kanjiPst = false;
        if (StringUtils.isNoneBlank(mItem.kanji)) {
            kanjiPst = true;
            if (isJapaneseReviewed) {
                reviewedTV.setText(mItem.kanji);
            } else {
                test = mItem.kanji;
                testTS.setText(getActivity().getString(R.string.review_switch_kanji));
            }
        }

        if (StringUtils.isNoneBlank(mItem.kana)) {
            if (!kanjiPst) {
                if (isJapaneseReviewed) {
                    reviewedTV.setText(mItem.kana);
                } else {
                    test = mItem.kana;
                    testTS.setText(getActivity().getString(R.string.review_switch_kana));
                }
                kanaTS.setVisibility(View.GONE);
            } else {
                kanaTS.setText(getActivity().getString(R.string.review_switch_kana));
            }
        } else {
            kanaTS.setVisibility(View.GONE);
        }

        if (isJapaneseReviewed) {
            test = mItem.input;
            testTS.setText(getActivity().getString(R.string.review_switch_input));
        } else {
            reviewedTV.setText(mItem.input);
        }

        detailsView.setText(mItem.details);
    }

    @OnClick(R.id.review_textSwitcher_kana)
    void onClickKana() {
        kanaTS.setText(mItem.kana);
        kanaTS.setClickable(false);
    }

    @OnClick(R.id.review_textSwitcher_test)
    void onClickTest() {
        testTS.setText(test);
        testTS.setClickable(false);
        if (detailsView.getVisibility() == View.INVISIBLE) {
            detailsView.setVisibility(View.VISIBLE);
        }
    }

}
