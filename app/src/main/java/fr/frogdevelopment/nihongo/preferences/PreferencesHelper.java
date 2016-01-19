package fr.frogdevelopment.nihongo.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private static final String NAME = "NihonGoPref";

    public static PreferencesHelper getInstance(Context ctx) {
        return new PreferencesHelper(ctx);
    }

    private final SharedPreferences sharedPreferences;

    private PreferencesHelper(Context ctx) {
        sharedPreferences = ctx.getSharedPreferences(NAME, 0);
    }

    public String getString(Preferences key) {
        return sharedPreferences.getString(key.code, "");
    }

    public void saveString(Preferences key, String value) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key.code, value);
        editor.apply();
    }


    public boolean getBoolean(Preferences key) {
        return sharedPreferences.getBoolean(key.code, false);
    }

    public void saveBoolean(Preferences key, boolean value) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key.code, value);
        editor.apply();
    }

}
