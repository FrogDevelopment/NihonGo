package fr.frogdevelopment.nihongo.dico;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import fr.frogdevelopment.nihongo.data.Item;

public class DicoViewModel extends AndroidViewModel {

    private DicoRepository mRepository;

    private LiveData<List<Item>> mAllWords;

    public DicoViewModel(Application application) {
        super(application);
        mRepository = new DicoRepository(application);
        mAllWords = mRepository.getAll();
    }

    LiveData<List<Item>> getAllWords() {
        return mAllWords;
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
}
