package fr.frogdevelopment.nihongo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestConnectionTask extends AsyncTask<String, Void, Boolean> {

	private static final String LOG_TAG = "NIHON_GO";

	private static final String DEFAULT_TEST_URL = "http://www.google.com";

	public interface OnTestConnectionListener {
		void onResult(boolean result);
	}

	private final ConnectivityManager connectivityManager;
	private final OnTestConnectionListener reference;

	public TestConnectionTask(Context context, OnTestConnectionListener listener) {
		this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		this.reference = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		if (connectivityManager == null) {
			return false;
		}

		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		if (isConnected) {
			try {
				HttpURLConnection urlConnection = (HttpURLConnection) new URL(DEFAULT_TEST_URL).openConnection();
				urlConnection.setRequestProperty("User-Agent", "Test");
				urlConnection.setRequestProperty("Connection", "close");
				urlConnection.setConnectTimeout(1500);
				urlConnection.connect();

				return urlConnection.getResponseCode() == 200;
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error during checking internet connection", e);
			}
		}

		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		reference.onResult(result);
	}
}
