package com.example.vytuatus.streetlive.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.vytuatus.streetlive.data.StreetLiveContract.StreetLiveEntry;

public class StreetLiveDbHelper extends SQLiteOpenHelper {

    //This is the name of the street events database
    public static final String DATABASE_NAME = "streetlive.db";
    private static final int DATABASE_VERSION = 1;

    public StreetLiveDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_STREET_LIVE_MAIN_TABLE =
                "CREATE TABLE " + StreetLiveEntry.TABLE_NAME_MAIN + " (" +
                        StreetLiveEntry._ID                     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StreetLiveEntry.COLUMN_EVENT_ID         + " INTEGER NOT NULL, "                  +
                        StreetLiveEntry.COLUMN_BAND_NAME      + " TEXT NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_EVENT_CITY     + " TEXT NOT NULL, "                  +
                        StreetLiveEntry.COLUMN_EVENT_COUNTRY + " TEXT NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_BAND_DESCRIPTION + " TEXT NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_EVENT_START_TIME + " REAL NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_EVENT_END_TIME + " REAL NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_BAND_GENRE + " TEXT NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_EVENT_LAT + " REAL NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_EVENT_LNG + " REAL NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_PHOTO_URL + " TEXT NOT NULL, "                     +
                        StreetLiveEntry.COLUMN_TIMESTAMP_CREATED + " REAL NOT NULL, "                     +
                        //Since we don't want the movie with the same id present in the database
                        //a few times, we define the movie id as unique. This will ensure that
                        //when we insert the movie with the same movie id, the old one will sinply
                        //be replaced
                        " UNIQUE (" + StreetLiveEntry.COLUMN_EVENT_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_STREET_LIVE_MAIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //We don't want to drop the table. Our user will not be happy not to find
        //their favorite movies after database upgrade.
        //For now we don't do anything
    }
}
