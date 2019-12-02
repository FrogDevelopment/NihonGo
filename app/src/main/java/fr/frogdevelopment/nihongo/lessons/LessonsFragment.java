package fr.frogdevelopment.nihongo.lessons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class LessonsFragment extends ListFragment {

    private static final String LOG_TAG = "NIHON_GO";

    private static final String[] LANGUAGES = {"fr_FR", "en_US"};
    private static final String DEFAULT_LANGUAGE = "en_US";

    private LessonAdapter mLessonAdapter;

    private String mLocale = Locale.getDefault().toString();

    private Set<String> mLessonsDownloaded;

    private WifiReceiver mWiFiReceiver;
    private TestConnectionTask mTestConnectionTask;
    private LessonsService mLessonsService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLessonsService = new RestServiceFactory().getLessonsService();

        mLocale = Locale.getDefault().toString();
        if (!ArrayUtils.contains(LANGUAGES, mLocale)) {
            mLocale = DEFAULT_LANGUAGE;
        }

        mLessonsDownloaded = PreferencesHelper.getInstance(requireContext()).getStrings(Preferences.LESSONS, ";");

        // Listener for WiFi state
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        mWiFiReceiver = new WifiReceiver();
        requireActivity().registerReceiver(mWiFiReceiver, intentFilter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.lessons_fragment, container, false);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.lesson_no_connection_test);
        rootView.addView(super.onCreateView(inflater, rootView, savedInstanceState), params);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(false);
    }

    @Override
    public void onDestroy() {
        requireActivity().unregisterReceiver(mWiFiReceiver);
        super.onDestroy();
    }

    private class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                boolean isConnectedOrConnecting = isConnectedOrConnecting(connectivityManager);
                if (isConnectedOrConnecting) {
                    testConnection();
                } else {
                    Toast.makeText(context, "Internet connection is need to download lessons", Toast.LENGTH_LONG).show();
                    getOffLineLessons();
                }
            }
        }

        private boolean isConnectedOrConnecting(ConnectivityManager connectivityManager) {
            boolean isConnectedOrConnecting = false;
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                isConnectedOrConnecting = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
            return isConnectedOrConnecting;
        }

        private void testConnection() {
            if (mTestConnectionTask == null) {
                mTestConnectionTask = new TestConnectionTask(LessonsFragment.this::onConnectionResult);
                mTestConnectionTask.execute();
            }
        }
    }

    private void onConnectionResult(boolean result) {
        mTestConnectionTask = null;

        if (result) {
            getAvailableLessons();
        } else {
            getOffLineLessons();
        }

        requireView().findViewById(R.id.lesson_no_connection_test).setVisibility(result ? GONE : VISIBLE);
    }

    private void getOffLineLessons() {
        String tag = getString(R.string.lesson_tag);

        List<Lesson> lessons = mLessonsDownloaded.stream()
                .sorted()
                .filter(StringUtils::isNotBlank)
                .map(code -> new Lesson(code, tag + " " + code, true))
                .collect(toList());

        requireView().findViewById(R.id.lesson_no_connection_test).setVisibility(VISIBLE);
        setLessons(lessons, false);
    }

    private void getAvailableLessons() {
        setListShown(false);
        mLessonsService.fetchAvailableLessons(mLocale).enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful()) {
                    List<Lesson> lessons = Optional.ofNullable(response.body())
                            .orElse(emptyList())
                            .stream()
                            .sorted(Comparator.comparing(Lesson::getCode))
                            .peek(l -> isLessonAlreadyPresent(l))
                            .collect(toList());
                    setLessons(lessons, true);
                } else {
                    getOffLineLessons();
                    Toast.makeText(requireContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                    Log.e(LOG_TAG, "fetchAvailableLessons returned status code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                getOffLineLessons();
                Toast.makeText(requireContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "An error occurred while fetching data", t);
            }
        });
    }

    private void isLessonAlreadyPresent(Lesson lesson) {
        lesson.isPresent = mLessonsDownloaded.contains(lesson.code);
    }

    private void setLessons(List<Lesson> lessons, boolean hasInternet) {
        mLessonAdapter = new LessonAdapter(requireActivity(), lessons);
        mLessonAdapter.setEnabled(hasInternet);
        setListAdapter(mLessonAdapter);
        setListShown(true);
    }

    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id) {
        checkBeforeDownloadLessons(position);
    }

    private void checkBeforeDownloadLessons(int position) {
        Lesson lesson = mLessonAdapter.getItem(position);
        if (lesson == null) {
            // fixme
            return;
        }

        boolean isAlreadyPresent = lesson.isPresent || mLessonsDownloaded.contains(lesson.code);
        if (isAlreadyPresent) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(R.string.lesson_already_present)
                    .setMessage(R.string.lesson_continue)
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> downloadLesson(lesson))
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        } else {
            downloadLesson(lesson);
        }
    }

    private void downloadLesson(Lesson lesson) {
        mLessonsService.fetchLessons(mLocale, lesson.code).enqueue(new Callback<List<Details>>() {
            @Override
            public void onResponse(Call<List<Details>> call, Response<List<Details>> response) {
                if (response.isSuccessful()) {

                    // fixme insert lesson

                    // Normalizer.normalize(source, Normalizer.Form.NFD) renvoi une chaine unicode décomposé.
                    // C'est à dire que les caractères accentués seront décomposé en deux caractères (par exemple "à" se transformera en "a`").
                    // Le replaceAll("[\u0300-\u036F]", "") supprimera tous les caractères unicode allant de u0300 à u036F,
                    // c'est à dire la plage de code des diacritiques (les accents qu'on a décomposé ci-dessus donc).
//                    String sortLetter = Normalizer.normalize(input.substring(0, 1), Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
//
//                    builder = ContentProviderOperation.newInsert(NihonGoContentProvider.URI_WORD)
//                            .withValue(DicoContract.SORT_LETTER, sortLetter)
//                            .withValue(DicoContract.INPUT, input)
//                            .withValue(DicoContract.KANJI, record.get("kanji"))
//                            .withValue(DicoContract.KANA, record.get("kana"))
//                            .withValue(DicoContract.DETAILS, record.get(col_details))
//                            .withValue(DicoContract.EXAMPLE, record.get(col_example))
//                            .withValue(DicoContract.TYPE, record.get("type"))
//                            .withValue(DicoContract.TAGS, tag + " " + numberFormat.format(Integer.valueOf(code)));


                    lesson.isPresent = true;
                    mLessonAdapter.notifyDataSetChanged();

//                    lessonsDownloaded.add(lesson.code);

                    PreferencesHelper.getInstance(requireContext())
                            .saveString(Preferences.LESSONS, mLessonsDownloaded
                                    .stream()
                                    .filter(StringUtils::isNotBlank)
                                    .collect(Collectors.joining(";"))
                            );
                } else {
                    getOffLineLessons();
                    Toast.makeText(requireContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                    Log.e(LOG_TAG, "fetchLessons returned status code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Details>> call, Throwable t) {
                getOffLineLessons();
                Toast.makeText(requireContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "An error occurred while fetching data", t);
            }
        });
    }
}
