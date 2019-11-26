package fr.frogdevelopment.nihongo.review;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.ReviewRepository;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ReviewViewModel extends AndroidViewModel {

    private ReviewRepository mReviewRepository;

    private MutableLiveData<List<String>> tags;
    private Disposable disposable;

    public ReviewViewModel(Application application) {
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
        disposable = mReviewRepository
                .getTags()
                .subscribeOn(Schedulers.computation())
                .subscribe(values -> {
                    tags.setValue(values.stream()
                            .flatMap(tag -> Stream.of(tag.split(",")))
                            .distinct()
                            .collect(Collectors.toList()));
                });
    }

    Maybe<List<Details>> fetch(boolean onlyFavorite, int selectedSort, String quantity, int learnedRate, String[] tags) {
        return mReviewRepository
                .fetch(onlyFavorite, selectedSort, quantity, learnedRate, tags)
                .subscribeOn(Schedulers.computation());
    }

    public void update(Details details) {
        mReviewRepository.update(details);
    }

    @Override
    protected void onCleared() {
        if (disposable != null) {
            disposable.dispose();
        }
        super.onCleared();
    }
}
