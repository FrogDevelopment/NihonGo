package fr.frogdevelopment.nihongo.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreferencesHelper {

    private static final String NAME = "NihonGoPref";

    public static PreferencesHelper getInstance(@NonNull Context ctx) {
        return new PreferencesHelper(ctx);
    }

    private final SharedPreferences sharedPreferences;

    private PreferencesHelper(Context ctx) {
        sharedPreferences = ctx.getSharedPreferences(NAME, 0);
    }

    public String getString(Preferences key) {
        return getString(key.code);
    }

    public String[] getArrayString(Preferences key, String regex) {
        return getString(key.code).split(regex);
    }

    public Set<String> getStrings(Preferences key, String regex) {
        return Stream.of(getArrayString(key, regex)).collect(Collectors.toSet());
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void saveString(Preferences key, String value) {
        saveString(key.code, value);
    }

    public void saveString(String key, String value) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, -1);
    }

    public void saveInt(String key, int value) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(Preferences key) {
        return getBoolean(key.code);
    }

    public void saveBoolean(Preferences key, boolean value) {
        saveBoolean(key.code, value);
    }

    public void saveBoolean(String key, boolean value) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void remove(String key) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
}
