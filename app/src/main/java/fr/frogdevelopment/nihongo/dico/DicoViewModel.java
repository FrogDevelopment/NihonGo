package fr.frogdevelopment.nihongo.dico;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Type;

public class DicoViewModel extends AndroidViewModel {

    private DicoRepository mRepository;

    public DicoViewModel(Application application) {
        super(application);
        mRepository = new DicoRepository(application);
    }

    LiveData<List<Item>> getAllByType(Type type, boolean isFilterByFavorite) {
        return mRepository.getAllByType(type, isFilterByFavorite);
    }

    LiveData<List<Item>> search(Type type, String query) {
        return mRepository.search(type, query);
    }

    public void insert(Item word) {
        mRepository.insert(word);
    }

    public void update(Item word) {
        mRepository.update(word);
    }

    public void delete(Item word) {
        mRepository.delete(word);
    }

    public void delete(Item... items) {
        mRepository.delete(items);
    }
}
