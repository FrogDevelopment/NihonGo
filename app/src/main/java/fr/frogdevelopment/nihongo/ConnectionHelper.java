/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionHelper {

    private static final String LOG_TAG = "NIHON_GO";

    private static final String DEFAULT_TEST_URL = "http://www.google.com";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (connectivity == null) {
		    return false;
	    }

	    NetworkInfo[] info = connectivity.getAllNetworkInfo();
	    if (info == null) {
		    return false;
	    }

	    for (NetworkInfo element : info) {
	        if (element.getState() == NetworkInfo.State.CONNECTED) {
	            return true;
	        }
	    }
	    return false;
    }

    public static boolean hasActiveInternetConnection(Context context) {
        return testUrl(context, DEFAULT_TEST_URL);
    }

    public static boolean testUrl(Context context, String url) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestProperty("User-Agent", "Test");
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.setConnectTimeout(1500);
                urlConnection.connect();
                return urlConnection.getResponseCode() == 200;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error during checking internet connection", e);
            }
        } else {
            Log.d(LOG_TAG, "No network available!");
        }
        return false;
    }

}
