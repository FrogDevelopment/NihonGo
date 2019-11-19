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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Letter;
import fr.frogdevelopment.nihongo.data.Row;

public class DicoAdapter extends BaseAdapter implements SectionIndexer {

    private final LayoutInflater mInflater;
    private final int mResource;

    DicoAdapter(Activity context, int resource) {
        mRows = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mResource = resource;
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
        if (getItem(position) instanceof Letter) {
            return 1;
        } else {
            return 0;
        }
    }

    boolean isLetterHeader(int position) {
        return getItemViewType(position) == 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (isLetterHeader(position)) { // Letter
            view = getLetterView(position, convertView, parent, view);
        } else { // Definiton
            view = getDataView(position, convertView, parent);
        }

        return view;
    }

    private View getLetterView(int position, View convertView, ViewGroup parent, View view) {
        LetterViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.row_header, parent, false);
            holder = new LetterViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (LetterViewHolder) view.getTag();
        }
        Letter letter = (Letter) getItem(position);
        holder.textView.setText(letter.text);
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
            view = mInflater.inflate(mResource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        Item item = (Item) getItem(position);
        holder.mInputView.setText(item.input);
        holder.switcher.setDisplayedChild(0);
        if (StringUtils.isNoneBlank(item.kanji)) {
            holder.switchable = true;
            holder.switcherKanji.setText(item.kanji);
            holder.switcherKana.setText(item.kana);
        } else {
            holder.switchable = false;
            holder.switcherKanji.setText(item.kana);
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

    private static final Pattern PATTERN_NUMBER = Pattern.compile("[0-9]");

    private String[] mSections;
    private boolean mIsSortByLetter = true;
    private final List<Row> mRows;
    private final HashMap<String, Integer> mapPositionByLetter = new LinkedHashMap<>();
    private final HashMap<Integer, Integer> mapSectionByPosition = new LinkedHashMap<>();

    void setSortByLetter(boolean isSortByLetter) {
        mIsSortByLetter = isSortByLetter;
    }

    void setRows(@Nullable List<Item> items) {
        mRows.clear();
        mapPositionByLetter.clear();
        mapSectionByPosition.clear();

        if (items == null) {
            notifyDataSetChanged();
            return;
        }

        String header;
        int position = 0;
        int section = -1;
        for (Item item : items) {
            header = mIsSortByLetter ? item.sort_letter : item.tags;

            // Group numbers together in the scroller
            if (PATTERN_NUMBER.matcher(header).matches()) {
                header = "#";
            }

            // Check if we need to add a header row
            if (!mapPositionByLetter.containsKey(header)) {
                mRows.add(new Letter(header));
                mapPositionByLetter.put(header, position);
                section++;
                mapSectionByPosition.put(position, section);
                position++;
            }

            // Add the definition to the list
            mRows.add(item);
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

    List<Item> getItems() {
        List<Item> items = new ArrayList<>();
        for (Row row : mRows) {
            if (row instanceof Item) {
                items.add((Item) row);
            }
        }

        return items;
    }

}
