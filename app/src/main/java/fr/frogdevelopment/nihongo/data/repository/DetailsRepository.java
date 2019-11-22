package fr.frogdevelopment.nihongo.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import fr.frogdevelopment.nihongo.data.dao.DetailsDao;
import fr.frogdevelopment.nihongo.data.model.Details;

import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.databaseWriteExecutor;
import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.getDatabase;

public class DetailsRepository {

    private DetailsDao mDetailsDao;

    public DetailsRepository(Application application) {
        NihonGoRoomDatabase db = getDatabase(application);
        mDetailsDao = db.detailsDao();
    }

    public LiveData<Details> getById(Integer id) {
        return mDetailsDao.getById(id);
    }

    public void insert(Details details) {
        databaseWriteExecutor.execute(() -> mDetailsDao.insert(details));
    }

    public void update(Details details) {
        databaseWriteExecutor.execute(() -> mDetailsDao.update(details));
    }

    public void delete(Integer id) {
        databaseWriteExecutor.execute(() -> mDetailsDao.delete(id));
    }
}
