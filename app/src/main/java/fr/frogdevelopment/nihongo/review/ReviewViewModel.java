package fr.frogdevelopment.nihongo.review;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.repository.ReviewRepository;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;

public class ReviewViewModel extends AndroidViewModel {

    private ReviewRepository mReviewRepository;

    public ReviewViewModel(Application application) {
        super(application);
        mReviewRepository = new ReviewRepository(application);
    }

    Maybe<List<Details>> fetch(boolean onlyFavorite, int selectedSort, String quantity, int learnedRate, String[] tags) {
        return mReviewRepository
                .fetch(onlyFavorite, selectedSort, quantity, learnedRate, tags)
                .subscribeOn(Schedulers.computation());
    }

    public void update(Details details) {
        mReviewRepository.update(details);
    }
}
