package com.example.vytuatus.streetlive;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.Maps.MapsActivity;
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Calendar;
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
    private String mBandNamePhotoUrl;
    private String mBandGenre;
    private String mBandDescription;

    private TextView mDateHeadingTextView, mStartTimeTextView, mEndTimeTextView;
    private TextView mLatLngTextview;
    private Button mCreateEventButton;
    // Variables to store current date
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String date_time;

    private double[] mLatLongFromMapAct;
    private String mResultAdress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_street_event);

        mLatLngTextview = findViewById(R.id.latLng_textView);
        mDateHeadingTextView = findViewById(R.id.display_selected_date_textView);
        mStartTimeTextView = findViewById(R.id.display_start_time_textView);
        mCreateEventButton = findViewById(R.id.button_select_location);
        mStartTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show DatePicker and TimePicker Dialogs
                showDatePickerDialog();
            }
        });
        mEndTimeTextView = findViewById(R.id.display_end_time_textView);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Get shared preferences and get the bandNames from there that were already pre-fetched by
        // Utility
        mBandName = getIntent().getStringExtra(ArtistProfileFragment.PASS_BAND_NAME_INTENT_KEY);
        mBandNamePhotoUrl = getIntent().getStringExtra(ArtistProfileFragment.PASS_BAND_PHOTO_URL_INTENT_KEY);
        mBandGenre = getIntent().getStringExtra(ArtistProfileFragment.PASS_BAND_GENRE_INTENT_KEY);
        mBandDescription = getIntent().getStringExtra(ArtistProfileFragment.PASS_BAND_DESCRIPTION_INTENT_KEY);

        mCreateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CreateStreetEvent.this, MapsActivity.class);
                startActivityForResult(i, 0);
            }
        });

        //createEventInDatabase();
    }

    private void createEventInDatabase() {

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
                mBandGenre,
                mBandDescription,
                mBandNamePhotoUrl,
                11.11,
                12.12,
                timestampCreated);
        eventReference.setValue(streetEvent);
        userEventReference.setValue(streetEvent);
    }

    private void showDatePickerDialog() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        date_time = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        //*************Call Time Picker Here ********************
                        timePicker();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void timePicker() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        mStartTimeTextView.setText(date_time + " " + hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();

    }

    //get the lat/long/adress data from the maps activity and use it to in addLocation() method
    //to save that info in Firebase
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 0:
                if(resultCode == Activity.RESULT_OK){
                    mLatLongFromMapAct = data.getDoubleArrayExtra(MapsActivity.INTENT_KEY_LAT_LONG);
                    mResultAdress = data.getStringExtra(MapsActivity.INTENT_KEY_RESULT_ADRESS);
                    Log.d("latitude is", String.valueOf(mLatLongFromMapAct[0]));
                    Log.d("longitude is", String.valueOf(mLatLongFromMapAct[1]));
                    Utility.saveSelectedLatLngToSharedPrefs(this, mLatLongFromMapAct);
                    mLatLngTextview.setText(mLatLongFromMapAct[0] + " " + mLatLongFromMapAct[1]);
                }
                break;

            default:
                if (resultCode == Activity.RESULT_CANCELED) {
                    //Write your code if there's no result
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.saveSelectedLatLngToSharedPrefs(this, new double[]{0, 0});
    }
}
