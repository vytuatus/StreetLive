package com.example.vytuatus.streetlive;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.vytuatus.streetlive.Maps.MapsActivity;
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.example.vytuatus.streetlive.MainActivity.BAND_LIST;
import static com.example.vytuatus.streetlive.MainActivity.USERS_CHILD;

public class CreateStreetEvent extends AppCompatActivity {

    private static final String TAG = CreateStreetEvent.class.getSimpleName();

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

    private TextView mDateHeadingTextView, mStartTimeTextView, mEndTimeTextView, mEventTimeLengthInfoTextView;
    private static final long TWO_HOURS_IN_MILLIS = 2 * 60 * 60 * 1000;
    private static final int TIME_PICKER_INTERVAL = 15;
    private boolean mIgnoreEvent = false;
    private TextView mLatLngTextview;
    private Button mCreateEventButton;
    private Button mSaveEvent;
    // Variables to store current date
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String date_time;
    private Calendar mStartDateCalendarTime;
    private Calendar mEndDateCalendarTime;

    private double[] mLatLongFromMapAct;
    private String mResultAdress;
    private String mResultCity;
    private String mResultCountry;


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
                showDatePickerDialog(mStartTimeTextView, false);
            }
        });
        mStartDateCalendarTime = Calendar.getInstance();
        mEndTimeTextView = findViewById(R.id.display_end_time_textView);
        mEndTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(mEndTimeTextView, true);
            }
        });
        mEndDateCalendarTime = Calendar.getInstance();
        mEventTimeLengthInfoTextView = findViewById(R.id.event_time_length_info_textView);
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


    }

    private void showDatePickerDialog(final TextView startEndTextView, final boolean isEndTime) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        //*************Call Time Picker Here ********************
                        date_time = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        timePicker(startEndTextView, isEndTime, year, monthOfYear, dayOfMonth);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
        // do not allow past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    private void timePicker(final TextView startEndTextView, final boolean isEndTime, final int year,
                            final int monthOfYear, final int dayOfMonth) {


        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);


        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {


                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

                        timePicker.setIs24HourView(true);
                        timePicker.setCurrentMinute(20);
//                        setTimePickerInterval(timePicker);
                        // if it was event's end time selected, save time in Calendar variable
                        if (isEndTime){
                            mEndDateCalendarTime.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                            // Do the same for event's start time
                        } else {
                            mStartDateCalendarTime.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                        }
                        mHour = hourOfDay;
                        mMinute = minute;

                        startEndTextView.setText(date_time + " " + hourOfDay + ":" + minute);
                        Log.d(TAG, "Time is: " + year + monthOfYear + dayOfMonth + hourOfDay + minute);

                        // if diff between end and start time of event is more than 2 hours, update
                        // error info TextView
                        if (mStartDateCalendarTime != null && mEndDateCalendarTime != null){
                            if (mEndDateCalendarTime.getTimeInMillis() -
                                    mStartDateCalendarTime.getTimeInMillis() > TWO_HOURS_IN_MILLIS){
                                mEventTimeLengthInfoTextView.setVisibility(View.VISIBLE);
                                mEventTimeLengthInfoTextView.setText("event cannot be more than 2 hours");
                            } else {
                                mEventTimeLengthInfoTextView.setVisibility(View.GONE);
                            }
                        }

                    }
                }, mHour, mMinute, false);

        timePickerDialog.show();

    }

    private void setTimePickerInterval(TimePicker timePicker) {
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                if (mIgnoreEvent)
                    return;
                if (minute%TIME_PICKER_INTERVAL != 0){
                    int minuteFloor = minute - (minute%TIME_PICKER_INTERVAL);
                    minute=minuteFloor + (minute == minuteFloor+1 ? TIME_PICKER_INTERVAL : 0);
                    if (minute == 60)
                        minute = 0;
                    mIgnoreEvent = true;
                    timePicker.setCurrentMinute(minute);
                    mIgnoreEvent = false;
                }
            }
        });
    }

    //get the lat/long/adress data from the maps activity and use it to in addLocation() method
    //to save that info in Firebase
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 0:
                if(resultCode == Activity.RESULT_OK){
                    mLatLongFromMapAct = data.getDoubleArrayExtra(MapsActivity.INTENT_KEY_RESULT_LAT_LONG);
                    mResultAdress = data.getStringExtra(MapsActivity.INTENT_KEY_RESULT_ADRESS);
                    mResultCity = data.getStringExtra(MapsActivity.INTENT_KEY_RESULT_CITY_NAME);
                    mResultCountry = data.getStringExtra(MapsActivity.INTENT_KEY_RESULT_COUNTRY_NAME);
                    Utility.saveSelectedLatLngToSharedPrefs(this, mLatLongFromMapAct);
                    mLatLngTextview.setText(mResultAdress);
                }
                break;

            default:
                if (resultCode == Activity.RESULT_CANCELED) {
                    //Write your code if there's no result
                }
                break;
        }
    }

    // Save selected Event to the database
    public void onEventSaveToDatabase (View v){
        createEventInDatabase();
        finish();
    }

    private void createEventInDatabase() {

        DatabaseReference eventDatabaseReference = mFirebaseDatabaseReference.push();
        final String eventReferenceId = eventDatabaseReference.getKey();

        DatabaseReference eventReference = FirebaseDatabase.getInstance().
                getReference(EVENTS_CHILD).
                child(mResultCity).
                child(eventReferenceId);
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

        // Convert Local time to UTC time
        // Utility.localToGMT();
        long utcEventStartTime = Utility.localToGMT(mStartDateCalendarTime.getTimeInMillis());
        long utcEventEndTime = Utility.localToGMT(mEndDateCalendarTime.getTimeInMillis());

        StreetEvent streetEvent = new StreetEvent(
                mBandName,
                mBandGenre,
                mBandDescription,
                mBandNamePhotoUrl,
                mResultCountry,
                mResultCity,
                mLatLongFromMapAct[0],
                mLatLongFromMapAct[1],
                utcEventStartTime,
                utcEventEndTime,
                timestampCreated);
        eventReference.setValue(streetEvent);
        userEventReference.setValue(streetEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.saveSelectedLatLngToSharedPrefs(this, new double[]{0, 0});
    }
}
