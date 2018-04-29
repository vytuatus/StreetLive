package com.example.vytuatus.streetlive.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.ArtistProfile;
import com.example.vytuatus.streetlive.CreateBand;
import com.example.vytuatus.streetlive.MainActivity;
import com.example.vytuatus.streetlive.Maps.MapsActivity;
import com.example.vytuatus.streetlive.R;
import com.example.vytuatus.streetlive.model.Band;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.example.vytuatus.streetlive.MainActivity.BAND_LIST;
import static com.example.vytuatus.streetlive.MainActivity.USERS_CHILD;

/**
 * Created by vytuatus on 2/6/18.
 */

public class Utility {

    private static final String TAG = Utility.class.getSimpleName();

    // Method that updates the shared pref variable which holds which fragment should be loaded when
    // user goes to check his bands
    public static void updateNumberOfBands(FirebaseUser firebaseUser,
                                           DatabaseReference databaseReference,
                                           final Context context) {
        // Got to artist profile
        String userId = firebaseUser.getUid();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sp.edit();

        final DatabaseReference bandDatabaseReference = databaseReference.
                child(USERS_CHILD).
                child(userId).
                child(BAND_LIST);
        bandDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    StringBuilder sb = new StringBuilder();

                    // Iterate through all the bands user has and store them as string in Shared Prefs
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        sb.append(postSnapshot.getKey()).append(",");
                    }
                    editor.putString(context.getString(R.string.band_names_pref_key), sb.toString());

