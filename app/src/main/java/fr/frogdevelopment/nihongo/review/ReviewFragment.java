/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.review;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;

import static fr.frogdevelopment.nihongo.R.id.review_count;

public class ReviewFragment extends Fragment {

    private Item mItem;
    private String test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated properly.
        View rootView = inflater.inflate(R.layout.fragment_review, container, false);

        populateView(rootView);

        return rootView;
    }

    private void populateView(View rootView) {
        TextView mCount = (TextView) rootView.findViewById(review_count);
        TextView mReviewed = (TextView) rootView.findViewById(R.id.review_reviewed);
        TextView mInfoTitle = (TextView) rootView.findViewById(R.id.review_info_title);
        TextView mInfo = (TextView) rootView.findViewById(R.id.review_info);
        TextView mExampleTitle = (TextView) rootView.findViewById(R.id.review_example_title);
        TextView mExample = (TextView) rootView.findViewById(R.id.review_example);
        TextView mTagsTitle = (TextView) rootView.findViewById(R.id.review_tags_title);
        TextView mTags = (TextView) rootView.findViewById(R.id.review_tags);
        TextView mRatio = (TextView) rootView.findViewById(R.id.review_tags_ratio);
        TextSwitcher mKana = (TextSwitcher) rootView.findViewById(R.id.review_textSwitcher_kana);
        mKana.setOnClickListener(view -> {
            mKana.setText(mItem.kana);
            mKana.setClickable(false);

        });
        TextSwitcher mTest = (TextSwitcher) rootView.findViewById(R.id.review_textSwitcher_test);
        mTest.setOnClickListener(view -> {
            mTest.setText(test);
            mTest.setClickable(false);

        });

        Bundle args = getArguments();
        String count = args.getString("count");
        mCount.setText(count);
        mItem = args.getParcelable("item");

        boolean isJapaneseReviewed = args.getBoolean("isJapaneseReviewed");

        boolean kanjiPst = false;
        if (StringUtils.isNoneBlank(mItem.kanji)) {
            kanjiPst = true;
            if (isJapaneseReviewed) {
                mReviewed.setText(mItem.kanji);
            } else {
                test = mItem.kanji;
                mTest.setText(getActivity().getString(R.string.review_switch_kanji));
            }
        }

        if (StringUtils.isNoneBlank(mItem.kana)) {
            if (!kanjiPst) {
                if (isJapaneseReviewed) {
                    mReviewed.setText(mItem.kana);
                } else {
                    test = mItem.kana;
                    mTest.setText(getActivity().getString(R.string.review_switch_kana));
                }
                mKana.setVisibility(View.GONE);
            } else {
                mKana.setText(getActivity().getString(R.string.review_switch_kana));
            }
        } else {
            mKana.setVisibility(View.GONE);
        }

        if (isJapaneseReviewed) {
            test = mItem.input;
            mTest.setText(getActivity().getString(R.string.review_switch_input));
        } else {
            mReviewed.setText(mItem.input);
        }

        if (StringUtils.isNotBlank(mItem.details)) {
            mInfo.setText(mItem.details);
            mInfo.setVisibility(View.VISIBLE);
            mInfoTitle.setVisibility(View.VISIBLE);
        } else {
            mInfo.setText(null);
            mInfo.setVisibility(View.GONE);
            mInfoTitle.setVisibility(View.GONE);
        }

        if (StringUtils.isNotBlank(mItem.example)) {
            mExample.setText(mItem.example);
            mExample.setVisibility(View.VISIBLE);
            mExampleTitle.setVisibility(View.VISIBLE);
        } else {
            mExample.setText(null);
            mExample.setVisibility(View.GONE);
            mExampleTitle.setVisibility(View.GONE);
        }

        if (StringUtils.isNotBlank(mItem.tags)) {
            mTags.setText(mItem.tags);
            mTags.setVisibility(View.VISIBLE);
            mTagsTitle.setVisibility(View.VISIBLE);
        } else {
            mTags.setText(null);
            mTags.setVisibility(View.GONE);
            mTagsTitle.setVisibility(View.GONE);
        }

        mRatio.setText(mItem.success + "/" + mItem.failed);
    }

}
