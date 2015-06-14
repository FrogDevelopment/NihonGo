package fr.frogdevelopment.nihongo.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

import fr.frogdevelopment.nihongo.contentprovider.DicoContract.Type;

public class NihonGoContentProvider extends ContentProvider {

    private DictionaryOpenHelper database;

    private static final String AUTHORITY = ".NihonGoContentProvider";

    // used for the UriMachter
    private static final int WORDS = 10;
    private static final int WORD_ID = 11;
    private static final int WORD_GROUP_BY_TAG = 12;
    private static final String BASE_PATH_WORD = "word";
    private static final String CONTENT_WORD_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_PATH_WORD + "s";
    private static final String CONTENT_WORD_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_PATH_WORD;
    public static final Uri URI_WORD = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_WORD);

    private static final int EXPRESSIONS = 20;
    private static final int EXPRESSION_ID = 21;
    private static final String BASE_PATH_EXPRESSION = "expression";
    private static final String CONTENT_EXPRESSION_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_PATH_EXPRESSION + "s";
    private static final String CONTENT_EXPRESSION_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_PATH_EXPRESSION;
    public static final Uri URI_EXPRESSION = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_EXPRESSION);

    private static final int SEARCH = 30;
    private static final String BASE_PATH_SEARCH = "search";
    private static final String CONTENT_SEARCH_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_PATH_SEARCH;
    public static final Uri URI_SEARCH = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_SEARCH);

    private static final int ERASE = 40;
    private static final String BASE_PATH_ERASE = "erase";
    private static final String CONTENT_ERASE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_PATH_ERASE;
    public static final Uri URI_ERASE = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_ERASE);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_WORD, WORDS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_WORD + "/#", WORD_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_WORD + "/TAGS", WORD_GROUP_BY_TAG);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_EXPRESSION, EXPRESSIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_EXPRESSION + "/#", EXPRESSION_ID);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_SEARCH, SEARCH);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_ERASE, ERASE);
    }

    @Override
    public boolean onCreate() {
        database = new DictionaryOpenHelper(getContext());
        return false;
    }

    public void insertDataCompileStatement(List<String[]> datas) {
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        SQLiteStatement statement = sqlDB.compileStatement(DicoContract.SQL_INSERT);
        sqlDB.beginTransaction();
        for (String[] data : datas) {
            statement.clearBindings();
            statement.bindAllArgsAsStrings(data);
            statement.executeInsert();
        }
        sqlDB.setTransactionSuccessful();
        sqlDB.endTransaction();
    }

    @Override
    public String getType(Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {

            case WORDS:
                return CONTENT_WORD_TYPE;
            case WORD_ID:
                return CONTENT_WORD_ITEM_TYPE;

            case EXPRESSIONS:
                return CONTENT_EXPRESSION_TYPE;
            case EXPRESSION_ID:
                return CONTENT_EXPRESSION_ITEM_TYPE;

            case SEARCH:
                return CONTENT_SEARCH_TYPE;

            case ERASE:
                return CONTENT_ERASE_TYPE;

            default:
                return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // Set the table
        queryBuilder.setTables(DicoContract.TABLE_NAME);
        String groupBy = null;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {

            case WORD_GROUP_BY_TAG:
                groupBy = DicoContract.TAGS;
                queryBuilder.appendWhere(DicoContract.TYPE + "='" + Type.WORD.code + "'");
                break;
            case WORD_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(DicoContract._ID + "=" + uri.getLastPathSegment());
            case WORDS:
                queryBuilder.appendWhere(DicoContract.TYPE + "='" + Type.WORD.code + "'");
                break;


            case EXPRESSIONS:
            case EXPRESSION_ID:

                if (EXPRESSION_ID == uriType) {
                    // adding the ID to the original query
                    queryBuilder.appendWhere(DicoContract._ID + "=" + uri.getLastPathSegment());
                }
                queryBuilder.appendWhere(DicoContract.TYPE + "='" + Type.EXPRESSION.code + "'");
                break;

            case SEARCH:
                final String search = selectionArgs[0];
                queryBuilder.appendWhere(DicoContract.TYPE + "='" + Type.WORD.code + "'");
                queryBuilder.appendWhere(" AND (" + DicoContract.INPUT + " LIKE '%" + search + "%' OR " + DicoContract.TAGS + " LIKE '%" + search + "%')");
                selectionArgs = null;

                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();

        long id;
        switch (uriType) {
            case WORDS:
            case EXPRESSIONS:
                id = sqlDB.insert(DicoContract.TABLE_NAME, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(BASE_PATH_WORD + "/" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;

        String id = uri.getLastPathSegment();
        switch (uriType) {
            case WORDS:
            case EXPRESSIONS:
                rowsUpdated = sqlDB.update(DicoContract.TABLE_NAME, values, selection, selectionArgs);
                break;

            case WORD_ID:
            case EXPRESSION_ID:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(DicoContract.TABLE_NAME, values, DicoContract._ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(DicoContract.TABLE_NAME, values, DicoContract._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;

        String id = uri.getLastPathSegment();
        switch (uriType) {
            case WORDS:
            case EXPRESSIONS:
                final String selectionDico = String.format(selection, DicoContract._ID);
                rowsDeleted = sqlDB.delete(DicoContract.TABLE_NAME, selectionDico, selectionArgs);
                break;

            case WORD_ID:
            case EXPRESSION_ID:
                rowsDeleted = sqlDB.delete(DicoContract.TABLE_NAME, DicoContract._ID + "=" + id, null);
                break;

            case ERASE:
                rowsDeleted = sqlDB.delete(DicoContract.TABLE_NAME, null, null);
                sqlDB.delete("sqlite_sequence", "name='" + DicoContract.TABLE_NAME + "'", null); // reset du compteur
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

}
