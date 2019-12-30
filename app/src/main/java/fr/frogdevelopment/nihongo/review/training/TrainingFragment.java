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

    private ImageView mBookmark;
    private ImageView mRate0;
    private ImageView mRate1;
    private ImageView mRate2;
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
        MaterialButton button = rootView.findViewById(review_button);
        TextView line0View = rootView.findViewById(review_line_0);
        TextView lineKanaView = rootView.findViewById(review_line_1);
        TextView line2View = rootView.findViewById(review_line_2);
        TextView infoTitleView = rootView.findViewById(review_info_title);
        TextView infoView = rootView.findViewById(review_info);
        TextView exampleTitleView = rootView.findViewById(review_example_title);
        TextView exampleView = rootView.findViewById(review_example);
        mTagsChipGroup = rootView.findViewById(review_tags);

        Bundle args = requireArguments();
        countView.setText(args.getString("count"));

        mItem = mTrainingViewModel.get(args.getInt("position"));

        boolean kanjiPst = isNotBlank(mItem.kanji);
        boolean isJapaneseReviewed = mTrainingViewModel.isJapaneseReview();

        String line0;
        String lineKana;
        String line2;
        int line1Visibility = kanjiPst ? VISIBLE : GONE;
        if (isJapaneseReviewed) {
            line0 = kanjiPst ? mItem.kanji : mItem.kana;
            lineKana = kanjiPst ? mItem.kana : null;
            line2 = mItem.input;
        } else {
            line0 = mItem.input;
            lineKana = kanjiPst ? mItem.kanji : mItem.kana;
            line2 = kanjiPst ? mItem.kana : null;
        }
        line0View.setText(line0);
        lineKanaView.setText(lineKana);
        line2View.setText(line2);

        if (isNotBlank(mItem.details)) {
            infoView.setText(mItem.details);
        }

        if (isNotBlank(mItem.example)) {
            exampleView.setText(mItem.example);
        }

        if (isNotBlank(mItem.tags)) {
            Stream.of(mItem.tags.split(", ")).forEach(this::addChipToGroup);
        }

        mBookmark = rootView.findViewById(bookmark);
        mBookmark.setOnClickListener(v -> bookmarkItem());
        handleBookmark();

        mRate0 = rootView.findViewById(rate_0);
        mRate0.setOnClickListener(v -> setRate(0));
        mRate1 = rootView.findViewById(rate_1);
        mRate1.setOnClickListener(v -> setRate(1));
        mRate2 = rootView.findViewById(rate_2);
        mRate2.setOnClickListener(v -> setRate(2));
        handleRate();

        button.setOnClickListener(v -> {
            button.setVisibility(GONE);
            lineKanaView.setVisibility(line1Visibility);
            line2View.setVisibility(VISIBLE);
            infoTitleView.setVisibility(VISIBLE);
            infoView.setVisibility(VISIBLE);
            exampleTitleView.setVisibility(VISIBLE);
            exampleView.setVisibility(VISIBLE);
        });
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
