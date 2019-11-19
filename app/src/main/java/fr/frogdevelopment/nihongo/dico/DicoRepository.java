package fr.frogdevelopment.nihongo.dico;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import fr.frogdevelopment.nihongo.data.DicoDao;
import fr.frogdevelopment.nihongo.data.DicoRoomDatabase;
import fr.frogdevelopment.nihongo.data.Item;

import static fr.frogdevelopment.nihongo.data.DicoRoomDatabase.databaseWriteExecutor;
import static fr.frogdevelopment.nihongo.data.DicoRoomDatabase.getDatabase;

class DicoRepository {

    private DicoDao mDicoDao;
    private LiveData<List<Item>> mAll;

    // Note that in order to unit test the DicoRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    DicoRepository(Application application) {
        DicoRoomDatabase db = getDatabase(application);
        mDicoDao = db.dicoDao();
        mAll = mDicoDao.getAll();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Item>> getAll() {
        return mAll;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Item dico) {
        databaseWriteExecutor.execute(() -> mDicoDao.insert(dico));
    }

    void update(Item dico) {
        databaseWriteExecutor.execute(() -> mDicoDao.update(dico));
    }

    void delete(Item dico) {
        databaseWriteExecutor.execute(() -> mDicoDao.delete(dico));
    }
}
