package fr.frogdevelopment.nihongo.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

import static fr.frogdevelopment.nihongo.preferences.Preferences.LESSONS;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsViewModel mSettingsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        CharSequence title = preference.getTitle();
        if (title.equals(getString(R.string.settings_erase_data_title))) {
            onClickErase();
        } else if (title.equals(getString(R.string.settings_reset_favorite_title))) {
            onClickResetFavorite();
        } else if (title.equals(getString(R.string.settings_reset_learned_title))) {
            onClickResetLearned();
        } else {
            return super.onPreferenceTreeClick(preference);
        }

        return true;
    }

    private void onClickErase() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_erase_data_title)
                .setMessage(R.string.settings_erase_data_confirmation)
                .setPositiveButton(R.string.settings_erase, (dialog, id) -> {
                    mSettingsViewModel.erase();

                    PreferencesHelper.getInstance(requireActivity()).saveString(LESSONS, "");

                    Snackbar.make(requireView(), R.string.settings_erase_data_success, Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void onClickResetFavorite() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_reset_favorite_title)
                .setMessage(R.string.settings_reset_favorite_confirmation)
                .setPositiveButton(R.string.settings, (dialog, id) -> {
                    mSettingsViewModel.resetFavorites();

                    Snackbar.make(requireView(), R.string.settings_reset_favorite_success, Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void onClickResetLearned() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_reset_learned_title)
                .setMessage(R.string.settings_reset_learned_erase_confirmation)
                .setPositiveButton(R.string.settings, (dialog, id) -> {
                    mSettingsViewModel.resetLearned();

                    Snackbar.make(requireView(), R.string.settings_reset_learned_erase_success, Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }
}
