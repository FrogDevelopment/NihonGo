package fr.frogdevelopment.nihongo.review.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static fr.frogdevelopment.nihongo.R.id.bookmark;
import static fr.frogdevelopment.nihongo.R.id.rate_0;
import static fr.frogdevelopment.nihongo.R.id.rate_1;
import static fr.frogdevelopment.nihongo.R.id.rate_2;
import static fr.frogdevelopment.nihongo.R.id.review_button;
import static fr.frogdevelopment.nihongo.R.id.review_count;
import static fr.frogdevelopment.nihongo.R.id.review_example;
import static fr.frogdevelopment.nihongo.R.id.review_example_title;
import static fr.frogdevelopment.nihongo.R.id.review_info;
import static fr.frogdevelopment.nihongo.R.id.review_info_title;
import static fr.frogdevelopment.nihongo.R.id.review_line_0;
import static fr.frogdevelopment.nihongo.R.id.review_line_1;
import static fr.frogdevelopment.nihongo.R.id.review_line_2;
import static fr.frogdevelopment.nihongo.R.id.review_tags;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TrainingFragment extends Fragment {

    private Details mItem;
    private TrainingViewModel mTrainingViewModel;

    private ImageView mBookmark;
    private TextView mLine0View;
    private TextView mLine1View;
    private TextView mLine2View;
    private TextView mInfoTitleView;
    private TextView mInfoView;
    private TextView mExampleTitleView;
    private TextView mExampleView;
    private ChipGroup mTagsChipGroup;
    private ImageView mRate0;
    private ImageView mRate1;
    private ImageView mRate2;

    private int mLine1Visibility = VISIBLE;
    private int mLine2Visibility = VISIBLE;
    private int mInfoVisibility = VISIBLE;
    private int mExampleVisibility = VISIBLE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTrainingViewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        return inflater.inflate(R.layout.review_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        TextView countView = rootView.findViewById(review_count);
        mBookmark = rootView.findViewById(bookmark);
        MaterialButton showButton = rootView.findViewById(review_button);
        mLine0View = rootView.findViewById(review_line_0);
        mLine1View = rootView.findViewById(review_line_1);
        mLine2View = rootView.findViewById(review_line_2);
        mInfoTitleView = rootView.findViewById(review_info_title);
        mInfoView = rootView.findViewById(review_info);
        mExampleTitleView = rootView.findViewById(review_example_title);
        mExampleView = rootView.findViewById(review_example);
        mTagsChipGroup = rootView.findViewById(review_tags);
        mRate0 = rootView.findViewById(rate_0);
        mRate1 = rootView.findViewById(rate_1);
        mRate2 = rootView.findViewById(rate_2);

        Bundle args = requireArguments();
        countView.setText(args.getString("count"));

        mItem = mTrainingViewModel.get(args.getInt("position"));

        boolean kanjiPst = isNotBlank(mItem.kanji);
        boolean isJapaneseReviewed = mTrainingViewModel.isJapaneseReview();

        if (isJapaneseReviewed) {
            mLine0View.setText(kanjiPst ? mItem.kanji : mItem.kana);
            mLine1View.setText(kanjiPst ? mItem.kana : null);
            mLine1Visibility = kanjiPst ? VISIBLE : GONE;
            mLine2View.setText(mItem.input);
        } else {
            mLine0View.setText(mItem.input);
            mLine1View.setText(kanjiPst ? mItem.kanji : mItem.kana);
            mLine2View.setText(kanjiPst ? mItem.kana : null);
            mLine2Visibility = kanjiPst ? VISIBLE : GONE;
        }

        if (isNotBlank(mItem.details)) {
            mInfoView.setText(mItem.details);
        } else {
            mInfoVisibility = GONE;
        }

        if (isNotBlank(mItem.example)) {
            mExampleView.setText(mItem.example);
        } else {
            mExampleVisibility = GONE;
        }

        if (isNotBlank(mItem.tags)) {
            Stream.of(mItem.tags.split(", ")).forEach(this::addChipToGroup);
        }

        mBookmark.setOnClickListener(v -> bookmarkItem());
        handleBookmark();

        mRate0.setOnClickListener(v -> setRate(0));
        mRate1.setOnClickListener(v -> setRate(1));
        mRate2.setOnClickListener(v -> setRate(2));
        handleRate();

        showButton.setOnClickListener(this::onShowClicked);
    }

    private void onShowClicked(View button) {
        button.setVisibility(GONE);
        mLine1View.setVisibility(mLine1Visibility);
        mLine2View.setVisibility(mLine2Visibility);
        mInfoTitleView.setVisibility(mInfoVisibility);
        mInfoView.setVisibility(mInfoVisibility);
        mExampleTitleView.setVisibility(mExampleVisibility);
        mExampleView.setVisibility(mExampleVisibility);
        mTagsChipGroup.setVisibility(VISIBLE);
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
