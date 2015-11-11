/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.details;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;


public class DetailsFragment extends Fragment {

    interface OnFragmentInteractionListener {
        void update(Item item);

        void delete(Item item);

        void favorite(Item item);
    }

    private WeakReference<OnFragmentInteractionListener> mListener;

    @Bind(R.id.details_word_input)
    TextView mInputView;
    @Bind(R.id.details_word_kanji)
    TextView mKanjiView;
    @Bind(R.id.details_word_kana)
    TextView mKanaView;
    @Bind(R.id.details_word_details)
    TextView mDetailsView;
    @Bind(R.id.details_word_tags)
    TextView mTagsView;

    private Item mItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated properly.
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);

        populateView();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actions_dico, menu);

        MenuItem favoriteMenuItem = menu.findItem(R.id.action_favorite);
        favoriteMenuItem.setIcon("1".equals(mItem.favorite) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mListener.get().delete(mItem);
                break;

            case R.id.action_update:
                mListener.get().update(mItem);
                break;

            case R.id.action_favorite:
                mItem.favorite = "1".equals(mItem.favorite) ? "0" : "1";
                mListener.get().favorite(mItem);
                break;

            default:
                return false;
        }

        return true;
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

        if (mItem == null) {
            return;
        }

        mInputView.setText(mItem.input);

        if (StringUtils.isNoneEmpty(mItem.kanji)) {
            mKanjiView.setText(mItem.kanji);
            mKanjiView.setVisibility(View.VISIBLE);
        }

        if (StringUtils.isNoneEmpty(mItem.kana)) {
            mKanaView.setText(mItem.kana);
            mKanaView.setVisibility(View.VISIBLE);
        }

        mDetailsView.setText(mItem.details);
        if (StringUtils.isNoneEmpty(mItem.details)) {
            mDetailsView.setText(mItem.details);
            mDetailsView.setVisibility(View.VISIBLE);
        }

        if (StringUtils.isNoneEmpty(mItem.tags)) {
            mTagsView.setText(mItem.tags);
            mTagsView.setVisibility(View.VISIBLE);
        }
    }

}
