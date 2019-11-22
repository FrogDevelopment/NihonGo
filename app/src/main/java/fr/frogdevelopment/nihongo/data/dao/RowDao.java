package fr.frogdevelopment.nihongo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Row;

@Dao
public interface RowDao {

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> getAll();

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE favorite = 1 ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> getFavorites();

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE input like :search ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> searchByInput(String search);

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE kanji like :search ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> searchByKanji(String search);

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE kana like :search ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> searchByKana(String search);

    @Query("DELETE FROM dico WHERE _id IN (:ids)")
    void delete(Integer... ids);
}
