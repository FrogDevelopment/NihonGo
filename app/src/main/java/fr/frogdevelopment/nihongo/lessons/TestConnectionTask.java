package fr.frogdevelopment.nihongo.lessons;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import static javax.net.ssl.HttpsURLConnection.HTTP_OK;

public class TestConnectionTask extends AsyncTask<String, Void, Boolean> {

    private static final String LOG_TAG = "NIHON_GO";

    // fixme use health endpoint of lessons service
    private static final String DEFAULT_TEST_URL = "http://www.google.com";

    public interface OnTestConnectionListener {
        void onResult(boolean result);
    }

    private final WeakReference<OnTestConnectionListener> reference;

    TestConnectionTask(OnTestConnectionListener listener) {
        this.reference = new WeakReference<>(listener);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(DEFAULT_TEST_URL).openConnection();
            urlConnection.setRequestProperty("User-Agent", "Test");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setConnectTimeout(1500);
            urlConnection.connect();

            return urlConnection.getResponseCode() == HTTP_OK;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error during checking internet connection", e);
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        OnTestConnectionListener listener = reference.get();
        if (listener != null) {
            listener.onResult(result);
        }
    }
}
