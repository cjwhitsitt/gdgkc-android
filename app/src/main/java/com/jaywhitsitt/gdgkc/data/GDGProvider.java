package com.jaywhitsitt.gdgkc.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * ContentProvider subclass that retrieves data related to GDG chapter
 * Created by jay on 4/20/15.
 */
public class GDGProvider extends ContentProvider {

    private final String LOG_TAG = GDGProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private GDGDbHelper mOpenHelper;

    static final int CHAPTERS = 100;
    static final int CHAPTER = 101;
    static final int CHAPTER_EVENTS = 102;
    static final int EVENT = 103;

    private static final SQLiteQueryBuilder sChapterQueryBuilder;
    static {
        sChapterQueryBuilder = new SQLiteQueryBuilder();
        sChapterQueryBuilder.setTables(
                GDGContract.ChapterEntry.TABLE_NAME
        );
    }

    private static final SQLiteQueryBuilder sEventQueryBuilder;
    static {
        sEventQueryBuilder = new SQLiteQueryBuilder();
        sEventQueryBuilder.setTables(
                GDGContract.EventEntry.TABLE_NAME
        );
    }

    private static final SQLiteQueryBuilder sEventByChapterQueryBuilder;
    static{
        sEventByChapterQueryBuilder = new SQLiteQueryBuilder();
        sEventByChapterQueryBuilder.setTables(
                GDGContract.EventEntry.TABLE_NAME + " INNER JOIN " +
                        GDGContract.ChapterEntry.TABLE_NAME +
                        " ON " + GDGContract.EventEntry.TABLE_NAME +
                        "." + GDGContract.EventEntry.COLUMN_CHAPTER_KEY +
                        " = " + GDGContract.ChapterEntry.TABLE_NAME +
                        "." + GDGContract.ChapterEntry._ID
        );
    }

    private static final String sChapterSelectionByGoogleId =
            GDGContract.ChapterEntry.TABLE_NAME +
                    "." + GDGContract.ChapterEntry.COLUMN_CHAPTER_GID + " = ? ";
    private static final String sChapterSelectionByRowId =
            GDGContract.ChapterEntry.TABLE_NAME +
                    "." + GDGContract.ChapterEntry._ID + " = ? ";

    public static final String sChapterEventsSelection =
            GDGContract.ChapterEntry.TABLE_NAME +
                    "." + GDGContract.ChapterEntry.COLUMN_CHAPTER_GID + " = ? ";

    private static final String sChapterSingleEventSelectionByGoogleId =
            GDGContract.EventEntry.TABLE_NAME +
                    "." + GDGContract.EventEntry.COLUMN_EVENT_GID + " = ? ";
    private static final String sChapterSingleEventSelectionByRowId =
            GDGContract.EventEntry.TABLE_NAME +
                    "." + GDGContract.EventEntry._ID + " = ? ";

    /* The methods */

    @Override
    public boolean onCreate() {
        mOpenHelper = new GDGDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CHAPTERS:
                return GDGContract.ChapterEntry.CONTENT_TYPE;
            case CHAPTER:
                return GDGContract.ChapterEntry.CONTENT_ITEM_TYPE;
            case CHAPTER_EVENTS:
                return GDGContract.EventEntry.CONTENT_TYPE;
            case EVENT:
                return GDGContract.EventEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = GDGContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        uriMatcher.addURI(authority, GDGContract.PATH_CHAPTERS, CHAPTERS);
        uriMatcher.addURI(authority, GDGContract.PATH_CHAPTERS + "/*", CHAPTER);
        uriMatcher.addURI(authority, GDGContract.PATH_CHAPTERS + "/*/" + GDGContract.PATH_EVENTS, CHAPTER_EVENTS);
        uriMatcher.addURI(authority, GDGContract.PATH_EVENTS + "/*", EVENT);

        // 3) Return the new matcher!
        return uriMatcher;
    }

