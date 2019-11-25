package fr.frogdevelopment.nihongo.data.dao;

import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;
import io.reactivex.Maybe;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface ReviewsDao {

    @RawQuery(observedEntities = {Details.class})
    Maybe<List<Details>> fetch(SupportSQLiteQuery query);

    @Update(onConflict = REPLACE)
    void update(Details item);
}
