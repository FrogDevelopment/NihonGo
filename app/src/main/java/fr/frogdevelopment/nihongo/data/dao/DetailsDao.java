package fr.frogdevelopment.nihongo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import fr.frogdevelopment.nihongo.data.model.Details;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface DetailsDao {

    @Query("SELECT * FROM dico WHERE _id = :id")
    LiveData<Details> getById(Integer id);

    @Insert(onConflict = REPLACE)
    void insert(Details item);

    @Update(onConflict = REPLACE)
    void update(Details item);

    @Delete
    void delete(Details... details);

}
