/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextSwitcher;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Letter;
import fr.frogdevelopment.nihongo.data.Row;

/**
 * @author PxL
 */
public class DicoAdapter extends SimpleCursorAdapter implements SectionIndexer {

    private final LayoutInflater mInflater;
    private final int mResource;

    public DicoAdapter(Activity context, int resource) {
        super(context, resource, null, DicoContract.COLUMNS, null, 0);
        rows = new ArrayList<>();
        mInflater = context.getLayoutInflater();
        mResource = resource;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
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

    public boolean isLetterHeader(int position) {
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
        @InjectView(R.id.row_header)
        TextView textView;

        public LetterViewHolder(View view) {
            ButterKnife.inject(this, view);
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
        @InjectView(R.id.dico_input)
        TextView mInputView;

        @InjectView(R.id.dico_switcher)
        TextSwitcher switcher;
        @InjectView(R.id.dico_switcher_kanji)
        TextView switcherKanji;
        @InjectView(R.id.dico_switcher_kana)
        TextView switcherKana;

        private boolean switchable;
        private boolean tmp;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
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

    private String[] sections;
    private final List<Row> rows;
    private final HashMap<String, Integer> mapPositionByLetter = new LinkedHashMap<>();
    private final HashMap<Integer, Integer> mapSectionByPosition = new LinkedHashMap<>();

    public Cursor swapCursor(Cursor cursor, boolean isSortByLetter) {
        super.swapCursor(cursor);

        rows.clear();
        mapPositionByLetter.clear();
        mapSectionByPosition.clear();

        if (cursor == null) {
            return null;
        }

        Item item;
        String header;
        int position = 0;
        int section = -1;
        while (cursor.moveToNext()) {
            item = new Item(cursor);

            header = isSortByLetter ? item.sort_letter : item.tags;

            // Group numbers together in the scroller
            if (PATTERN_NUMBER.matcher(header).matches()) {
                header = "#";
            }

            // Check if we need to add a header row
            if (!mapPositionByLetter.containsKey(header)) {
                rows.add(new Letter(header));
                mapPositionByLetter.put(header, position);
                section++;
                mapSectionByPosition.put(position, section);
                position++;
            }

            // Add the definition to the list
            rows.add(item);
            mapSectionByPosition.put(position, section);
            position++;
        }

        Set<String> sectionLetters = mapPositionByLetter.keySet();
        sections = new String[sectionLetters.size()];
        sectionLetters.toArray(sections);

        return cursor;
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        return mapPositionByLetter.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return mapSectionByPosition.get(position);
    }

    public List<Item> getItems() {
        List<Item> items = new ArrayList<>();
        for (Row row : rows) {
            if (row instanceof Item) {
                items.add((Item) row);
            }
        }

        return items;
    }

}
