package fr.frogdevelopment.nihongo.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import fr.frogdevelopment.nihongo.R;

import static fr.frogdevelopment.nihongo.BuildConfig.VERSION_CODE;
import static fr.frogdevelopment.nihongo.BuildConfig.VERSION_NAME;

public class AboutSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_about, rootKey);

        Preference version = findPreference("version");
        if (version != null) {
            version.setSummary(getString(R.string.settings_about_version, VERSION_NAME, VERSION_CODE));
        }
    }

}
