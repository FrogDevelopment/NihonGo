/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.contentprovider;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class ConjugationContract implements BaseColumns {

	public static final String TABLE_NAME = "CONJUGATION";

	public static final String GROUP   = "_GROUP";
	public static final String DICO    = "DICO";
	public static final String MASU    = "MASU";
	public static final String TE      = "TE";
	public static final String NAI     = "NAI";
	public static final String TA      = "TA";
	public static final String WORD_ID = "WORD_ID";

	public static final int INDEX_ID      = 0;
	public static final int INDEX_GROUP   = 1;
	public static final int INDEX_DICO    = 2;
	public static final int INDEX_MASU    = 3;
	public static final int INDEX_TE      = 4;
	public static final int INDEX_NAI     = 5;
	public static final int INDEX_TA      = 6;
	public static final int INDEX_WORD_ID = 7;

	public static final String[] COLUMNS = {_ID, GROUP, DICO, MASU, TE, NAI, TA, WORD_ID};

	// Queries
	private static final String SQL_CREATE = String.format(
			"CREATE TABLE %s ( %s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s TEXT, %s INTEGER NOT NULL);",
			TABLE_NAME, _ID, GROUP, DICO, MASU, TE, NAI, TA, WORD_ID);

	private static final String SQL_DELETE = String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME);

	static void create(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	static void delete(SQLiteDatabase db) {
		db.execSQL(SQL_DELETE);
	}

	public enum Group {
		I("I"),
		II("II"),
		III("III");

		Group(String code) {
			this.code = code;
		}

		public String code;
	}

}
