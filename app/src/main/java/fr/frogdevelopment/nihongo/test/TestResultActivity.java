/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import fr.frogdevelopment.nihongo.R;

public class TestResultActivity extends Activity {

    @Bind(R.id.test_result_list)
    ListView mListView;

    private ResultAdapter adapter;
    private int           successCounter;
    private int           quantity;

    @OnClick(R.id.test_result_ok)
    void back() {
        NavUtils.navigateUpFromSameTask(this);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_result);

        // Show the Up button in the action bar.
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setTitle(R.string.test_results_title);
        }

        ButterKnife.bind(this);

        List<Result> results = getIntent().getParcelableArrayListExtra("results");
        adapter = new ResultAdapter(this, results);
        mListView.setAdapter(adapter);

        successCounter = getIntent().getIntExtra("successCounter", 0);
        quantity = getIntent().getIntExtra("quantity", 0);

        setProgressBarIndeterminateVisibility(false);

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result, menu);

        MenuItem indexMenuItem = menu.findItem(R.id.menu_result);
        String title = successCounter + "/" + quantity;
        indexMenuItem.setTitle(title);

        return true;
    }

    @OnItemClick(R.id.test_result_list)
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListView.setItemChecked(position, true);
        Result item = adapter.getItem(position);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.test_results_title))
                .setMessage(String.format(getString(R.string.test_results_details), item.test, item.answerExpected, item.answerGiven))
                .create()
                .show();
    }

    class ResultAdapter extends ArrayAdapter<Result> {

        private final LayoutInflater mInflater;

        public ResultAdapter(Activity context, List<Result> objects) {
            super(context, R.layout.row_test_result, objects);
            mInflater = context.getLayoutInflater();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
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

        class ResultHolder {
            @Bind(R.id.dico_test_test)
            TextView test;
            @Bind(R.id.dico_test_answer)
            TextView answer;

            public ResultHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }

    }

}
