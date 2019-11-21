package fr.frogdevelopment.nihongo.dico.details;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsFragment extends Fragment {

    private DetailsViewModel mDetailsViewModel;

    private Details mRow;

    private ImageView mBookmark;
    private ImageView mRate0;
    private ImageView mRate1;
    private ImageView mRate2;
    private TextView mInputView;
    private TextView mKanjiView;
    private TextView mKanaView;
    private TextView mDetailsTitleView;
    private TextView mDetailsView;
    private TextView mExampleTitleView;
    private TextView mExampleView;
    private TextView mTagsViewTitle;
    private TextView mTagsView;
    private TextView mSuccessView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Integer mId = requireArguments().getInt("item_id");
        mDetailsViewModel = new ViewModelProvider(this).get(DetailsViewModel.class);
        mDetailsViewModel.getById(mId).observe(this, this::fillFields);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        mInputView = rootView.findViewById(R.id.details_word_input);
        mKanjiView = rootView.findViewById(R.id.details_word_kanji);
        mKanaView = rootView.findViewById(R.id.details_word_kana);
        mDetailsTitleView = rootView.findViewById(R.id.details_word_info_title);
        mDetailsView = rootView.findViewById(R.id.details_word_info);
        mExampleTitleView = rootView.findViewById(R.id.details_word_example_title);
        mExampleView = rootView.findViewById(R.id.details_word_example);
        mTagsViewTitle = rootView.findViewById(R.id.details_word_tags_title);
        mTagsView = rootView.findViewById(R.id.details_word_tags);
        mSuccessView = rootView.findViewById(R.id.details_word_success);
        mBookmark = rootView.findViewById(R.id.bookmark);
        mRate0 = rootView.findViewById(R.id.rate_0);
        mRate1 = rootView.findViewById(R.id.rate_1);
        mRate2 = rootView.findViewById(R.id.rate_2);

        mKanjiView.setOnLongClickListener(v -> copyToClipboard("kanji", mKanjiView.getText(), R.string.copy_kanji));
        mKanaView.setOnLongClickListener(v -> copyToClipboard("kana", mKanaView.getText(), R.string.copy_kana));
        mBookmark.setOnClickListener(v -> onFavoriteChanged());
        mRate0.setOnClickListener(v -> onRateChanged(0));
        mRate1.setOnClickListener(v -> onRateChanged(1));
        mRate2.setOnClickListener(v -> onRateChanged(2));
    }

    private boolean copyToClipboard(String label, CharSequence text, int p) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
            Toast.makeText(requireContext(), p, Toast.LENGTH_LONG).show();

            return true;
        }

        return false;
    }

    private void fillFields(Details details) {
        if (details == null) {
            // fixme
            return;
        }

        mRow = details;

        mInputView.setText(mRow.input);

        if (StringUtils.isNotBlank(mRow.kanji)) {
            mKanjiView.setText(mRow.kanji);
            mKanjiView.setVisibility(VISIBLE);
        }

        if (StringUtils.isNotBlank(mRow.kana)) {
            mKanaView.setText(mRow.kana);
        }

        mDetailsView.setText(mRow.details);
        if (StringUtils.isNotBlank(mRow.details)) {
            mDetailsView.setText(mRow.details);
            mDetailsView.setVisibility(VISIBLE);
            mDetailsTitleView.setVisibility(VISIBLE);
        }

        mExampleView.setText(mRow.example);
        if (StringUtils.isNotBlank(mRow.example)) {
            mExampleView.setText(mRow.example);
            mExampleView.setVisibility(VISIBLE);
            mExampleTitleView.setVisibility(VISIBLE);
        }

        if (StringUtils.isNotBlank(mRow.tags)) {
            mTagsView.setText(mRow.tags);
            mTagsView.setVisibility(VISIBLE);
            mTagsViewTitle.setVisibility(VISIBLE);
        } else {
            mTagsView.setText(null);
            mTagsView.setVisibility(GONE);
            mTagsViewTitle.setVisibility(GONE);
        }

        int total = mRow.success + mRow.failed;
        mSuccessView.setText(getString(R.string.details_ratio, (total == 0) ? 0 : ((mRow.success / total) * 100)));

        handleRate();
        handleBookmark();
    }

    private void handleRate() {
        switch (mRow.learned) {
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
        mBookmark.setImageResource(mRow.bookmark ? R.drawable.ic_baseline_bookmark_24 : R.drawable.ic_baseline_bookmark_border_24);
    }

    private void onRateChanged(int rate) {
        mRow.learned = rate;
        handleRate();

        mDetailsViewModel.update(mRow);
    }

    private void onFavoriteChanged() {
        mRow.switchBookmark();
        handleBookmark();

        mDetailsViewModel.update(mRow);

        Toast.makeText(requireContext(), getString(mRow.bookmark ? R.string.bookmark_add : R.string.bookmark_remove), Toast.LENGTH_SHORT).show();
    }
}
