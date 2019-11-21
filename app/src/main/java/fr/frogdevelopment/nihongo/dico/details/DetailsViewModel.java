package fr.frogdevelopment.nihongo.dico.details;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.DetailsRepository;

class DetailsViewModel extends AndroidViewModel {

    private DetailsRepository mDetailsRepository;

    DetailsViewModel(Application application) {
        super(application);
        mDetailsRepository = new DetailsRepository(application);
    }

    LiveData<Details> getById(Integer id) {
        return mDetailsRepository.getById(id);
    }

    void update(Details details) {
        mDetailsRepository.update(details);
    }
}
