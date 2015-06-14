/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.contentprovider;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class DicoContract implements BaseColumns {

    public static final String TABLE_NAME = "DICO";

    public static final String INPUT = "INPUT";
    public static final String SORT_LETTER = "SORT_LETTER";
    public static final String KANJI = "KANJI";
    public static final String KANA = "KANA";
    public static final String DETAILS = "DETAILS";
    public static final String TYPE = "TYPE";
    public static final String TAGS = "TAGS";
    public static final String FAVORITE = "FAVORITE";

    public static final int INDEX_ID = 0;
    public static final int INDEX_INPUT = 1;
    public static final int INDEX_SORT_LETTER = 2;
    public static final int INDEX_KANJI = 3;
    public static final int INDEX_KANA = 4;
    public static final int INDEX_TAGS = 5;
    public static final int INDEX_DETAILS = 6;
    public static final int INDEX_FAVORITE = 7;

    public static final String[] COLUMNS = {_ID, INPUT, SORT_LETTER, KANJI, KANA, TAGS, DETAILS, FAVORITE};

    // Queries
    private static final String SQL_CREATE = String.format(
            "CREATE TABLE %s ( %s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s TEXT, %s TEXT NOT NULL, %s TEXT, %s INTEGER);",
            TABLE_NAME, _ID, INPUT, SORT_LETTER, KANJI, KANA, DETAILS, TYPE, TAGS, FAVORITE);
    public static final String SQL_INSERT = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME, INPUT, SORT_LETTER, KANJI, KANA, DETAILS, TYPE, TAGS);

    private static final String SQL_DELETE = String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME);

    static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    static void delete(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE);
    }

    static void update(SQLiteDatabase db) {
        delete(db);
        create(db);
    }

    public enum Type {
        WORD("w"),
        EXPRESSION("e");

        private Type(String code) {
            this.code = code;
        }

        public String code;

        public static Type fromCode(String code) {
            if (WORD.code.equals(code)) {
                return WORD;
            } else if (EXPRESSION.code.equals(code)) {
                return EXPRESSION;
            } else {
                throw new IllegalArgumentException("Unknow code :" + code);
            }
        }
    }

}
