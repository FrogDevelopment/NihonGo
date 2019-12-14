package fr.frogdevelopment.nihongo.edit;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.DetailsRepository;

public class EditViewModel extends AndroidViewModel {

    private final DetailsRepository mDetailsRepository;

    public EditViewModel(Application application) {
        super(application);
        mDetailsRepository = new DetailsRepository(application);
    }

    LiveData<Details> getById(Integer id) {
        return mDetailsRepository.getById(id);
    }

    void insert(Details details) {
        mDetailsRepository.insert(details);
    }

    void update(Details details) {
        mDetailsRepository.update(details);
    }
}
