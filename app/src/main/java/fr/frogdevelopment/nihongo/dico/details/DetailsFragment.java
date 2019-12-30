package fr.frogdevelopment.nihongo.dico.details;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.Snackbar.Callback;

import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.edit.EditActivity;

import static android.view.View.GONE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DetailsFragment extends Fragment {

    private DetailsViewModel mDetailsViewModel;

    private Details mItem;

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
    private ChipGroup mTagsChipGroup;
//    private TextView mSuccessView;

    static DetailsFragment newInstance(Bundle extras) {
        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setArguments(extras);
        return detailsFragment;
    }

    public DetailsFragment() {
        super(R.layout.details_fragment);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Integer itemId = requireArguments().getInt("item_id");
        mDetailsViewModel = new ViewModelProvider(requireActivity()).get(DetailsViewModel.class);
        mDetailsViewModel.getById(itemId).observe(getViewLifecycleOwner(), this::fillFields);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        mTagsChipGroup = rootView.findViewById(R.id.details_word_tags);
//        mSuccessView = rootView.findViewById(R.id.details_word_success);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.edit_delete, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                update();
                return true;

            case R.id.action_delete:
                confirmDelete();
                return true;

            default:
                return false;
        }
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
        mItem = details;

        mInputView.setText(mItem.input);

        if (isNotBlank(mItem.kanji)) {
            mKanjiView.setText(mItem.kanji);
        } else {
            mKanjiView.setVisibility(GONE);
        }

        if (isNotBlank(mItem.kana)) {
            mKanaView.setText(mItem.kana);
        }

        mDetailsView.setText(mItem.details);
        if (isNotBlank(mItem.details)) {
            mDetailsView.setText(mItem.details);
        } else {
            mDetailsView.setVisibility(GONE);
            mDetailsTitleView.setVisibility(GONE);
        }

        mExampleView.setText(mItem.example);
        if (isNotBlank(mItem.example)) {
            mExampleView.setText(mItem.example);
        } else {
            mExampleView.setVisibility(GONE);
            mExampleTitleView.setVisibility(GONE);
        }

        if (isNotBlank(mItem.tags)) {
            Stream.of(mItem.tags.split(", ")).forEach(this::addChipToGroup);
        }

//        int total = mItem.success + mItem.failed;
//        mSuccessView.setText(getString(R.string.details_ratio, (total == 0) ? 0 : ((mItem.success / total) * 100)));

        handleRate();
        handleBookmark();
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

    private void onRateChanged(int rate) {
        mItem.learned = rate;
        handleRate();

        mDetailsViewModel.update(mItem);
    }

    private void onFavoriteChanged() {
        mItem.switchBookmark();
        handleBookmark();

        mDetailsViewModel.update(mItem);

        Toast.makeText(requireContext(), getString(mItem.bookmark ? R.string.bookmark_add : R.string.bookmark_remove), Toast.LENGTH_SHORT).show();
    }

    private void update() {
        Intent intent = new Intent(requireContext(), EditActivity.class);
        intent.putExtra("item_id", mItem.id);

        startActivity(intent);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> delete())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void delete() {
        Snackbar.make(requireView(), R.string.delete_done, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_cancel, v -> {
                    // keep empty action to display action ...
                })
                .addCallback(new Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            mDetailsViewModel.delete(mItem);
                        }
                    }
                })
                .show();
    }
}
