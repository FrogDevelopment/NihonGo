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

    @Query("SELECT * FROM dico")
    LiveData<List<Item>> getAll();

    @Insert(onConflict = REPLACE)
    void insert(Item item);

    @Update(onConflict = REPLACE)
    void update(Item item);

    @Delete
    void delete(Item item);
}
