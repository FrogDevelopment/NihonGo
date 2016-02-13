/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.lessons;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import fr.frogdevelopment.nihongo.MainActivity;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.TestConnectionTask;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class LessonsFragment extends ListFragment {

	private static final String LOG_TAG = "NIHON_GO";

	// http://loopj.com/android-async-http/
	private static final AsyncHttpClient CLIENT = new AsyncHttpClient();

	// fixme utiliser variable externe
	private static final String   BASE_URL               = "http://legall.benoit.free.fr/nihon_go/";
	private static final String   AVAILABLE_LESSONS_FILE = "available_lessons_2.json";
	private static final String   LESSONS_FILE           = "lessons.tsv";
	private static final String[] LANGUAGES              = {"fr_FR", "en_US"};
	private static final String   DEFAULT_LANGUAGE       = "en_US";

	private boolean hasInternet = false;

	private LessonAdapter adapter;

	private String myLocale;

	private Set<String> lessonsDownloaded;
	final private Map<String, Lesson> selectedLessons = new HashMap<>();

	public LessonsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_lessons, container, false);

		myLocale = Locale.getDefault().toString();
		if (!ArrayUtils.contains(LANGUAGES, myLocale)) {
			myLocale = DEFAULT_LANGUAGE;
		}
		getDownloadedLessons();

		inProgress(true);
		new TestConnectionTask(getContext(), result -> {
			if (result) {
				getAvailableLessons();
			} else {
				getOffLineLessons();
			}

			hasInternet = result;
		}).execute();

		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(multiChoiceListener);
	}

	private void inProgress(boolean wait) {
		((MainActivity) getActivity()).showLoading(wait);
//		getListView().setEnabled(!wait);
	}

	private void getDownloadedLessons() {
		String lessonsSaved = PreferencesHelper.getInstance(getContext()).getString(Preferences.LESSONS);
		lessonsDownloaded = new HashSet<>(Arrays.asList(lessonsSaved.split(";")));
	}

	private void getOffLineLessons() {
		String tag = getString(R.string.lesson_tag);

		List<Lesson> lessons = new ArrayList<>();
		for (String code : lessonsDownloaded) {
			lessons.add(new Lesson(code, tag + " " + code, true));
		}

		// update the UI
		setLessons(lessons);
	}

	private void getAvailableLessons() {

		String url = BASE_URL + AVAILABLE_LESSONS_FILE;
		Log.d(LOG_TAG, "Calling : " + url);
		CLIENT.get(url, new JsonHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray response) {
				Log.e(LOG_TAG, "KO", throwable);
				Toast.makeText(getContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
				inProgress(false);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					JSONArray jsonArray = response.getJSONArray(myLocale);
					String tag = getString(R.string.lesson_tag);

					List<Lesson> lessonsAvailable = new ArrayList<>();
					for (int index = 0, nbItem = jsonArray.length(); index < nbItem; index++) {
						String code = jsonArray.getString(index);
						lessonsAvailable.add(new Lesson(code, tag + " " + code, lessonsDownloaded.contains(code)));
					}
					// update the UI
					setLessons(lessonsAvailable);

				} catch (JSONException e) {
					Log.e(LOG_TAG, "Data Fetch KO", e);
					Toast.makeText(getContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
					inProgress(false);
				}
			}
		});
	}

	private void setLessons(List<Lesson> lessonsAvailable) {
		// update the UI
		Collections.sort(lessonsAvailable);

		adapter = new LessonAdapter(getActivity(), lessonsAvailable);
		setListAdapter(adapter);
		adapter.setEnabled(hasInternet);

		inProgress(false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Lesson lesson = adapter.getItem(position);
		selectedLessons.put(lesson.code, lesson);
		checkBeforeDownloadLessons();
	}

	private void checkBeforeDownloadLessons() {

		if (selectedLessons.isEmpty()) {
			return;
		}

		boolean onPresent = false;
		for (Lesson lesson : selectedLessons.values()) {
			if (lesson.isPresent) {
				onPresent = true;
				break;
			}
		}

		if (onPresent) {
			new AlertDialog.Builder(getContext())
					.setIcon(R.drawable.ic_warning_black)
					.setTitle(R.string.lesson_already_present)
					.setMessage(R.string.lesson_continue)
					.setPositiveButton(getString(R.string.yes), (dialog, which) -> downloadLessons())
					.setNegativeButton(getString(R.string.no), null)
					.show();
		} else {
			downloadLessons();
		}
	}

	private void downloadLessons() {

		final String url = BASE_URL + LESSONS_FILE;
		Log.d(LOG_TAG, "Calling : " + url);
		inProgress(true);
		CLIENT.get(url, new FileAsyncHttpResponseHandler(getContext()) {

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
				Log.e(LOG_TAG, "KO", throwable);
				Toast.makeText(getContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
				inProgress(false);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, final File file) {
				Log.d(LOG_TAG, "File downloaded");
				new DownloadTask().execute(file);
			}
		});
	}

	static class Lesson implements Comparable<Lesson> {
		public final String  code;
		public final String  title;
		public       boolean isPresent;

		public Lesson(String code, String title, boolean isPresent) {
			this.code = code;
			this.title = title;
			this.isPresent = isPresent;
		}

		@Override
		public String toString() {
			return title;
		}

		@Override
		public int compareTo(@NonNull Lesson another) {
			return code.compareTo(another.code);
		}
	}

	private AbsListView.MultiChoiceModeListener multiChoiceListener = new AbsListView.MultiChoiceModeListener() {

		private int rowSelectedNumber = 0;

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getActivity().getMenuInflater().inflate(R.menu.lessons_context, menu);
			rowSelectedNumber = 0;
			selectedLessons.clear();
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.lessons_download:
					actionMode.finish();
					checkBeforeDownloadLessons();
					return true;
			}

			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {

		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
			Lesson lesson = adapter.getItem(position);
			if (checked) {
				rowSelectedNumber++;
				selectedLessons.put(lesson.code, lesson);
			} else {
				selectedLessons.remove(lesson.code);
				rowSelectedNumber--;
			}
			mode.setTitle(getResources().getQuantityString(R.plurals.action_lesson, rowSelectedNumber, rowSelectedNumber));
			mode.invalidate();
		}
	};

	private class DownloadTask extends AsyncTask<File, Void, Boolean> {

		@Override
		protected Boolean doInBackground(File... params) {
			File file = params[0];

			try (Reader in = new FileReader(file)) {
				String col_input = myLocale + "_input";
				String col_details = myLocale + "_details";
				String col_example = myLocale + "_example";
				String tag = getString(R.string.lesson_tag);

				Set<String> selectedCodes = selectedLessons.keySet();

				ContentProviderOperation.Builder builder;
				ArrayList<ContentProviderOperation> ops = new ArrayList<>();
				CSVParser parse = CSVFormat.TDF.withHeader().withSkipHeaderRecord().parse(in);
				for (CSVRecord record : parse.getRecords()) {

					String code = record.get("tags");

					if (!selectedCodes.contains(code)) {
						continue;
					}

					String input = StringUtils.capitalize(record.get(col_input));
					if (StringUtils.isBlank(input)) {
						continue;
					}

					// Normalizer.normalize(source, Normalizer.Form.NFD) renvoi une chaine unicode décomposé.
					// C'est à dire que les caractères accentués seront décomposé en deux caractères (par exemple "à" se transformera en "a`").
					// Le replaceAll("[\u0300-\u036F]", "") supprimera tous les caractères unicode allant de u0300 à u036F,
					// c'est à dire la plage de code des diacritiques (les accents qu'on a décomposé ci-dessus donc).
					String sortLetter = Normalizer.normalize(input.substring(0, 1), Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");

					builder = ContentProviderOperation.newInsert(NihonGoContentProvider.URI_WORD)
							.withValue(DicoContract.SORT_LETTER, sortLetter)
							.withValue(DicoContract.INPUT, input)
							.withValue(DicoContract.KANJI, record.get("kanji"))
							.withValue(DicoContract.KANA, record.get("kana"))
							.withValue(DicoContract.DETAILS, record.get(col_details))
							.withValue(DicoContract.EXAMPLE, record.get(col_example))
							.withValue(DicoContract.TYPE, record.get("type"))
							.withValue(DicoContract.TAGS, tag + " " + record.get("tags"));

					ops.add(builder.build());
				}

				getContext().getContentResolver().applyBatch(NihonGoContentProvider.AUTHORITY, ops);

			} catch (RemoteException | OperationApplicationException | IOException e) {
				Log.e(LOG_TAG, "Error while fetching data", e);
				return false;
			}

			for (Lesson lesson : selectedLessons.values()) {
				lesson.isPresent = true;
				lessonsDownloaded.add(lesson.code);
			}

			PreferencesHelper.getInstance(getContext()).saveString(Preferences.LESSONS, StringUtils.join(lessonsDownloaded, ";"));

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				getListView().invalidateViews();
				Snackbar.make(getActivity().findViewById(R.id.lessons_layout), getString(R.string.lesson_download_success, selectedLessons.size()), Snackbar.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
			}

			selectedLessons.clear();
			inProgress(false);
		}
	}
}
