/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.contentprovider;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class DicoContract implements BaseColumns {

	public static final String TABLE_NAME = "DICO";

	public static final String INPUT       = "INPUT";
	public static final String SORT_LETTER = "SORT_LETTER";
	public static final String KANJI       = "KANJI";
	public static final String KANA        = "KANA";
	public static final String DETAILS     = "DETAILS";
	public static final String EXAMPLE     = "EXAMPLE";
	public static final String TYPE        = "TYPE";
	public static final String TAGS        = "TAGS";
	public static final String FAVORITE    = "FAVORITE";
	public static final String LEARNED     = "LEARNED";

	public static final int INDEX_ID          = 0;
	public static final int INDEX_INPUT       = 1;
	public static final int INDEX_SORT_LETTER = 2;
	public static final int INDEX_KANJI       = 3;
	public static final int INDEX_KANA        = 4;
	public static final int INDEX_TAGS        = 5;
	public static final int INDEX_DETAILS     = 6;
	public static final int INDEX_EXAMPLE     = 7;
	public static final int INDEX_FAVORITE    = 8;
	public static final int INDEX_LEARNED     = 9;

	public static final String[] COLUMNS = {_ID, INPUT, SORT_LETTER, KANJI, KANA, TAGS, DETAILS, EXAMPLE, FAVORITE, LEARNED};

	// Queries
	private static final String SQL_CREATE = String.format(
			"CREATE TABLE %s ( %s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s TEXT, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s INTEGER NOT NULL DEFAULT 0, %s INTEGER NOT NULL DEFAULT 0);",
			TABLE_NAME, _ID, INPUT, SORT_LETTER, KANJI, KANA, DETAILS, EXAMPLE, TYPE, TAGS, FAVORITE, LEARNED);

	private static final String SQL_DELETE = String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME);

	static void create(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	static void delete(SQLiteDatabase db) {
		db.execSQL(SQL_DELETE);
	}

	public enum Type {
		WORD("w"),
		EXPRESSION("e");

		Type(String code) {
			this.code = code;
		}

		public String code;
	}

	// UPDATE
	static final String UPDATE_11 = "ALTER TABLE DICO ADD COLUMN LEARNED INTEGER NOT NULL DEFAULT 0";
	static final String UPDATE_12 = "ALTER TABLE DICO ADD COLUMN EXAMPLE TEXT";

}
