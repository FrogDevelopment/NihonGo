package fr.frogdevelopment.nihongo.dico;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Row;
import fr.frogdevelopment.nihongo.data.repository.RowRepository;

public class RowViewModel extends AndroidViewModel {

    private RowRepository mRepository;

    public RowViewModel(Application application) {
        super(application);
        mRepository = new RowRepository(application);
    }

    LiveData<List<Row>> getAll(boolean isFilterByFavorite) {
        return mRepository.getAll(isFilterByFavorite);
    }

    LiveData<List<Row>> search(String query) {
        return mRepository.search(query);
    }

    public void delete(Integer... ids) {
        mRepository.delete(ids);
    }
}
