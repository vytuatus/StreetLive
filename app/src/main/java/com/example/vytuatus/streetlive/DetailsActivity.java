package com.example.vytuatus.streetlive;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.Band;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.vytuatus.streetlive.MainActivity.BAND_LIST;
import static com.example.vytuatus.streetlive.MainActivity.EVENTS_CHILD;
import static com.example.vytuatus.streetlive.MainActivity.USERS_CHILD;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String INTENT_KEY_EVENT_ID = "intent_key_event_id";

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private String mEventId;

    private SharedPreferences mSharedPreferences;
    private String mCityName;

    private TextView mBandNameTextView;
    private TextView mGenreTextView;
    private TextView mBandDescriptionTextView;
    private ImageView mImageView;
    private TextView mAddressTextView;
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;

    private String mBandPhotoUrl;
    private long mStartTime;
    private long mEndTime;
    private double mEventLat;
    private double mEventLng;
    private String mEventAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (getIntent() != null && getIntent().hasExtra(INTENT_KEY_EVENT_ID)){
            mEventId = getIntent().getStringExtra(INTENT_KEY_EVENT_ID);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCityName = mSharedPreferences.getString(
                getString(R.string.cityName_pref_key), // cityName Pref key
                getString(R.string.cityName_pref_default)); // cityName pref default

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mBandNameTextView = findViewById(R.id.bandName_textView);
        mGenreTextView = findViewById(R.id.bandGenre_textView);
        mBandDescriptionTextView = findViewById(R.id.bandDescription_textView);
        mImageView = findViewById(R.id.band_imageView);
        mAddressTextView = findViewById(R.id.address_textView);
        mStartTimeTextView = findViewById(R.id.startTime_textView);
        mEndTimeTextView = findViewById(R.id.endTime_textView);

        // fetch band information from Firebase Database
        fetchEventInfoFromFirebase();


    }

    // Fetch band info from Firebase and update the UI
    private void fetchEventInfoFromFirebase() {
        DatabaseReference bandDatabaseReference = mFirebaseDatabaseReference
                .child(EVENTS_CHILD)
                .child(mCityName)
                .child(mEventId);
        bandDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StreetEvent streetEvent = dataSnapshot.getValue(StreetEvent.class);

                mStartTime = streetEvent.getStartTime();
                mEndTime = streetEvent.getEndTime();
                mEventLat = streetEvent.getLat();
                mEventLng = streetEvent.getLng();
                mEventAddress = Utility.getAddressFromLatLng(DetailsActivity.this,
                        mEventLat,
                        mEventLng);

                mBandNameTextView.setText(streetEvent.getBandName());
                mGenreTextView.setText(streetEvent.getGenre());
                mBandDescriptionTextView.setText(streetEvent.getDescription());
                mBandPhotoUrl = streetEvent.getPhotoUrl();
                mAddressTextView.setText(mEventAddress);
                mStartTimeTextView.setText(Utility.getFriendlyTime(mStartTime));
                mEndTimeTextView.setText(Utility.getFriendlyTime(mEndTime));

                // If url starts with "gs" it means there is an image stored in Firebase for this user
                if (mBandPhotoUrl != null){
                    // If Firebase holds the image, then
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(mBandPhotoUrl);
                    storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                //String downloadUrl = task.getResult().getScheme();
                                Log.d(TAG, mBandPhotoUrl);
                                Glide.with(DetailsActivity.this)
                                        .load(mBandPhotoUrl)
                                        .into(mImageView);
                            } else {
                                Log.w(TAG, "Getting download url was not successful.",
                                        task.getException());
                            }
                        }
                    });

                } else {
                    //If it doesn't start with "gs" - not Firebase image. Still fetch and load from net
                    Log.d(TAG, "load the fallback Icon");
                    Glide.with(DetailsActivity.this)
                            .load(R.drawable.ic_launcher_background)
                            .into(mImageView);
                }

                // We first need to load the band related info and update all variables, so that
                // they store the Firebase information and only then load the events recycler view.

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
