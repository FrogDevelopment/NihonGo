package fr.frogdevelopment.nihongo.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface DicoDao {

    @Query("SELECT * FROM dico WHERE type = :type ORDER BY sort_letter, input ASC")
    LiveData<List<Item>> getAllByType(String type);

    @Query("SELECT * FROM dico WHERE type = :type and favorite = 1 ORDER BY sort_letter, input ASC")
    LiveData<List<Item>> getFavoritesByType(String type);

    @Insert(onConflict = REPLACE)
    void insert(Item item);

    @Update(onConflict = REPLACE)
    void update(Item item);

    @Delete
    void delete(Item item);

    @Delete
    void delete(Item... items);
}
