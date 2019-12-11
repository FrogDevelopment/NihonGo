package fr.frogdevelopment.nihongo.data.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class SettingsDao {

    @Query("DELETE FROM dico;")
    public abstract void deleteDicoTable();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'dico';")
    public abstract void resetDicoSequence();

    @Query("UPDATE dico SET favorite = 0 WHERE 1 = 1")
    public abstract void resetFavorites();

    @Query("UPDATE dico SET learned = 0 WHERE 1 = 1")
    public abstract void resetLearned();

    @Transaction
    public void erase() {
        deleteDicoTable();
        resetDicoSequence();
    }

}
