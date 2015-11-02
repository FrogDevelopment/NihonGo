package fr.frogdevelopment.nihongo.kana;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import fr.frogdevelopment.nihongo.R;

public class KanaActivity extends Activity {

	private GesturesAdapter  mAdapter;
	private GesturesLoadTask mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kana);

		mAdapter = new GesturesAdapter(this);
		GridView gridview = (GridView) findViewById(R.id.kana_gridview);
		gridview.setAdapter(mAdapter);

		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Kana kana = mAdapter.getItem(position);
				Intent intent = new Intent(KanaActivity.this, DrawingActivity.class);
				intent.putExtra("kana", kana);
				startActivity(intent);
			}
		});

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		loadGestures();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		if (mTask != null && mTask.getStatus() != GesturesLoadTask.Status.FINISHED) {
			mTask.cancel(true);
			mTask = null;
		}
	}

	private void loadGestures() {
		if (mTask != null && mTask.getStatus() != GesturesLoadTask.Status.FINISHED) {
			mTask.cancel(true);
		}
		int kana = getIntent().getIntExtra("kana", 0);
		mTask = (GesturesLoadTask) new GesturesLoadTask().execute(kana);
	}

	private static final int STATUS_SUCCESS    = 0;
	private static final int STATUS_CANCELLED  = 1;

	private class GesturesLoadTask extends AsyncTask<Integer, Kana, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mAdapter.setNotifyOnChange(false);
			mAdapter.clear();
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			if (isCancelled()) return STATUS_CANCELLED;

			if (params[0] == 0) {
				for (Hiragana hiragana : Hiragana.values()) {
					if (isCancelled()) break;
					publishProgress(new Kana(hiragana));
				}
			}else {
				for (Katakana katakana : Katakana.values()) {
					if (isCancelled()) break;

					publishProgress(new Kana(katakana));
				}
			}

			return STATUS_SUCCESS;
		}

		@Override
		protected void onProgressUpdate(Kana... values) {
			super.onProgressUpdate(values);

			final GesturesAdapter adapter = mAdapter;
			adapter.setNotifyOnChange(false);

			for (Kana kana : values) {
				adapter.add(kana);
			}

			adapter.notifyDataSetChanged();
		}
	}

	private class GesturesAdapter extends ArrayAdapter<Kana> {

		private final LayoutInflater mInflater;

		public GesturesAdapter(Context context) {
			super(context, 0);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_kana, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) convertView.findViewById(R.id.kana_image);
				viewHolder.textView = (TextView) convertView.findViewById(R.id.kana_text);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final Kana kana = getItem(position);
			viewHolder.imageView.setImageResource(kana.resource);
			viewHolder.textView.setText(String.valueOf(kana.name));

			return convertView;
		}

		private class ViewHolder {
			private ImageView imageView;
			private TextView  textView;
		}
	}
}
