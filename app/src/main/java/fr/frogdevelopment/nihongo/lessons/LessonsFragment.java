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

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.view.View.VISIBLE;
import static android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

public class LessonsFragment extends ListFragment {

    private static final String LOG_TAG = "NIHON_GO";

    private static final String[] LANGUAGES = {"fr_FR", "en_US"};
    private static final String DEFAULT_LANGUAGE = "en_US";

    private LessonAdapter mLessonAdapter;

    private String mLocale = Locale.getDefault().toString();
    private String mSuffixTag;

    private Set<String> mLessonsDownloaded;

    private WifiReceiver mWiFiReceiver;
    private LessonsService mLessonsService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLessonsService = new RestServiceFactory().getLessonsService();

        mLocale = Locale.getDefault().toString();
        if (!ArrayUtils.contains(LANGUAGES, mLocale)) {
            mLocale = DEFAULT_LANGUAGE;
        }

        mSuffixTag = getString(R.string.lesson_tag);

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
                    getAvailableLessons();
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
    }

    private void getOffLineLessons() {
        List<Lesson> lessons = mLessonsDownloaded.stream()
                .sorted()
                .filter(StringUtils::isNotBlank)
                .map(code -> new Lesson(code, mSuffixTag, true))
                .collect(toList());

        requireView().findViewById(R.id.lesson_no_connection_test).setVisibility(VISIBLE);
        setLessons(lessons, false);
    }

    private void getAvailableLessons() {
        setListShown(false);
        mLessonsService.fetchAvailableLessons(mLocale).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful()) {
                    Integer lastLesson = Optional.ofNullable(response.body()).orElse(0);
                    List<Lesson> lessons = IntStream.rangeClosed(1, lastLesson)
                            .mapToObj(String::valueOf)
                            .map(n -> new Lesson(n, mSuffixTag, mLessonsDownloaded.contains(n)))
                            .collect(toList());
                    setLessons(lessons, true);
                } else {
                    getOffLineLessons();
                    Toast.makeText(requireContext(), "Lessons service not reachable", Toast.LENGTH_LONG).show();
                    Log.e(LOG_TAG, "fetchAvailableLessons returned status code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                getOffLineLessons();
                Toast.makeText(requireContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "An error occurred while fetching data", t);
            }
        });
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
        mLessonsService.fetchLessons(mLocale, uncapitalize(lesson.title)).enqueue(new Callback<List<Details>>() {
            @Override
            public void onResponse(@NonNull Call<List<Details>> call, @NonNull Response<List<Details>> response) {
                if (response.isSuccessful()) {

                    // fixme insert lesson


//                    lesson.isPresent = true;
//                    mLessonAdapter.notifyDataSetChanged();

//                    mLessonsDownloaded.add(lesson.code);
//                    PreferencesHelper.getInstance(requireContext())
//                            .saveString(Preferences.LESSONS, mLessonsDownloaded
//                                    .stream()
//                                    .filter(StringUtils::isNotBlank)
//                                    .collect(Collectors.joining(";"))
//                            );
                } else {
                    getOffLineLessons();
                    Toast.makeText(requireContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                    Log.e(LOG_TAG, "fetchLessons returned status code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Details>> call, @NonNull Throwable t) {
                getOffLineLessons();
                Toast.makeText(requireContext(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "An error occurred while fetching data", t);
            }
        });
    }
}
