package com.jaywhitsitt.gdgkc.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jay on 4/20/15.
 */
public class GDGDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "weather.db";

    public GDGDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create "weather" table
        final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE " + GDGContract.EventEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                GDGContract.EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                GDGContract.EventEntry.COLUMN_EVENT_GID + " TEXT NOT NULL, " +

                // the ID of the location entry associated with this weather data
                GDGContract.EventEntry.COLUMN_CHAPTER_KEY + " INTEGER NOT NULL, " +
                GDGContract.EventEntry.COLUMN_START_DATE + " TEXT NOT NULL, " +
                GDGContract.EventEntry.COLUMN_END_DATE + " TEXT NOT NULL," +
                GDGContract.EventEntry.COLUMN_ALL_DAY + " INTEGER NOT NULL, " +

                GDGContract.EventEntry.COLUMN_TITLE + " TEXT NOT NULL, " +

                GDGContract.EventEntry.COLUMN_ABOUT + " TEXT NOT NULL, " +
                GDGContract.EventEntry.COLUMN_EVENT_URL + " TEXT NOT NULL, " +
                GDGContract.EventEntry.COLUMN_ICON_URL + " TEXT NOT NULL, " +
                GDGContract.EventEntry.COLUMN_LOCATION + " TEXT NOT NULL, " +
                GDGContract.EventEntry.COLUMN_PARTICIPANTS + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + GDGContract.EventEntry.COLUMN_CHAPTER_KEY + ") REFERENCES " +
                GDGContract.ChapterEntry.TABLE_NAME + " (" + GDGContract.ChapterEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + GDGContract.EventEntry.COLUMN_EVENT_GID + ", " +
                GDGContract.EventEntry.COLUMN_CHAPTER_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_EVENT_TABLE);

        // Create "location" table
        final String SQL_CREATE_CHAPTER_TABLE = "CREATE TABLE " + GDGContract.ChapterEntry.TABLE_NAME + " (" +
                GDGContract.ChapterEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GDGContract.ChapterEntry.COLUMN_CHAPTER_GID + " TEXT UNIQUE NOT NULL, " +
                GDGContract.ChapterEntry.COLUMN_CHAPTER_NAME + " TEXT NOT NULL, " +
                GDGContract.ChapterEntry.COLUMN_CHAPTER_CITY + " TEXT NOT NULL, " +
                GDGContract.ChapterEntry.COLUMN_CHAPTER_STATUS + " TEXT NOT NULL" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_CHAPTER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GDGContract.ChapterEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GDGContract.EventEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
