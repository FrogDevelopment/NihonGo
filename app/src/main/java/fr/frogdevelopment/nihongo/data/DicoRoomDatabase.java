package fr.frogdevelopment.nihongo.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {Item.class},
        version = 14,
        exportSchema = false
)
public abstract class DicoRoomDatabase extends RoomDatabase {

    public abstract DicoDao dicoDao();

    private static volatile DicoRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static DicoRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DicoRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DicoRoomDatabase.class, "NIHON_GO")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
