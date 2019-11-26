package fr.frogdevelopment.nihongo.review;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.apache.commons.lang3.mutable.Mutable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.ReviewRepository;
import io.reactivex.disposables.CompositeDisposable;

import static io.reactivex.schedulers.Schedulers.computation;

public class ReviewViewModel extends AndroidViewModel {

    private ReviewRepository mReviewRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private MutableLiveData<List<String>> tags;
    private MutableLiveData<List<Details>> reviews;
    private Mutable<Boolean> mIsJapaneseReview;

    public ReviewViewModel(Application application) {
        super(application);
        mReviewRepository = new ReviewRepository(application);
    }

    boolean isJapaneseReview() {
        return mIsJapaneseReview.getValue();
    }

    void isJapaneseReview(boolean isJapaneseReview) {
        this.mIsJapaneseReview.setValue(isJapaneseReview);
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

    MutableLiveData<List<Details>> reviews(boolean onlyFavorite, int selectedSort, String quantity, int learnedRate, String[] tags) {
        if (reviews == null) {
            reviews = new MutableLiveData<>();
            fetchReviews(onlyFavorite, selectedSort, quantity, learnedRate, tags);
        }
        return reviews;
    }

    private void fetchReviews(boolean onlyFavorite, int selectedSort, String quantity, int learnedRate, String[] tags) {
        disposables.add(mReviewRepository
                .fetch(onlyFavorite, selectedSort, quantity, learnedRate, tags)
                .subscribeOn(computation())
                .subscribe(data -> reviews.postValue(data)));
    }

    Details get(int position) {
        return reviews.getValue().get(position);
    }

    public void update(Details details) {
        mReviewRepository.update(details);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
