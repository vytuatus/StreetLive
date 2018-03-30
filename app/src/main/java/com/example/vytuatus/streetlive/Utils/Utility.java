package com.example.vytuatus.streetlive.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.ArtistProfile;
import com.example.vytuatus.streetlive.CreateBand;
import com.example.vytuatus.streetlive.MainActivity;
import com.example.vytuatus.streetlive.R;
import com.example.vytuatus.streetlive.model.Band;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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


}
