package fr.frogdevelopment.nihongo.dico;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.DetailsRepository;
import io.reactivex.disposables.CompositeDisposable;

import static io.reactivex.schedulers.Schedulers.computation;

public class DetailsViewModel extends AndroidViewModel {

    private final DetailsRepository mDetailsRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private MutableLiveData<Details> mDetails;

    public DetailsViewModel(Application application) {
        super(application);
        mDetailsRepository = new DetailsRepository(application);
    }

    public LiveData<Details> getById(Integer id) {
        if (mDetails == null) {
            mDetails = new MutableLiveData<>();
            disposables.add(mDetailsRepository.getById(id)
                    .subscribeOn(computation())
                    .subscribe(value -> this.mDetails.postValue(value)));
        }
        return mDetails;
    }

    public Details details() {
        return mDetails == null ? null : mDetails.getValue();
    }

    public void insert(Details details) {
        mDetailsRepository.insert(details);
    }

    public void update(Details details) {
        mDetailsRepository.update(details);
    }

    public void delete(Details details) {
        mDetailsRepository.delete(details);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
