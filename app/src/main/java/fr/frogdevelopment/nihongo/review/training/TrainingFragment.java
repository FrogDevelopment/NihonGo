package fr.frogdevelopment.nihongo.review.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static fr.frogdevelopment.nihongo.R.id.review_count;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TrainingFragment extends Fragment {

    private Details mItem;
    private String test;

    private ImageView mBookmark;
    private ImageView mRate0;
    private ImageView mRate1;
    private ImageView mRate2;
    private TextSwitcher mKanaSwitcher;
    private TextSwitcher mTestSwitcher;
    private TrainingViewModel mTrainingViewModel;
    private ChipGroup mTagsChipGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTrainingViewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        return inflater.inflate(R.layout.review_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        TextView countView = rootView.findViewById(review_count);
        TextView reviewedView = rootView.findViewById(R.id.review_reviewed);
        TextView infoTitleView = rootView.findViewById(R.id.review_info_title);
        TextView infoView = rootView.findViewById(R.id.review_info);
        TextView exampleTitleView = rootView.findViewById(R.id.review_example_title);
        TextView exampleView = rootView.findViewById(R.id.review_example);
        mTagsChipGroup = rootView.findViewById(R.id.review_tags);

        mKanaSwitcher = rootView.findViewById(R.id.review_textSwitcher_kana);
        mKanaSwitcher.setOnClickListener(view -> {
            mKanaSwitcher.setText(mItem.kana);
            mKanaSwitcher.setClickable(false);

        });
        mTestSwitcher = rootView.findViewById(R.id.review_textSwitcher_test);
        mTestSwitcher.setOnClickListener(view -> {
            mTestSwitcher.setText(test);
            mTestSwitcher.setClickable(false);

        });

        Bundle args = requireArguments();
        countView.setText(args.getString("count"));

        mItem = mTrainingViewModel.get(args.getInt("position"));

        boolean isJapaneseReviewed = mTrainingViewModel.isJapaneseReview();

        boolean kanjiPst = false;
        if (isNoneBlank(mItem.kanji)) {
            kanjiPst = true;
            if (isJapaneseReviewed) {
                reviewedView.setText(mItem.kanji);
            } else {
                test = mItem.kanji;
                mTestSwitcher.setText(getString(R.string.review_switch_kanji));
            }
        }

        if (isNoneBlank(mItem.kana)) {
            if (!kanjiPst) {
                if (isJapaneseReviewed) {
                    reviewedView.setText(mItem.kana);
                } else {
                    test = mItem.kana;
                    mTestSwitcher.setText(getString(R.string.review_switch_kana));
                }
                mKanaSwitcher.setVisibility(INVISIBLE);
            } else {
                mKanaSwitcher.setText(getString(R.string.review_switch_kana));
            }
        } else {
            mKanaSwitcher.setVisibility(INVISIBLE);
        }

        if (isJapaneseReviewed) {
            test = mItem.input;
            mTestSwitcher.setText(getString(R.string.review_switch_input));
        } else {
            reviewedView.setText(mItem.input);
        }

        if (isNotBlank(mItem.details)) {
            infoView.setText(mItem.details);
            infoView.setVisibility(VISIBLE);
            infoTitleView.setVisibility(VISIBLE);
        } else {
            infoView.setText(null);
            infoView.setVisibility(GONE);
            infoTitleView.setVisibility(GONE);
        }

        if (isNotBlank(mItem.example)) {
            exampleView.setText(mItem.example);
            exampleView.setVisibility(VISIBLE);
            exampleTitleView.setVisibility(VISIBLE);
        } else {
            exampleView.setText(null);
            exampleView.setVisibility(GONE);
            exampleTitleView.setVisibility(GONE);
        }

        if (isNotBlank(mItem.tags)) {
            Stream.of(mItem.tags.split(", ")).forEach(this::addChipToGroup);
        }

        mBookmark = rootView.findViewById(R.id.bookmark);
        mBookmark.setOnClickListener(v -> bookmarkItem());
        handleBookmark();

        mRate0 = rootView.findViewById(R.id.rate_0);
        mRate0.setOnClickListener(v -> setRate(0));
        mRate1 = rootView.findViewById(R.id.rate_1);
        mRate1.setOnClickListener(v -> setRate(1));
        mRate2 = rootView.findViewById(R.id.rate_2);
        mRate2.setOnClickListener(v -> setRate(2));
        handleRate();
    }

    private void addChipToGroup(String tag) {
        Chip chip = new Chip(requireContext());
        chip.setText(tag);
        chip.setClickable(false);
        chip.setChipBackgroundColorResource(R.color.accent);
        chip.setTextColor(getResources().getColor(R.color.white, requireActivity().getTheme()));

        mTagsChipGroup.addView(chip);
    }

    private void handleRate() {
        switch (mItem.learned) {
            case 1:
                mRate0.setImageResource(R.drawable.ic_baseline_star_24);
                mRate1.setImageResource(R.drawable.ic_baseline_star_24);
                mRate2.setImageResource(R.drawable.ic_baseline_star_border_24);

                break;

            case 2:
                mRate0.setImageResource(R.drawable.ic_baseline_star_24);
                mRate1.setImageResource(R.drawable.ic_baseline_star_24);
                mRate2.setImageResource(R.drawable.ic_baseline_star_24);

                break;

            case 0:
            default:
                mRate0.setImageResource(R.drawable.ic_baseline_star_24);
                mRate1.setImageResource(R.drawable.ic_baseline_star_border_24);
                mRate2.setImageResource(R.drawable.ic_baseline_star_border_24);

                break;
        }
    }

    private void handleBookmark() {
        mBookmark.setImageResource(mItem.bookmark ? R.drawable.ic_baseline_bookmark_24 : R.drawable.ic_baseline_bookmark_border_24);
    }

    private void setRate(int rate) {
        mItem.learned = rate;
        handleRate();

        mTrainingViewModel.update(mItem);
    }

    private void bookmarkItem() {
        mItem.switchBookmark();
        handleBookmark();

        mTrainingViewModel.update(mItem);

        Toast.makeText(requireActivity(), getString(mItem.bookmark ? R.string.bookmark_add : R.string.bookmark_remove), Toast.LENGTH_SHORT).show();
    }
}