                    if (context instanceof CreateBand) {
                        Intent intent = new Intent(context, ArtistProfile.class);
                        context.startActivity(intent);
                    }

                } else {

                    editor.putString(context.getString(R.string.band_names_pref_key), "empty");
                }

                editor.putBoolean(context.getString(R.string.update_number_of_bands_process_complete), true).commit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                editor.putBoolean(context.getString(R.string.update_number_of_bands_process_complete), true).commit();
            }
        });

    }

    // get the number of Bands from the shared Preferences
    public static String[] getNumberOfBands(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String[] bandNames = sp.getString(context.getString(R.string.band_names_pref_key), "empty").
                split(",");

        return bandNames;
    }

    public static void saveSelectedLatLngToSharedPrefs(Context context, double[] latLng) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(context.getString(R.string.latitude_pref_key), Double.doubleToLongBits(latLng[0]));
        editor.putLong(context.getString(R.string.longitude_pref_key), Double.doubleToLongBits(latLng[1]));
        editor.commit();

    }

    public static double[] getSelectedLatLngFromSharedPrefs(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        double latitude = Double.longBitsToDouble(
                sp.getLong(context.getString(R.string.latitude_pref_key),
                        Double.doubleToLongBits(0)));
        double longitude = Double.longBitsToDouble(
                sp.getLong(context.getString(R.string.longitude_pref_key),
                        Double.doubleToLongBits(0)));

        return new double[]{latitude, longitude};

    }

    /**
     * Save selected cityName in MainActivity by user in Shared Prefs
     * @param context
     * @param cityName city name selected by user from autoCompTextView
     */
    public static void saveSelectedCityNameToSharedPrefs(Context context, String cityName) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.cityName_pref_key), cityName);
        editor.commit();
    }

    // get the previously selected city name from Shared prefs
    public static String getSelectedCityNameFromSharedPrefs(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.cityName_pref_key),
                context.getString(R.string.cityName_pref_default));

    }

    /**
     * @param context
     * @param daySelected is the selected day "today", "tomorrow" and ect in the filter
     */
    public static void saveSelectedDayFilterToSharedPrefs(Context context, String daySelected){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.daySelected_pref_key), daySelected);
        editor.commit();

    }

    // Convert local time to UTC
    public static long localToGMT(long localTime) {
        Date date = new Date(localTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utc = new Date(sdf.format(date));
        return utc.getTime();

    }

    // Covert UTC time to Local time
    public static long gmttoLocalDate(long utcTime) {
        Date date = new Date(utcTime);
        String timeZone = Calendar.getInstance().getTimeZone().getID();
        Date local = new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
        return local.getTime();
    }

    /**
     * Get the time interval for a selected day in Filter in UTC
     * @param daySelected in the filter
     * @return time interval of selected day/period in UTC time from 00:00 to 24:00
     */
    public static long[] getSelectedDayFilterInterval(String daySelected){

        long startTime;
        long endTime;
        long[] timeInterval = new long[2];
        Calendar cal = Calendar.getInstance();
        Log.d(TAG, "Local timezone " + cal.getTimeInMillis());

        if (daySelected.equals("Today")){

            // get the start time of today by setting the local time to 00:00 o'clock
            cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            // Get the today's date start time in UTC
            startTime = localToGMT(cal.getTimeInMillis());

            // get the end time of today by setting the local time to 24:00 o'clock
            cal.set(Calendar.HOUR_OF_DAY, 24); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            // Get the today's date end time in UTC
            endTime = localToGMT(cal.getTimeInMillis());

        } else if (daySelected.equals("Tomorrow")){

            // add one day to get the tomorrow's day
            cal.add(Calendar.DAY_OF_YEAR, 1);
            // get the start time of tomorrow by setting the local time to 00:00 o'clock
            cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            // Get the tomorrow's date start time in UTC
            startTime = localToGMT(cal.getTimeInMillis());

            // get the end time of tomorrow by setting the local time to 24:00 o'clock
            cal.set(Calendar.HOUR_OF_DAY, 24); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            // Get the tomorrow's date end time in UTC
            endTime = localToGMT(cal.getTimeInMillis());

        } else if (daySelected.equals("Day After Tomorrow")){
            // add two days to get the day after tomorrow's day
            cal.add(Calendar.DAY_OF_YEAR, 2);
            // get the start time of tomorrow by setting the local time to 00:00 o'clock
            cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            // Get the tomorrow's date start time in UTC
            startTime = localToGMT(cal.getTimeInMillis());

            // get the end time of tomorrow by setting the local time to 24:00 o'clock
            cal.set(Calendar.HOUR_OF_DAY, 24); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            // Get the tomorrow's date end time in UTC
            endTime = localToGMT(cal.getTimeInMillis());

        } else {
            //else Weekend

            // Set the calendar to Saturday to get start date
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero

            // Get the weekends start time in UTC
            startTime = localToGMT(cal.getTimeInMillis());

            // add 1 day, so we get Sunday and also set the the local time to 24:00 o'clock to get End of Sunday
            cal.add(Calendar.DAY_OF_YEAR, 1);;
            cal.set(Calendar.HOUR_OF_DAY, 24); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            // Get the tomorrow's date end time in UTC
            endTime = localToGMT(cal.getTimeInMillis());
        }

        timeInterval[0] = startTime;
        timeInterval[1] = endTime;
        return timeInterval;
    }

    // get a friendly time string
    public static String getFriendlyTime(long eventTime){
        Date date = new Date(eventTime);
        String friendlyDate = date.getHours() + ":" + date.getMinutes();
        return friendlyDate;
    }

    // get a friendly time string
    public static String getFriendlyDate(long eventTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eventTime);
        String friendlyDate = (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        return friendlyDate;
    }

    /**
     * @param context
     * @param latitude events latitude
     * @param longitude events longitude
     * @return returns full event adress
     */
    public static String getAddressFromLatLng(Context context, double latitude, double longitude){

        Geocoder mGeocoder;
        StringBuilder addressStringBuilder = new StringBuilder();

        mGeocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses = new ArrayList<>();
        try {
            addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If you click a sea location ex., there will be no address. account for that
        if (addresses.size() > 0){
            android.location.Address address = addresses.get(0);

            if (address != null) {
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressStringBuilder.append(address.getAddressLine(i) + "\n");
                }

            }
        }

        return addressStringBuilder.toString();
    }


}
