package fr.frogdevelopment.nihongo.data.repository;

import android.app.Application;

import fr.frogdevelopment.nihongo.data.dao.SettingsDao;

import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.databaseWriteExecutor;
import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.getDatabase;

public class SettingsRepository {

    private SettingsDao mSettingsDao;

    public SettingsRepository(Application application) {
        NihonGoRoomDatabase db = getDatabase(application);
        mSettingsDao = db.settingsDao();
    }

    public void erase() {
        databaseWriteExecutor.execute(() -> mSettingsDao.erase());
    }

    public void resetFavorites() {
        databaseWriteExecutor.execute(() -> mSettingsDao.resetFavorites());
    }

    public void resetLearned() {
        databaseWriteExecutor.execute(() -> mSettingsDao.resetLearned());
    }
}
