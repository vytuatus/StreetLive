package com.example.vytuatus.streetlive.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class StreetLiveContract {

    //Contract Authority
    public static final String CONTENT_AUTHORITY = "com.example.vytuatus.streetlive";

    //base URI
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Path to the main table
    public static final String PATH_MAIN = "main";


    public static final class StreetLiveEntry implements BaseColumns {

        //Content URI for main table
        public static final Uri CONTENT_URI_MAIN = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MAIN)
                .build();

        //Our SQL main movie table name
        public static final String TABLE_NAME_MAIN = "main";

        //column for movie id
        public static final String COLUMN_EVENT_ID = "event_id";

        //column for movie titles
        public static final String COLUMN_BAND_NAME = "band_name";

        //column for release date
        public static final String COLUMN_EVENT_CITY = "event_city";

        //column for movie poster URL
        public static final String COLUMN_EVENT_COUNTRY = "event_country";

        //column for vote average
        public static final String COLUMN_BAND_DESCRIPTION = "band_description";

        //column for plot synopsis
        public static final String COLUMN_EVENT_START_TIME = "start_time";

        //column for plot synopsis
        public static final String COLUMN_EVENT_END_TIME = "end_time";

        //column for plot synopsis
        public static final String COLUMN_BAND_GENRE = "band_genre";

        //column for plot synopsis
        public static final String COLUMN_EVENT_LAT = "event_lat";

        //column for plot synopsis
        public static final String COLUMN_EVENT_LNG = "event_lng";

        //column for plot synopsis
        public static final String COLUMN_PHOTO_URL = "photo_url";

        //column for plot synopsis
        public static final String COLUMN_TIMESTAMP_CREATED = "timestamp_created";

        //returns the Uri for a specific event in main table.
        // This would be used to get a reference to a favorite movie in the detail activity
        public static Uri buildMainEventUriWithId(int id){
            return CONTENT_URI_MAIN.buildUpon()
                    .appendPath(String.valueOf(id))
                    .build();
        }
    }
}
