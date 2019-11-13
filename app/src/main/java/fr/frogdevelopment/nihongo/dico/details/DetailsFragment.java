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
import fr.frogdevelopment.nihongo.data.Item;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsFragment extends Fragment {

    private Item mItem;

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

        mItem = args.getParcelable("item");

        if (mItem == null) {
            return;
        }

        inputView.setText(mItem.input);

        if (StringUtils.isNotBlank(mItem.kanji)) {
            kanjiView.setText(mItem.kanji);
            kanjiView.setVisibility(VISIBLE);
        }

        if (StringUtils.isNotBlank(mItem.kana)) {
            kanaView.setText(mItem.kana);
        }

        detailsView.setText(mItem.details);
        if (StringUtils.isNotBlank(mItem.details)) {
            detailsView.setText(mItem.details);
            detailsView.setVisibility(VISIBLE);
            detailsTitleView.setVisibility(VISIBLE);
        }

        exampleView.setText(mItem.example);
        if (StringUtils.isNotBlank(mItem.example)) {
            exampleView.setText(mItem.example);
            exampleView.setVisibility(VISIBLE);
            exampleTitleView.setVisibility(VISIBLE);
        }

        if (StringUtils.isNotBlank(mItem.tags)) {
            tagsView.setText(mItem.tags);
            tagsView.setVisibility(VISIBLE);
            tagsViewTitle.setVisibility(VISIBLE);
        } else {
            tagsView.setText(null);
            tagsView.setVisibility(GONE);
            tagsViewTitle.setVisibility(GONE);
        }

        int total = mItem.success + mItem.failed;
        successView.setText(getString(R.string.details_ratio, (total == 0) ? 0 : ((mItem.success / total) * 100)));

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
        switch (mItem.learned) {
            case 1:
                mRate0.setImageResource(R.drawable.ic_star_black_24dp);
                mRate1.setImageResource(R.drawable.ic_star_black_24dp);
                mRate2.setImageResource(R.drawable.ic_star_border_black_24dp);

                break;

            case 2:
                mRate0.setImageResource(R.drawable.ic_star_black_24dp);
                mRate1.setImageResource(R.drawable.ic_star_black_24dp);
                mRate2.setImageResource(R.drawable.ic_star_black_24dp);

                break;

            case 0:
            default:
                mRate0.setImageResource(R.drawable.ic_star_black_24dp);
                mRate1.setImageResource(R.drawable.ic_star_border_black_24dp);
                mRate2.setImageResource(R.drawable.ic_star_border_black_24dp);

                break;
        }
    }

    private void handleBookmark() {
        mBookmark.setImageResource(mItem.bookmark ? R.drawable.ic_bookmark_on : R.drawable.ic_bookmark_off);
    }

    private void setRate(int rate) {
        mItem.learned = rate;
        handleRate();

        final ContentValues values = new ContentValues();
        values.put(DicoContract.LEARNED, mItem.learned);
        updateItem(values);

        int rateName;
        switch (rate) {
            case 1:
                rateName = R.string.rate_1;
                break;
            case 2:
                rateName = R.string.rate_2;
                break;
            case 0:
            default:
                rateName = R.string.rate_0;
                break;
        }

        Toast.makeText(getActivity(), getString(R.string.rate_done, getString(rateName)), Toast.LENGTH_SHORT).show();
    }

    private void bookmarkItem() {
        mItem.switchBookmark();
        handleBookmark();

        final ContentValues values = new ContentValues();
        values.put(DicoContract.BOOKMARK, mItem.bookmark);
        updateItem(values);

        Toast.makeText(getActivity(), getString(mItem.bookmark ? R.string.bookmark_add : R.string.bookmark_remove), Toast.LENGTH_SHORT).show();
    }

    private void updateItem(ContentValues values) {
        final String where = DicoContract._ID + "=?";
        final String[] selectionArgs = {mItem.id};

        requireActivity().getContentResolver().update(NihonGoContentProvider.URI_WORD, values, where, selectionArgs);
    }

}
