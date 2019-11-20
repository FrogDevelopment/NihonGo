package fr.frogdevelopment.nihongo.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import fr.frogdevelopment.nihongo.contentprovider.DicoContract.Type;
import fr.frogdevelopment.nihongo.data.dao.RowDao;
import fr.frogdevelopment.nihongo.data.model.Row;
import fr.frogdevelopment.nihongo.dico.input.InputUtils;

import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.databaseWriteExecutor;
import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.getDatabase;

public class RowRepository {

    private RowDao mRowDao;

    public RowRepository(Application application) {
        NihonGoRoomDatabase db = getDatabase(application);
        mRowDao = db.rowDao();
    }

    public LiveData<List<Row>> getAllByType(Type type, boolean isFilterByFavorite) {
        return isFilterByFavorite ?
                mRowDao.getFavoritesByType(type.code) :
                mRowDao.getAllByType(type.code);
    }

    public LiveData<List<Row>> search(Type type, String search) {
        if (InputUtils.containsNoJapanese(search)) {
            return mRowDao.searchByInput(type.code, toLike(search));
        } else if (InputUtils.containsKanji(search)) {
            return mRowDao.searchByKanji(type.code, toLike(search));
        } else {
            return mRowDao.searchByKana(type.code, toLike(search));
        }
    }

    private static String toLike(String value) {
        return "%" + value + "%";
    }

    public void delete(Integer... ids) {
        databaseWriteExecutor.execute(() -> mRowDao.delete(ids));
    }
}
