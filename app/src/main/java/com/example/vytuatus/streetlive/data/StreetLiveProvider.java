package com.example.vytuatus.streetlive.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class StreetLiveProvider extends ContentProvider {

    //the constants will be used in URI matcher. Just to make life easier when matching a URI
    //With the data action they want to perform

    public static final int CODE_ALL_MAIN_STREET_EVENTS = 100;
    public static final int CODE_SPECIFIC_STREET_EVENT = 101;

    //declare URI matcher
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StreetLiveContract.CONTENT_AUTHORITY;

        // This Uri will match the uri below will fetch all data in the table
        // content://com.example.vytuatus.streetlive/*
        matcher.addURI(authority, StreetLiveContract.PATH_MAIN, CODE_ALL_MAIN_STREET_EVENTS);
        matcher.addURI(authority, StreetLiveContract.PATH_MAIN + "/#", CODE_SPECIFIC_STREET_EVENT);

        return matcher;
    }

    private StreetLiveDbHelper mStreetLiveDbOpenHelper;

    @Override
    public boolean onCreate() {
        mStreetLiveDbOpenHelper = new StreetLiveDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_SPECIFIC_STREET_EVENT:
                String mainEventId = uri.getLastPathSegment();
                String[] selectionArgumentsMain = new String[]{mainEventId};

                cursor = mStreetLiveDbOpenHelper.getReadableDatabase().query(
                        StreetLiveContract.StreetLiveEntry.TABLE_NAME_MAIN,
                        projection,
                        StreetLiveContract.StreetLiveEntry.COLUMN_EVENT_ID + " = ? ",
                        selectionArgumentsMain,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_ALL_MAIN_STREET_EVENTS:
                cursor = mStreetLiveDbOpenHelper.getReadableDatabase().query(
                        StreetLiveContract.StreetLiveEntry.TABLE_NAME_MAIN,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //User of will expect to see how many rows were deleted
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)){
            case CODE_ALL_MAIN_STREET_EVENTS:
                numRowsDeleted = mStreetLiveDbOpenHelper.getWritableDatabase().delete(
                        StreetLiveContract.StreetLiveEntry.TABLE_NAME_MAIN,
                        selection,
                        selectionArgs
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri for Delete: " + uri);
        }

        if (numRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mStreetLiveDbOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)){
            case CODE_ALL_MAIN_STREET_EVENTS:
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for (ContentValues value: values){
                        long _id = db.insert(StreetLiveContract.StreetLiveEntry.TABLE_NAME_MAIN,
                                null,
                                value);
                        if (_id != -1){
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }

    }
}
