package fr.frogdevelopment.nihongo.review.training;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.ReviewRepository;
import io.reactivex.disposables.CompositeDisposable;

import static io.reactivex.schedulers.Schedulers.computation;

public class TrainingViewModel extends AndroidViewModel {

    private final ReviewRepository mReviewRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private MutableLiveData<List<Details>> reviews;
    private final MutableLiveData<Boolean> mIsJapaneseReview = new MutableLiveData<>();

    public TrainingViewModel(Application application) {
        super(application);
        mReviewRepository = new ReviewRepository(application);
    }

    Boolean isJapaneseReview() {
        return mIsJapaneseReview.getValue();
    }

    void isJapaneseReview(boolean isJapaneseReview) {
        this.mIsJapaneseReview.postValue(isJapaneseReview);
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
