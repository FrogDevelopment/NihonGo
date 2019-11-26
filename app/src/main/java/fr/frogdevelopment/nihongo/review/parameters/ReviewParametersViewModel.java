package fr.frogdevelopment.nihongo.review.parameters;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.data.repository.ReviewRepository;
import io.reactivex.disposables.CompositeDisposable;

import static io.reactivex.schedulers.Schedulers.computation;

public class ReviewParametersViewModel extends AndroidViewModel {

    private final ReviewRepository mReviewRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private MutableLiveData<List<String>> tags;

    public ReviewParametersViewModel(Application application) {
        super(application);
        mReviewRepository = new ReviewRepository(application);
    }

    MutableLiveData<List<String>> tags() {
        if (tags == null) {
            tags = new MutableLiveData<>();
            getTags();
        }
        return tags;
    }

    private void getTags() {
        disposables.add(mReviewRepository
                .getTags()
                .subscribeOn(computation())
                .subscribe(values -> tags.postValue(values.stream()
                        .flatMap(tag -> Stream.of(tag.split(",")))
                        .distinct()
                        .collect(Collectors.toList()))));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
