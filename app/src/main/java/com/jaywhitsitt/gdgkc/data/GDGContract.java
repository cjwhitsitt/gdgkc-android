package com.jaywhitsitt.gdgkc.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by jay on 4/20/15.
 */
public class GDGContract {

    /* Content provider stuff */

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.jaywhitsitt.gdgkc.provider";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_CHAPTERS = "chapters";
    public static final String PATH_EVENTS = "events";

    /* SQL query stuff */

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the contents of the chapter table */
    public static final class ChapterEntry implements BaseColumns {

        public static final String TABLE_NAME = "chapter";

        public static final String COLUMN_CHAPTER_GID = "chapter_gid";
        public static final String COLUMN_CHAPTER_NAME = "name";
        public static final String COLUMN_CHAPTER_CITY = "city";
        public static final String COLUMN_CHAPTER_STATUS = "status";

        /* Content provider stuff */

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHAPTERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHAPTERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHAPTERS;

        public static Uri buildChapterUri(String id) {
            // have to manually do this because ContentUris.withAppendedId() requires a long param
            return CONTENT_URI.buildUpon().appendEncodedPath(id).build();
        }

        public static Uri buildChapterEventsUri(String chapterId) {
            return CONTENT_URI.buildUpon().appendEncodedPath(chapterId).appendPath(PATH_EVENTS).build();
        }

        public static String getChapterGoogleIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /* Inner class that defines the contents of the event table */
    public static final class EventEntry implements BaseColumns {

        public static final String TABLE_NAME = "event";

        public static final String COLUMN_EVENT_GID = "event_gid";

        // Column with the foreign key into the chapter table, based on actual _id, not GID
        public static final String COLUMN_CHAPTER_KEY = "chapter_key";

        // Date, stored as milliseconds from epoch
        public static final String COLUMN_START_DATE = "start";
        public static final String COLUMN_END_DATE = "end";

        // 0 = false, 1 = true
        public static final String COLUMN_ALL_DAY = "all_day";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ABOUT = "about";
        public static final String COLUMN_EVENT_URL = "event_url"; // String
        public static final String COLUMN_ICON_URL = "icon_url"; // String relative to developer.google.com
        public static final String COLUMN_LOCATION = "location"; // human-readable String
        public static final String COLUMN_PARTICIPANTS = "participants"; // int

        /* Content provider stuff */

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;

        public static Uri buildEventUri(String eventId) {
            return CONTENT_URI.buildUpon().appendEncodedPath(eventId).appendPath(PATH_EVENTS).build();
        }

        public static String getEventIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
