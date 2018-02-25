package com.example.vytuatus.streetlive.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.ArtistProfile;
import com.example.vytuatus.streetlive.CreateBand;
import com.example.vytuatus.streetlive.MainActivity;
import com.example.vytuatus.streetlive.R;
import com.example.vytuatus.streetlive.model.Band;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
                if (dataSnapshot.hasChildren()){
                    StringBuilder sb = new StringBuilder();

                    // Iterate through all the bands user has and store them as string in Shared Prefs
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                        sb.append(postSnapshot.getKey()).append(",");
                    }
                    editor.putString(context.getString(R.string.band_names_pref_key), sb.toString());

                    if (context instanceof CreateBand){
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
    public static String[] getNumberOfBands(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String[] bandNames = sp.getString(context.getString(R.string.band_names_pref_key), "empty").
                split(",");

        return bandNames;
    }

    public static void saveSelectedLatLngToSharedPrefs(Context context, double[] latLng){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(context.getString(R.string.latitude_pref_key), Double.doubleToLongBits(latLng[0]));
        editor.putLong(context.getString(R.string.longitude_pref_key), Double.doubleToLongBits(latLng[1]));
        editor.commit();

    }

    public static double[] getSelectedLatLngFromSharedPrefs(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        double latitude = Double.longBitsToDouble(
                sp.getLong(context.getString(R.string.latitude_pref_key),
                        Double.doubleToLongBits(11.1111)));
        double longitude = Double.longBitsToDouble(
                sp.getLong(context.getString(R.string.longitude_pref_key),
                        Double.doubleToLongBits(11.1111)));

        return new double[]{latitude, longitude};

    }

}
