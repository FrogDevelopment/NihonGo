package fr.frogdevelopment.nihongo.data.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.frogdevelopment.nihongo.data.dao.DetailsDao;
import fr.frogdevelopment.nihongo.data.dao.RowDao;
import fr.frogdevelopment.nihongo.data.model.Details;

@Database(
        entities = {Details.class},
        version = 14,
        exportSchema = false
)
public abstract class NihonGoRoomDatabase extends RoomDatabase {

    private static volatile NihonGoRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static NihonGoRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NihonGoRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            NihonGoRoomDatabase.class, "NIHON_GO")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract RowDao rowDao();

    public abstract DetailsDao detailsDao();
}
