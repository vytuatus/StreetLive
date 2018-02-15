package com.example.vytuatus.streetlive;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

import static com.example.vytuatus.streetlive.MainActivity.BAND_LIST;
import static com.example.vytuatus.streetlive.MainActivity.USERS_CHILD;

public class CreateStreetEvent extends AppCompatActivity {

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String EVENTS_CHILD = "events";
    public static final String EVENTS_IN_BAND = "eventsInBand";
    private String mBandName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_street_event);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Get shared preferences and get the bandNames from there that were already pre-fetched by
        // Utility
        mBandName = getIntent().getStringExtra(OneArtistProfile.LAUNCH_CREATE_EVENT_INTENT_KEY);

        DatabaseReference eventDatabaseReference = mFirebaseDatabaseReference.push();
        final String eventReferenceId = eventDatabaseReference.getKey();

        DatabaseReference eventReference = FirebaseDatabase.getInstance().
                getReference(EVENTS_CHILD).child(eventReferenceId);
        DatabaseReference userEventReference = FirebaseDatabase.getInstance().
                getReference(USERS_CHILD).
                child(mFirebaseUser.getUid()).
                child(BAND_LIST).
                child(mBandName).
                child(EVENTS_IN_BAND).
                child(eventReferenceId);

        //make a hashmap to store the time when the event was added
        HashMap<String, Object> timestampCreated = new HashMap<>();
        timestampCreated.put(FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        StreetEvent streetEvent = new StreetEvent(
                mBandName,
                "Rock",
                "Mes esam Loxai",
                "https://i.ebayimg.com/images/g/nlMAAOSwAYtWPfzf/s-l300.jpg",
                11.11,
                12.12,
                timestampCreated);
        eventReference.setValue(streetEvent);
        userEventReference.setValue(streetEvent);
    }
}
