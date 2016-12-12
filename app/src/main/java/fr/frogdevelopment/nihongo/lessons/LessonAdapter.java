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
            convertView = mInflater.inflate(R.layout.row_lesson, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LessonsFragment.Lesson item = getItem(position);
        holder.text.setText(item.title);
        if (item.isPresent) {
            holder.text.setTypeface(holder.text.getTypeface(), Typeface.ITALIC);
            holder.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black, 0);
        } else {
            holder.text.setTypeface(holder.text.getTypeface(), Typeface.BOLD);
            holder.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_file_download_black, 0);
        }

        return convertView;
    }

    private class ViewHolder {

        private final TextView text;

        private ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.lesson_row);
        }
    }
}
