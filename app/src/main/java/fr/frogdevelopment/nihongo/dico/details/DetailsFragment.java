package fr.frogdevelopment.nihongo.dico.details;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
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

import org.apache.commons.lang3.StringUtils;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.model.Details;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsFragment extends Fragment {

    private Details mRow;

    private ImageView mBookmark;
    private ImageView mRate0;
    private ImageView mRate1;
    private ImageView mRate2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        TextView inputView = rootView.findViewById(R.id.details_word_input);
        TextView kanjiView = rootView.findViewById(R.id.details_word_kanji);
        TextView kanaView = rootView.findViewById(R.id.details_word_kana);
        TextView detailsTitleView = rootView.findViewById(R.id.details_word_info_title);
        TextView detailsView = rootView.findViewById(R.id.details_word_info);
        TextView exampleTitleView = rootView.findViewById(R.id.details_word_example_title);
        TextView exampleView = rootView.findViewById(R.id.details_word_example);
        TextView tagsViewTitle = rootView.findViewById(R.id.details_word_tags_title);
        TextView tagsView = rootView.findViewById(R.id.details_word_tags);
        TextView successView = rootView.findViewById(R.id.details_word_success);

        Bundle args = requireArguments();

        mRow = args.getParcelable("item");

        if (mRow == null) {
            return;
        }

        inputView.setText(mRow.input);

        if (StringUtils.isNotBlank(mRow.kanji)) {
            kanjiView.setText(mRow.kanji);
            kanjiView.setVisibility(VISIBLE);
        }

        if (StringUtils.isNotBlank(mRow.kana)) {
            kanaView.setText(mRow.kana);
        }

        detailsView.setText(mRow.details);
        if (StringUtils.isNotBlank(mRow.details)) {
            detailsView.setText(mRow.details);
            detailsView.setVisibility(VISIBLE);
            detailsTitleView.setVisibility(VISIBLE);
        }

        exampleView.setText(mRow.example);
        if (StringUtils.isNotBlank(mRow.example)) {
            exampleView.setText(mRow.example);
            exampleView.setVisibility(VISIBLE);
            exampleTitleView.setVisibility(VISIBLE);
        }

        if (StringUtils.isNotBlank(mRow.tags)) {
            tagsView.setText(mRow.tags);
            tagsView.setVisibility(VISIBLE);
            tagsViewTitle.setVisibility(VISIBLE);
        } else {
            tagsView.setText(null);
            tagsView.setVisibility(GONE);
            tagsViewTitle.setVisibility(GONE);
        }

        int total = mRow.success + mRow.failed;
        successView.setText(getString(R.string.details_ratio, (total == 0) ? 0 : ((mRow.success / total) * 100)));

        kanjiView.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("kanji", kanjiView.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }

            Toast.makeText(getActivity(), R.string.copy_kanji, Toast.LENGTH_LONG).show();
            return true;
        });

        kanaView.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("kana", kanaView.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }

            Toast.makeText(getActivity(), R.string.copy_kana, Toast.LENGTH_LONG).show();
            return true;
        });

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

    private void setRate(int rate) {
        mRow.learned = rate;
        handleRate();

        final ContentValues values = new ContentValues();
        values.put(DicoContract.LEARNED, mRow.learned);
        updateItem(values);
    }

    private void bookmarkItem() {
        mRow.switchBookmark();
        handleBookmark();

        final ContentValues values = new ContentValues();
        values.put(DicoContract.BOOKMARK, mRow.bookmark);
        updateItem(values);

        Toast.makeText(getActivity(), getString(mRow.bookmark ? R.string.bookmark_add : R.string.bookmark_remove), Toast.LENGTH_SHORT).show();
    }

    private void updateItem(ContentValues values) {
        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {String.valueOf(mRow.id)};

        requireActivity().getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
    }

}
