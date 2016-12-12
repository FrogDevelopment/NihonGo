/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.nihongo.R;

public class TestResultActivity extends AppCompatActivity {

    private ListView mListView;

    private ResultAdapter adapter;

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_result);

        Switch quantitySwitch = (Switch) findViewById(R.id.test_result_quantity_switch);
        quantitySwitch.setOnCheckedChangeListener((compoundButton, b) -> adapter.getFilter().filter(Boolean.toString(b)));

        TextView mQuantity = (TextView) findViewById(R.id.test_result_quantity);
        mListView = (ListView) findViewById(R.id.test_result_list);
        mListView.setOnItemClickListener((adapterView, view, i, l) -> onItemClick(i));

        List<Result> results = getIntent().getParcelableArrayListExtra("results");
        adapter = new ResultAdapter(this, results);
        mListView.setAdapter(adapter);

        int successCounter = getIntent().getIntExtra("successCounter", 0);
        int quantity = getIntent().getIntExtra("quantity", 0);
        mQuantity.setText(successCounter + "/" + quantity);

        Button buttonOK = (Button) findViewById(R.id.test_result_ok);
        buttonOK.setOnClickListener(v -> onBackPressed());
    }

    private void onItemClick(int position) {
        mListView.setItemChecked(position, true);
        Result item = adapter.getItem(position);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.test_results_title))
                .setMessage(String.format(getString(R.string.test_results_details), item.test, item.answerExpected, item.answerGiven))
                .create()
                .show();
    }

    private class ResultAdapter extends ArrayAdapter<Result> implements Filterable {

        private final Filter mFilter = new SuccessFilter();

        private final Object mLock = new Object();

        private List<Result> mObjects;
        private List<Result> mOriginalValues;
        private final LayoutInflater mInflater;

        ResultAdapter(Activity context, List<Result> objects) {
            super(context, R.layout.row_test_result);
            mObjects = objects;
            mInflater = context.getLayoutInflater();
        }

        @NonNull
        @Override
        public View getView(int position, View view, @NonNull ViewGroup parent) {
            ResultHolder holder;
            Result result = getItem(position);

            if (view == null) {
                view = mInflater.inflate(R.layout.row_test_result, parent, false);
                holder = new ResultHolder(view);

                view.setTag(holder);
            } else {
                holder = (ResultHolder) view.getTag();
            }

            holder.test.setText(result.test);
            holder.answer.setText(result.answerExpected);
            holder.ratio.setText(result.nbSuccess + "/" + result.nbFailed);

            if (result.success) {
                holder.answer.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
//                holder.answer.setCompoundDrawablesWithIntrinsicBounds(0,0,0,R.drawable.test_ok);
                view.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
            } else if (result.almost) {
                holder.answer.setTextColor(getContext().getResources().getColor(android.R.color.holo_orange_dark));
            } else {
//                holder.answer.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
//                holder.answer.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
                holder.answer.setTextColor(getContext().getResources().getColor(android.R.color.black));
                view.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            return view;
        }

        private class ResultHolder {
            private final TextView test;
            private final TextView answer;
            private final TextView ratio;

            private ResultHolder(View view) {
                test = (TextView) view.findViewById(R.id.dico_test_test);
                answer = (TextView) view.findViewById(R.id.dico_test_answer);
                ratio = (TextView) view.findViewById(R.id.dico_test_ratio);
            }
        }

        @Override
        public int getCount() {
            return mObjects.size();
        }

        @Override
        public int getPosition(@Nullable Result item) {
            return mObjects.indexOf(item);
        }

        @Nullable
        @Override
        public Result getItem(int position) {
            return mObjects.get(position);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return mFilter;
        }

        private class SuccessFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();

                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new ArrayList<>(mObjects);
                    }
                }
                if (StringUtils.isBlank(constraint)) {
                    final ArrayList<Result> list;
                    synchronized (mLock) {
                        list = new ArrayList<>(mOriginalValues);
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    final boolean showOnlyFails = Boolean.valueOf(constraint.toString());

                    final ArrayList<Result> values;
                    synchronized (mLock) {
                        values = new ArrayList<>(mOriginalValues);
                    }

                    final int count = values.size();
                    final ArrayList<Result> newValues = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        final Result value = values.get(i);

                        if (!showOnlyFails || !value.success) {
                            newValues.add(value);
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mObjects = (List<Result>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }

}
