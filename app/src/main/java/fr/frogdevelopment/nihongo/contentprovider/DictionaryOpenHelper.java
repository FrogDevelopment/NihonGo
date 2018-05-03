/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DictionaryOpenHelper extends SQLiteOpenHelper {

    // When changing the database schema, increment the database version.
    private static final int DATABASE_VERSION = 14;
    private static final String DATABASE_NAME = "NIHON_GO";

    public DictionaryOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION/*,DatabaseErrorHandler*/);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        DicoContract.create(db);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 11) {
            db.execSQL(DicoContract.UPDATE_11);
        }
        if (newVersion == 12) {
            db.execSQL(DicoContract.UPDATE_12);
        }

        if (newVersion == 14) {
            db.execSQL(DicoContract.UPDATE_13);
            db.execSQL(DicoContract.UPDATE_14);
        }
    }

}
