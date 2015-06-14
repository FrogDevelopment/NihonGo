/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.drawer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.frogdevelopment.nihongo.R;

public class DrawerItemAdapter extends ArrayAdapter<DrawerItemAdapter.MenuItem> {

    private final LayoutInflater mInflater;

    public DrawerItemAdapter(Activity context) {
        super(context, 0);
        mInflater = context.getLayoutInflater();
    }

    public void addSeparator() {
        add(new MenuItem());
    }

    public void addItem(int title, int index) {
        add(new MenuItem(title, index));
    }

    @Override
    public boolean isEnabled(int position) {
        return !getItem(position).isSeparator;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isSeparator ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MenuItem item = getItem(position);

        if (item.isSeparator) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.drawer_list_separator, parent, false);
            }
        } else {
            ItemViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ItemViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
            }

            holder.title.setText(item.title);
        }

        if (convertView.isSelected()) {
            convertView.setBackgroundResource(R.drawable.background_drawer);
        } else {
            convertView.setBackgroundResource(android.R.color.transparent);

        }

        return convertView;
    }

    static class ItemViewHolder {

        @InjectView(android.R.id.text1)
        TextView title;

        public ItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public int getIndex(int position) {
        return getItem(position).index;
    }

    static class MenuItem {

        final int title;
        final boolean isSeparator;
        final int index;

        private MenuItem() {
            super();
            this.title = -1;
            this.isSeparator = true;
            this.index = 0;
        }

        private MenuItem(int title, int index) {
            super();
            this.title = title;
            this.isSeparator = false;
            this.index = index;
        }

    }
}
