package fr.frogdevelopment.nihongo.data.repository;

import android.app.Application;

import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;
import java.util.stream.Stream;

import fr.frogdevelopment.nihongo.data.dao.TrainingDao;
import fr.frogdevelopment.nihongo.data.model.Details;
import io.reactivex.Maybe;

import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.databaseWriteExecutor;
import static fr.frogdevelopment.nihongo.data.repository.NihonGoRoomDatabase.getDatabase;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

public class ReviewRepository {

    private TrainingDao mDetailsDao;

    public ReviewRepository(Application application) {
        NihonGoRoomDatabase db = getDatabase(application);
        mDetailsDao = db.trainingDao();
    }

    public Maybe<List<String>> getTags() {
        return mDetailsDao.getTags();
    }

    public void update(Details details) {
        databaseWriteExecutor.execute(() -> mDetailsDao.update(details));
    }

    public Maybe<List<Details>> fetch(boolean onlyFavorite, int selectedSort, String quantity, int learnedRate, String[] tags) {
        String select = getSelect();
        String where = getWhere(onlyFavorite, learnedRate, tags);
        String sortOrder = getOrderBy(selectedSort);
        String limit = getLimit(quantity);

        return mDetailsDao.fetch(new SimpleSQLiteQuery(select + where + sortOrder + limit));
    }

    private String getSelect() {
        return "SELECT * FROM dico";
    }

    private String getWhere(boolean onlyFavorite, int learnedRate, String[] tags) {
        String selection = " WHERE 1 = 1";
        if (onlyFavorite) {
            selection += " AND FAVORITE = '1'";
        }

        if (tags != null && isNotEmpty(tags)) {
            String orTags = Stream.of(tags)
                    .map(tag -> format("TAGS = '%s'", tag))
                    .collect(joining(" OR "));
            selection += " AND (" + orTags + ")";
        }

        switch (learnedRate) {
            case 0: // new
            case 1: // view
            case 2: // learned
                selection += " AND learned = " + learnedRate;
                break;
        }

        return selection;
    }

    private String getOrderBy(int selectedSort) {
        String sortOrder;
        switch (selectedSort) {
            case 0: // new -> old
                sortOrder = " ORDER BY _id ASC";
                break;
            case 1: // old -> new
                sortOrder = " ORDER BY _id DESC";
                break;
            case 2: // random
                sortOrder = " ORDER BY RANDOM()";
                break;
            default:
                sortOrder = "";
                break;
        }

        return sortOrder;
    }

    private String getLimit(String quantity) {
        return " LIMIT " + quantity;
    }
}
