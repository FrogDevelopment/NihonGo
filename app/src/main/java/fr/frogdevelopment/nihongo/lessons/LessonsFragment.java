package fr.frogdevelopment.nihongo.lessons;

import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
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
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

public class LessonsFragment extends ListFragment {

    private static final String LOG_TAG = "NIHON_GO";

    // http://loopj.com/android-async-http/
    private static final AsyncHttpClient CLIENT = new AsyncHttpClient();

    // fixme use external variables
    private static final String BASE_URL = "http://legall.benoit.free.fr/nihon_go/";
    private static final String AVAILABLE_LESSONS_FILE = "available_lessons_2.json";
    private static final String LESSONS_FILE = "lessons.tsv";
    private static final String[] LANGUAGES = {"fr_FR", "en_US"};
    private static final String DEFAULT_LANGUAGE = "en_US";

    private boolean hasInternet = false;

    private LessonAdapter adapter;

    private String myLocale;

    private Set<String> lessonsDownloaded;
    final private Map<String, Lesson> selectedLessons = new HashMap<>();

    private RelativeLayout rootView;
    private WifiReceiver mWiFiReceiver;
    private TestConnectionTask testConnectionTask;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_lessons, container, false);

        myLocale = Locale.getDefault().toString();
        if (!ArrayUtils.contains(LANGUAGES, myLocale)) {
            myLocale = DEFAULT_LANGUAGE;
        }
        // Downloaded Lessons
        String lessonsSaved = PreferencesHelper.getInstance(getActivity()).getString(Preferences.LESSONS);
        lessonsDownloaded = new HashSet<>(Arrays.asList(lessonsSaved.split(";")));

        // Listener for WiFi state
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWiFiReceiver = new WifiReceiver();
        requireActivity().registerReceiver(mWiFiReceiver, intentFilter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        requireActivity().unregisterReceiver(mWiFiReceiver);
        super.onDestroyView();
    }

    private class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info != null && info.isConnectedOrConnecting()) {
                // testing connection to network
                if (testConnectionTask == null) {
                    testConnectionTask = new TestConnectionTask(requireActivity(), LessonsFragment.this::onConnectionResult);
                    testConnectionTask.execute();
                }
            } else {
                Snackbar.make(rootView, "Need internet connection to download lesson", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void onConnectionResult(boolean result) {
        if (result) {
            getAvailableLessons();
        } else {
            getOffLineLessons();
        }

        rootView.findViewById(R.id.lesson_no_connection_test).setVisibility(result ? View.GONE : View.VISIBLE);

        hasInternet = result;

        testConnectionTask = null;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(multiChoiceListener);
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
                Toast.makeText(getActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id) {
        Lesson lesson = adapter.getItem(position);
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
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning)
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
        CLIENT.get(url, new FileAsyncHttpResponseHandler(getActivity()) {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.e(LOG_TAG, "KO", throwable);
                Toast.makeText(getActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final File file) {
                Log.d(LOG_TAG, "File downloaded");
                new DownloadTask().execute(file);
            }
        });
    }

    static class Lesson implements Comparable<Lesson> {
        final String code;
        public final String title;
        boolean isPresent;

        Lesson(String code, String title, boolean isPresent) {
            this.code = code;
            this.title = title;
            this.isPresent = isPresent;
        }

        void setPresent(boolean present) {
            isPresent = present;
        }

        @NonNull
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
            requireActivity().getMenuInflater().inflate(R.menu.lessons_context, menu);
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
            if (item.getItemId() == R.id.lessons_download) {
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

    private class DownloadTask extends AsyncTask<File, String, Boolean> {

        @Override
        protected Boolean doInBackground(File... params) {
            File file = params[0];

            try (Reader in = new FileReader(file)) {
                String col_input = myLocale + "_input";
                String col_details = myLocale + "_details";
                String col_example = myLocale + "_example";
                String tag = getString(R.string.lesson_tag);

                Set<String> selectedCodes = selectedLessons.keySet();
                String previousCode = null;

                NumberFormat numberFormat = new DecimalFormat("00");

                ContentProviderOperation.Builder builder;
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                CSVParser parse = CSVFormat.TDF.withHeader().withSkipHeaderRecord().parse(in);
                for (CSVRecord record : parse.getRecords()) {

                    String code = record.get("tags");

                    if (!selectedCodes.contains(code)) {
                        continue;
                    }

                    // next lesson, save previous one and update UI
                    if (previousCode != null && !code.equals(previousCode)) {
                        requireActivity().getContentResolver().applyBatch(NihonGoContentProvider.AUTHORITY, ops);
                        publishProgress(previousCode);
                        ops.clear();
                    }

                    previousCode = code;

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
                            .withValue(DicoContract.TAGS, tag + " " + numberFormat.format(Integer.valueOf(code)));

                    ops.add(builder.build());
                }

                // save last lesson and update UI
                requireActivity().getContentResolver().applyBatch(NihonGoContentProvider.AUTHORITY, ops);
                publishProgress(previousCode);

            } catch (RemoteException | OperationApplicationException | IOException e) {
                Log.e(LOG_TAG, "Error while fetching data", e);
                return false;
            }

            for (Lesson lesson : selectedLessons.values()) {
                lesson.isPresent = true;
                lessonsDownloaded.add(lesson.code);
            }

            PreferencesHelper.getInstance(getActivity()).saveString(Preferences.LESSONS, StringUtils.join(lessonsDownloaded, ";"));

            return true;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            try {
                String progressValue = progress[0];
                if (StringUtils.isNotBlank(progressValue)) {
                    int index = Integer.parseInt(progressValue);
                    adapter.getItem(index - 1).setPresent(true);
                    adapter.notifyDataSetChanged();
                }
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Error while fetching data", e);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Snackbar.make(requireActivity().findViewById(R.id.lessons_layout), getString(R.string.lesson_download_success), Snackbar.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
            }

            selectedLessons.clear();
        }
    }
}
