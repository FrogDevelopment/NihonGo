package fr.frogdevelopment.nihongo.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.frogdevelopment.nihongo.data.dao.DetailsDao;
import fr.frogdevelopment.nihongo.data.dao.ReviewsDao;
import fr.frogdevelopment.nihongo.data.dao.RowDao;
import fr.frogdevelopment.nihongo.data.model.Details;

@Database(
        entities = {Details.class},
        version = 15,
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
                            .addMigrations(
                                    MIGRATION_10_11,
                                    MIGRATION_11_12,
                                    MIGRATION_12_13,
                                    MIGRATION_13_14,
                                    MIGRATION_14_15
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract RowDao rowDao();

    public abstract DetailsDao detailsDao();

    public abstract ReviewsDao reviewsDao();

    private static Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DICO ADD COLUMN LEARNED INTEGER NOT NULL DEFAULT 0;");
        }
    };
    private static Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DICO ADD COLUMN EXAMPLE TEXT;");
        }
    };
    private static Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DICO ADD COLUMN SUCCESS INTEGER NOT NULL DEFAULT 0;");
        }
    };
    private static Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DICO ADD COLUMN FAILED INTEGER NOT NULL DEFAULT 0;");
        }
    };
    private static Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // can't drop column => https://www.sqlite.org/faq.html#q11
            database.execSQL("CREATE TEMPORARY TABLE dico_backup (_id INTEGER PRIMARY KEY AUTOINCREMENT, input TEXT NOT NULL, sort_letter TEXT NOT NULL, kanji TEXT, kana TEXT, details TEXT, example TEXT, tags TEXT, favorite INTEGER NOT NULL DEFAULT 0, success INTEGER NOT NULL DEFAULT 0, learned INTEGER NOT NULL DEFAULT 0, failed INTEGER NOT NULL DEFAULT 0);");
            database.execSQL("INSERT INTO dico_backup (_id, input, sort_letter, kanji, kana, details, example, tags, favorite, success, learned, failed) SELECT _id, input, sort_letter, kanji, kana, details, example, tags, favorite, success, learned, failed FROM dico;");
            database.execSQL("DROP TABLE dico;");
            database.execSQL("CREATE TABLE dico (_id INTEGER PRIMARY KEY AUTOINCREMENT, input TEXT NOT NULL, sort_letter TEXT NOT NULL, kanji TEXT, kana TEXT, details TEXT, example TEXT, tags TEXT, favorite INTEGER NOT NULL DEFAULT 0, success INTEGER NOT NULL DEFAULT 0, learned INTEGER NOT NULL DEFAULT 0, failed INTEGER NOT NULL DEFAULT 0);");
            database.execSQL("INSERT INTO dico (_id, input, sort_letter, kanji, kana, details, example, tags, favorite, success, learned, failed) SELECT _id, input, sort_letter, kanji, kana, details, example, tags, favorite, success, learned, failed FROM dico_backup;");
        }
    };
}