    private Cursor getChapterInformation(Uri uri, String[] projection, String sortOrder) {
        String chapterId = GDGContract.ChapterEntry.getChapterGoogleIdFromUri(uri);

        String[] selectionArgs = new String[]{chapterId};
        String selection = sChapterSelectionByGoogleId;

        return sChapterQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getChapterEvents(Uri uri, String[] projection, String sortOrder) {
        String chapterId = GDGContract.ChapterEntry.getChapterGoogleIdFromUri(uri);

        String[] selectionArgs = new String[]{chapterId};
        String selection = sChapterEventsSelection;

        return sEventByChapterQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getEventInformation(Uri uri, String[] projection, String sortOrder) {
        String eventId = GDGContract.EventEntry.getEventIdFromUri(uri);

        String[] selectionArgs = new String[]{eventId};
        String selection = sChapterSingleEventSelectionByGoogleId;

        return sEventQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case CHAPTER:
            {
                retCursor = getChapterInformation(uri, projection, sortOrder);
                break;
            }

            case CHAPTER_EVENTS: {
                retCursor = getChapterEvents(uri, projection, sortOrder);
                break;
            }

            case EVENT: {
                retCursor = getEventInformation(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unsupported uri for query: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private String getChapterGIdFromRowId(SQLiteDatabase db, long _id) {
        String[] columns = new String[]{GDGContract.ChapterEntry.COLUMN_CHAPTER_GID};
        String selection = sChapterSelectionByRowId;
        String[] selectionArgs = new String[]{String.valueOf(_id)};
        Cursor cursor = db.query(GDGContract.ChapterEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // verify that an entry exists
        if (!cursor.moveToFirst()) {
            return null;
        }

        return cursor.getString(0);
    }

    private String getEventGIdFromRowId(SQLiteDatabase db, long _id) {
        String[] columns = new String[]{GDGContract.EventEntry.COLUMN_EVENT_GID};
        String selection = sChapterSingleEventSelectionByRowId;
        String[] selectionArgs = new String[]{String.valueOf(_id)};
        Cursor cursor = db.query(GDGContract.EventEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // verify that an entry exists
        if (!cursor.moveToFirst()) {
            return null;
        }

        return cursor.getString(0);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CHAPTERS: {
                long _id = db.insert(GDGContract.ChapterEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    String chapterGId = getChapterGIdFromRowId(db, _id);
                    returnUri = GDGContract.ChapterEntry.buildChapterUri(chapterGId);
                    Log.d(LOG_TAG, "Successfully inserted chapter " + GDGContract.ChapterEntry.getChapterGoogleIdFromUri(returnUri));
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case EVENT: {
                long _id = db.insert(GDGContract.EventEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    String eventGId = getEventGIdFromRowId(db, _id);
                    returnUri = GDGContract.EventEntry.buildEventUri(eventGId);
                }
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported uri for insert: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        // UnsupportedOperationException is thrown below

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        String tableName = null;
        switch (match) {
            case CHAPTER: {
                tableName = GDGContract.ChapterEntry.TABLE_NAME;
                break;
            }
            case EVENT: {
                tableName = GDGContract.EventEntry.TABLE_NAME;
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported uri for delete: " + uri);
            }
        }

        if ( null == selection ) selection = "1";
        int rowsDeleted = db.delete(tableName, selection, selectionArgs);

        if (rowsDeleted != 0 )
            getContext().getContentResolver().notifyChange(uri, null);

        // Student: return the actual rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        // UnsupportedOperationException is thrown below

        String tableName = null;
        switch (match) {
            case CHAPTER: {
                tableName = GDGContract.ChapterEntry.TABLE_NAME;
                break;
            }
            case EVENT: {
                tableName = GDGContract.EventEntry.TABLE_NAME;
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if ( null == selection ) selection = "1";
        int rowsUpdated = db.update(tableName, values, selection, selectionArgs);

        if (rowsUpdated != 0 )
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EVENT:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(GDGContract.EventEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
