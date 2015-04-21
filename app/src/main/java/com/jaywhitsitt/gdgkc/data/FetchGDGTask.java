package com.jaywhitsitt.gdgkc.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import com.jaywhitsitt.gdgkc.NextMeetingFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by jay on 4/21/15.
 */
public class FetchGDGTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchGDGTask.class.getSimpleName();

    private final Context mContext;

    public FetchGDGTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    /**
     * Helper method to handle insertion of a new chapter into the database
     *
     * @param gid The ID provided by the API for the chapter
     * @param name The name of the chapter
     * @param city A human-readable city name, e.g "Mountain View"
     * @param status The longitude of the city
     * @return the row ID of the added location.
     */
    long addChapter(String gid, String name, String city, String status) {
        // Students: First, check if the location with this city name exists in the db
        long rowId;
        Cursor cursor = mContext.getContentResolver().query(
                GDGContract.ChapterEntry.CONTENT_URI,
                new String[]{GDGContract.ChapterEntry._ID},
                GDGContract.ChapterEntry.COLUMN_CHAPTER_GID + " = ?",
                new String[]{gid},
                null
        );

        // If it exists, return the current ID
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(GDGContract.ChapterEntry._ID);
            rowId = cursor.getLong(index);
        }
        // Otherwise, insert it using the content resolver and the base URI
        else {
            ContentValues values = new ContentValues();
            values.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_GID, gid);
            values.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_NAME, name);
            values.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_CITY, city);
            values.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_STATUS, status);

            Uri uri = mContext.getContentResolver().insert(GDGContract.ChapterEntry.CONTENT_URI, values);

            rowId = ContentUris.parseId(uri);
        }

        return rowId;
    }

    /**
     * Take the String representing the chapter in JSON Format and
     * pull out the data we need to construct the DB object.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void saveChapterDataFromJson(String chapterJsonStr)
            throws JSONException {

        Log.d(LOG_TAG, "JSON String to parse = \n" + chapterJsonStr);

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String GID_KEY = "_id";
        final String NAME_KEY = "name";
        final String CITY_KEY = "city";
        final String STATUS_KEY = "status";

        try {
            JSONObject chapterJson = new JSONObject(chapterJsonStr);

            String gid = chapterJson.getString(GID_KEY);
            String name = chapterJson.getString(NAME_KEY);
            String city = chapterJson.getString(CITY_KEY);
            String status = chapterJson.getString(STATUS_KEY);

            // Insert the new chapter information into the database
            ContentValues chapterValues = new ContentValues();

            chapterValues.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_GID, gid);
            chapterValues.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_NAME, name);
            chapterValues.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_CITY, city);
            chapterValues.put(GDGContract.ChapterEntry.COLUMN_CHAPTER_STATUS, status);

            // add to database
            mContext.getContentResolver().insert(GDGContract.ChapterEntry.CONTENT_URI, chapterValues);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String chapterJsonStr = null;

        try {
            // Construct the URL for the GDGx query
            // http://hub.gdgx.io/developers/api
            final String GDGX_BASE_URL =
                    "http://hub.gdgx.io/api/v1/";
            final String CHAPTERS_PARAM = "chapters";
            final String EVENTS_PARAM = "events";
            final String UPCOMING_PARAM = "upcoming";

            final String chapterGId = "116015988631052616691";

            Uri builtUri = Uri.parse(GDGX_BASE_URL).buildUpon()
                    .appendPath(CHAPTERS_PARAM).appendEncodedPath(chapterGId)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            chapterJsonStr = buffer.toString();
            saveChapterDataFromJson(chapterJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the chapter data, there's no point in attempting
            // to parse it.
            return null;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

    }
}
