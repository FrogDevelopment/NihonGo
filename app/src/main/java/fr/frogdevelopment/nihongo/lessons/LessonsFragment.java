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
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
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
import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import fr.frogdevelopment.nihongo.ConnectionHelper;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

// todo : proposer rafraîchissement de la vue
public class LessonsFragment extends ListFragment {

    private static final String LOG_TAG = "NIHON_GO";

    // http://loopj.com/android-async-http/
    private static final AsyncHttpClient CLIENT = new AsyncHttpClient();

    // fixme
    private static final String BASE_URL = "http://legall.benoit.free.fr/nihon_go/";
    private static final String AVAILABLE_LESSONS_URL = "%s/available_lessons.json";
    private static final String PACK_FILE = "%s.tsv"; // ex : pack_01.tsv
    private static final String[] LANGUAGES = {"fr_FR", "en_US"};
    private static final String DEFAULT_LANGUAGE = "en_US";

    private LessonAdapter adapter;

    private String myLocale;

    private Set<String> packs;

    public LessonsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLocale = Locale.getDefault().toString();
        if (!ArrayUtils.contains(LANGUAGES, myLocale)) {
            myLocale = DEFAULT_LANGUAGE;
        }

        new TestConnectionTask(this).execute();
    }

    private class TestConnectionTask extends AsyncTask<String, Void, Boolean> {

        private final WeakReference<LessonsFragment> reference;

        public TestConnectionTask(LessonsFragment fragment) {
            this.reference = new WeakReference<>(fragment);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return ConnectionHelper.hasActiveInternetConnection(getActivity());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                reference.get().getAvailableProducts();
            } else {
                reference.get().setEmptyText(getActivity().getString(R.string.no_connection));
            }
        }
    }

    private void inProgress(boolean wait) {
        getActivity().setProgressBarIndeterminateVisibility(wait);
        getListView().setEnabled(!wait);
    }

    private void getAvailableProducts() {

        inProgress(true);

        String packsSaved = PreferencesHelper.getInstance(getContext()).getString(Preferences.PACKS);
        packs = new HashSet<>(Arrays.asList(packsSaved.split(";")));

        String urlLessonsAvailable = String.format(AVAILABLE_LESSONS_URL, BASE_URL);
        Log.d(LOG_TAG, "Calling : " + urlLessonsAvailable);
        CLIENT.get(urlLessonsAvailable, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray response) {
                Log.e(LOG_TAG, "KO", throwable);
                Toast.makeText(getActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                inProgress(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                List<String> availableLessons = new ArrayList<>();

                for (int index = 0, nbItem = response.length(); index < nbItem; index++) {
                    try {
                        String productId = response.getString(index);
                        availableLessons.add(productId);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "KO", e);
                    }
                }

                displayAvailableLessons(availableLessons);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray(myLocale);

                    List<String> availableProducts = new ArrayList<>();
                    for (int index = 0, nbItem = jsonArray.length(); index < nbItem; index++) {
                        try {
                            String productId = jsonArray.getString(index);
                            availableProducts.add(productId);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "KO", e);
                        }
                    }

                    displayAvailableLessons(availableProducts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayAvailableLessons(final List<String> availableProducts) {
        // update the UI
        List<Lesson> lessons = new ArrayList<>();
        for (String availableProduct : availableProducts) {
            // fixme
            lessons.add(new Lesson(availableProduct, availableProduct, "fixme description", packs.contains(availableProduct)));
        }

        Collections.sort(lessons);

        adapter = new LessonAdapter(getActivity(), lessons);
        setListAdapter(adapter);
        inProgress(false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        final Lesson lesson = adapter.getItem(position);
// fixme vérifier que seul les leçons non présentes sont cliquables (définie dans LessonAdapter)
//        if (lesson.isPresent) {
////            new AlertDialog.Builder(getActivity())
////                    .setIcon(R.drawable.ic_warning_black)
////                    .setTitle(R.string.lesson_already_present)
////                    .setMessage(R.string.lesson_continue)
////                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> fetchData(lesson))
////                    .setNegativeButton(getString(R.string.no), null)
////                    .show();
//        } else {
            fetchData(lesson);
//        }
    }

    private void fetchData(final Lesson lesson) {

        final String url = BASE_URL + String.format(PACK_FILE, lesson.code);

        Log.d(LOG_TAG, "Calling : " + url);
        inProgress(true);
        CLIENT.get(url, new FileAsyncHttpResponseHandler(getActivity()) {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.e(LOG_TAG, "KO", throwable);
                Toast.makeText(getActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                inProgress(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final File file) {
                Log.d(LOG_TAG, "File downloaded");
                insertData(file, lesson);
            }
        });
    }

    private void insertData(File file, final Lesson lesson) {
        adapter.setEnabled(false);

        try (Reader in = new FileReader(file)) {
            String col_input = myLocale + "_input";
            String col_details = myLocale + "_details";
            String col_tags = myLocale + "_tags";

            String[] insert = new String[7];
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            CSVParser parse = CSVFormat.TDF.withHeader().withSkipHeaderRecord().parse(in);
            for (CSVRecord record : parse.getRecords()) {

                // 0 : INPUT
                String input = StringUtils.capitalize(record.get(col_input));

                if (StringUtils.isBlank(input)) {
                    continue;
                }

                insert[0] = input;

                // 0 bis : SORT_LETTER
                // Normalizer.normalize(source, Normalizer.Form.NFD) renvoi une chaine unicode décomposé.
                // C'est à dire que les caractères accentués seront décomposé en deux caractères (par exemple "à" se transformera en "a`").
                // Le replaceAll("[\u0300-\u036F]", "") supprimera tous les caractères unicode allant de u0300 à u036F,
                // c'est à dire la plage de code des diacritiques (les accents qu'on a décomposé ci-dessus donc).
                insert[1] = Normalizer.normalize(input.substring(0, 1), Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");

                // 1 : KANJI
                insert[2] = record.get("kanji");

                // 2 : KANA
                insert[3] = record.get("kana");

                // 3 : DETAILS
                insert[4] = record.get(col_details);

                // 4 : TYPE
                insert[5] = record.get("type");

                // 5 : TAGS
                insert[6] = record.get(col_tags);

                ops.add(ContentProviderOperation
                                .newInsert(NihonGoContentProvider.URI_WORD)
                                .withValue(DicoContract.INPUT, insert[0])
                                .withValue(DicoContract.SORT_LETTER, insert[1])
                                .withValue(DicoContract.KANJI, insert[2])
                                .withValue(DicoContract.KANA, insert[3])
                                .withValue(DicoContract.DETAILS, insert[4])
                                .withValue(DicoContract.TYPE, insert[5])
                                .withValue(DicoContract.TAGS, insert[6])
                                .build()
                );
            }

            getActivity().getContentResolver().applyBatch(NihonGoContentProvider.AUTHORITY, ops);

            lesson.isPresent = true;

        } catch (RemoteException | OperationApplicationException | IOException e) {
            Log.e(LOG_TAG, "Error while fetching data", e);
            // fixme afficher Toast d'erreur
        } finally {
            adapter.setEnabled(true);
            getListView().invalidateViews();
        }

        packs.add(lesson.code);
        PreferencesHelper.getInstance(getContext()).saveString(Preferences.PACKS, StringUtils.join(packs, ";"));

        Toast.makeText(getActivity(), getActivity().getString(R.string.lesson_download_success, lesson.title), Toast.LENGTH_LONG).show();
        inProgress(false);
    }

    static class Lesson implements Comparable<Lesson> {
        public final String code;
        public final String title;
        public final String description;
        public boolean isPresent;

        public Lesson(String code, String title, String description, boolean isPresent) {
            this.code = code;
            this.title = title;
            this.description = description;
            this.isPresent = isPresent;
        }

        @Override
        public String toString() {
            return title;
        }

        @Override
        public int compareTo(@NonNull Lesson another) {
            return title.compareTo(another.code);
        }
    }


}
