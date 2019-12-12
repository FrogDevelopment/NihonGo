package fr.frogdevelopment.nihongo.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import fr.frogdevelopment.nihongo.R;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commitNow();
        }
        setTitle(R.string.drawer_item_settings);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();

        // Instantiate the new Fragment
        Fragment fragment;
        if ("about".equals(pref.getKey())) {
            fragment = new AboutSettingsFragment();
        } else {
            fragment = supportFragmentManager.getFragmentFactory().instantiate(getClassLoader(), pref.getFragment());
        }
        Bundle extras = pref.getExtras();
        fragment.setArguments(extras);
        fragment.setTargetFragment(caller, 0);

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }
}
