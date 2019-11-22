package fr.frogdevelopment.nihongo.dico;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Row;

import static fr.frogdevelopment.nihongo.R.layout.row_entry;
import static fr.frogdevelopment.nihongo.R.layout.row_header;
import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

public class DicoAdapter extends BaseAdapter implements SectionIndexer {

    private final LayoutInflater mInflater;

    DicoAdapter(Activity context) {
        mRows = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mRows.size();
    }

    @Override
    public Row getItem(int position) {
        return mRows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).id == null) {
            return 1;
        } else {
            return 0;
        }
    }

    boolean isHeader(int position) {
        return getItemViewType(position) == 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (isHeader(position)) {
            view = getHeaderView(position, convertView, parent, view);
        } else {
            view = getDataView(position, convertView, parent);
        }

        return view;
    }

    private View getHeaderView(int position, View convertView, ViewGroup parent, View view) {
        LetterViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(row_header, parent, false);
            holder = new LetterViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (LetterViewHolder) view.getTag();
        }
        Row header = getItem(position);
        holder.textView.setText(header.input);
        return view;
    }

    static class LetterViewHolder {
        TextView textView;

        LetterViewHolder(View view) {
            textView = view.findViewById(R.id.row_header);
        }
    }

    private View getDataView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(row_entry, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        Row row = getItem(position);
        holder.mInputView.setText(row.input);
        holder.switcher.setDisplayedChild(0);
        if (isNoneBlank(row.kanji)) {
            holder.switchable = true;
            holder.switcherKanji.setText(row.kanji);
            holder.switcherKana.setText(row.kana);
        } else {
            holder.switchable = false;
            holder.switcherKanji.setText(row.kana);
        }

        return view;
    }

    static class ViewHolder {
        TextView mInputView;
        TextSwitcher switcher;
        TextView switcherKanji;
        TextView switcherKana;

        private boolean switchable;
        private boolean tmp;

        ViewHolder(View view) {
            mInputView = view.findViewById(R.id.dico_input);
            switcher = view.findViewById(R.id.dico_switcher);
            switcherKanji = view.findViewById(R.id.dico_switcher_kanji);
            switcherKana = view.findViewById(R.id.dico_switcher_kana);
        }

        void switchKanjiKana() {
            if (!switchable) {
                return;
            }

            if (tmp)
                switcher.showNext();
            else
                switcher.showPrevious();

            tmp = !tmp;
        }
    }

    private String[] mSections;
    private final List<Row> mRows;
    private final HashMap<String, Integer> mapPositionByLetter = new LinkedHashMap<>();
    private final HashMap<Integer, Integer> mapSectionByPosition = new LinkedHashMap<>();

    List<Row> getRows() {
        return mRows.stream()
                .filter(row -> row.id != null)
                .collect(Collectors.toList());
    }

    void setRows(@Nullable List<Row> rows) {
        mRows.clear();
        mapPositionByLetter.clear();
        mapSectionByPosition.clear();

        if (rows == null) {
            notifyDataSetChanged();
            return;
        }

        String sort_letter;
        int position = 0;
        int section = -1;
        for (Row row : rows) {
            sort_letter = row.sort_letter;

            // Group numbers together in the scroller
            if (NumberUtils.isParsable(sort_letter)) {
                sort_letter = "#";
            }

            // Group non alpha
            if (!isAlpha(sort_letter)) {
                sort_letter = "@";
            }

            // Check if we need to add a header row
            if (!mapPositionByLetter.containsKey(sort_letter)) {
                Row header = new Row();
                header.id = null;
                header.input = sort_letter;
                mRows.add(header);
                mapPositionByLetter.put(sort_letter, position);
                section++;
                mapSectionByPosition.put(position, section);
                position++;
            }

            // Add the definition to the list
            mRows.add(row);
            mapSectionByPosition.put(position, section);
            position++;
        }

        Set<String> sectionLetters = mapPositionByLetter.keySet();
        mSections = new String[sectionLetters.size()];
        sectionLetters.toArray(mSections);

        notifyDataSetChanged();
    }

    @Override
    public Object[] getSections() {
        return mSections;
    }

    @Override
    public int getPositionForSection(int section) {
        return mapPositionByLetter.get(mSections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return mapSectionByPosition.get(position);
    }

}
