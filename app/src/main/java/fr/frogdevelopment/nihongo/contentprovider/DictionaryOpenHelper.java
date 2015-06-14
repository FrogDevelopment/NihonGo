/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "NIHON_GO";

    // When changing the database schema, increment the database version.
    private static final int DATABASE_VERSION = 9;
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
        Log.w(LOG_TAG, "Upgrading database. Existing contents will be lost. [" + oldVersion + "]->[" + newVersion + "]");

        DicoContract.update(db);
    }

}
