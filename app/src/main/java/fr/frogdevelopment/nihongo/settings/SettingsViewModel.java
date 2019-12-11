package fr.frogdevelopment.nihongo.settings;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import fr.frogdevelopment.nihongo.data.repository.SettingsRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final SettingsRepository mSettingsRepository;

    public SettingsViewModel(Application application) {
        super(application);
        mSettingsRepository = new SettingsRepository(application);
    }

    void erase() {
        mSettingsRepository.erase();
    }

    void resetFavorites() {
        mSettingsRepository.resetFavorites();
    }

    void resetLearned() {
        mSettingsRepository.resetLearned();
    }
}
