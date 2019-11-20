package fr.frogdevelopment.nihongo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Row;

@Dao
public interface RowDao {

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE type = :type ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> getAllByType(String type);

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE type = :type and favorite = 1 ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> getFavoritesByType(String type);

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE type = :type and input like :search ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> searchByInput(String type, String search);

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE type = :type and kanji like :search ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> searchByKanji(String type, String search);

    @Query("SELECT _id, input, kanji, kana, tags, sort_letter FROM dico WHERE type = :type and kana like :search ORDER BY sort_letter, input ASC")
    LiveData<List<Row>> searchByKana(String type, String search);

    @Query("DELETE FROM dico WHERE _id IN (:ids)")
    void delete(Integer... ids);
}
