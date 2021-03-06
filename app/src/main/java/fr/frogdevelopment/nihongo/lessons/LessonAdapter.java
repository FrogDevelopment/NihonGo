package fr.frogdevelopment.nihongo.lessons;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import fr.frogdevelopment.nihongo.R;

public class LessonAdapter extends ArrayAdapter<Lesson> {

    private final LayoutInflater mInflater;

    LessonAdapter(Activity context, List<Lesson> objects) {
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

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.lesson_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Lesson item = getItem(position);
        holder.text.setText(item.title);
        if (item.isPresent) {
            holder.text.setTypeface(holder.text.getTypeface(), Typeface.ITALIC);
            holder.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black, 0);
        } else {
            holder.text.setTypeface(holder.text.getTypeface(), Typeface.BOLD);
            holder.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_file_download, 0);
        }

        return convertView;
    }

    private static class ViewHolder {

        private final TextView text;

        private ViewHolder(View view) {
            text = view.findViewById(R.id.lesson_row);
        }
    }
}
