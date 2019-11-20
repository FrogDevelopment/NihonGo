package fr.frogdevelopment.nihongo.dico.input;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.DetailsRepository;

public class InputViewModel extends AndroidViewModel {

    private DetailsRepository mDetailsRepository;

    public InputViewModel(Application application) {
        super(application);
        mDetailsRepository = new DetailsRepository(application);
    }

    LiveData<Details> getById(String id) {
        return mDetailsRepository.getById(id);
    }

    void insert(Details details) {
        mDetailsRepository.insert(details);
    }

    void update(Details details) {
        mDetailsRepository.update(details);
    }
}
