package com.example.vytuatus.streetlive.Maps;

import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.vytuatus.streetlive.R;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventMapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String TAG = MapsActivity.class.getSimpleName();
    public static final String INTENT_KEY_EVENT_LAT = "intent_key_event_lat";
    public static final String INTENT_KEY_EVENT_LNG = "intent_key_event_lng";
    private static final double defaultVilniusLat = 54.68665982073372;
    private static final double defaultVilniusLng = 25.279131643474102;

    private GoogleMap mMap;
    Geocoder mGeocoder;


    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ValueEventListener mLocationListener;
    public static final String EVENTS_CHILD = "events";
    private DatabaseReference mEventsDatabaseReference;

    // holds variables of the event that was displayed in Details Activity
    private double mSelectedEventLat, mSelectedEventLng;

    // Place lat and lng fetched from Firebase to a list variables
    private List<Marker> mMarkersFromDb = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.events_map);

        // get coordinates from selected event that is displayed in DetailsActivity
        if (getIntent() != null &&
                getIntent().hasExtra(INTENT_KEY_EVENT_LAT) &&
                getIntent().hasExtra(INTENT_KEY_EVENT_LNG)){
            mSelectedEventLat = getIntent().getDoubleExtra(INTENT_KEY_EVENT_LAT, defaultVilniusLat);
            mSelectedEventLng = getIntent().getDoubleExtra(INTENT_KEY_EVENT_LNG, defaultVilniusLng);
        } else {
            mSelectedEventLat = defaultVilniusLat;
            mSelectedEventLng = defaultVilniusLng;
        }

        // instantiate Firebase variables
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mGeocoder = new Geocoder(this, Locale.getDefault());

        mEventsDatabaseReference = FirebaseDatabase.getInstance().getReference(EVENTS_CHILD);

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // zoom camera to the event location that was selected by user in DetailsActivity
        LatLng selectedEventLatLng = new LatLng(mSelectedEventLat,
                mSelectedEventLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedEventLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        // Get existing locations from the Firebase Database
        mLocationListener = mEventsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Count " ,"" + dataSnapshot.getChildrenCount());
                // we are getting the https://streetmusic-a3c02.firebaseio.com/events children
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    // we are getting the https://streetmusic-a3c02.firebaseio.com/events/"cityname" children
                    for (DataSnapshot eventSnapshot: postSnapshot.getChildren()){
                        StreetEvent streetEvent = eventSnapshot.getValue(StreetEvent.class);
                        LatLng latLngFromDatabase = new LatLng(streetEvent.getLat(), streetEvent.getLng());
                        Marker markerFromDb = mMap.addMarker(new MarkerOptions().position(latLngFromDatabase).title(streetEvent.getBandName())
                                .snippet(streetEvent.getBandName()));

                        mMarkersFromDb.add(markerFromDb);
                        //Draw circles around existing locations fetched from Firebase
                        Circle circle = mMap.addCircle(new CircleOptions()
                                .center(latLngFromDatabase)
                                .radius(20) //20 meters
                                .strokeColor(Color.rgb(0, 136, 255))
                                .fillColor(Color.argb(20, 0, 136, 255)));
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
