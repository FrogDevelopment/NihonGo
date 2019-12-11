package fr.frogdevelopment.nihongo.lessons;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.DetailsRepository;

public class LessonsViewModel extends AndroidViewModel {

    private final DetailsRepository mDetailsRepository;

    public LessonsViewModel(Application application) {
        super(application);
        mDetailsRepository = new DetailsRepository(application);
    }

    public void insert(List<Details> details) {
        mDetailsRepository.insert(details);
    }
}
