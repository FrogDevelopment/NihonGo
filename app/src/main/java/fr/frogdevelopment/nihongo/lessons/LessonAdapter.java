/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.lessons;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;

public class LessonAdapter extends ArrayAdapter<LessonsFragment.Lesson> {

    private final LayoutInflater mInflater;

    public LessonAdapter(Activity context, List<LessonsFragment.Lesson> objects) {
        super(context, 0, objects);

        mInflater = context.getLayoutInflater();
    }

    private boolean enabled = true;

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled(int position) {
        return enabled;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return enabled;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LessonsFragment.Lesson item = getItem(position);
        String title = item.title;
        if (item.isPresent) {
            title += getContext().getString(R.string.lesson_present);
            holder.text.setTypeface(holder.text.getTypeface(), Typeface.ITALIC);
        } else {
            holder.text.setTypeface(holder.text.getTypeface(), Typeface.BOLD);
        }
        holder.text.setText(title);

        return convertView;
    }

    class ViewHolder {

        @Bind(android.R.id.text1)
        TextView text;

        private ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
