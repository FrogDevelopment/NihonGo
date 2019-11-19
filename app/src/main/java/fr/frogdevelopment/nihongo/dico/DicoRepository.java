package fr.frogdevelopment.nihongo.dico;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import fr.frogdevelopment.nihongo.data.DicoDao;
import fr.frogdevelopment.nihongo.data.DicoRoomDatabase;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;

import static fr.frogdevelopment.nihongo.data.DicoRoomDatabase.databaseWriteExecutor;
import static fr.frogdevelopment.nihongo.data.DicoRoomDatabase.getDatabase;

class DicoRepository {

    private DicoDao mDicoDao;

    DicoRepository(Application application) {
        DicoRoomDatabase db = getDatabase(application);
        mDicoDao = db.dicoDao();
    }

    LiveData<List<Item>> getAllByType(Type type, boolean isFilterByFavorite) {
        return isFilterByFavorite ?
                mDicoDao.getFavoritesByType(type.code) :
                mDicoDao.getAllByType(type.code);
    }

    void insert(Item dico) {
        databaseWriteExecutor.execute(() -> mDicoDao.insert(dico));
    }

    void update(Item dico) {
        databaseWriteExecutor.execute(() -> mDicoDao.update(dico));
    }

    void delete(Item dico) {
        databaseWriteExecutor.execute(() -> mDicoDao.delete(dico));
    }

    void delete(Item... items) {
        databaseWriteExecutor.execute(() -> mDicoDao.delete(items));
    }
}
