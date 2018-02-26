package com.example.vytuatus.streetlive.Maps;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.R;
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String INTENT_KEY_RESULT_ADRESS = "resultAdress";
    public static final String INTENT_KEY_LAT_LONG = "latLong";
    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    Geocoder mGeocoder;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ValueEventListener mLocationListener;
    public static final String EVENTS_CHILD = "events";
    private DatabaseReference mEventsDatabaseReference;

    private double longitude;
    private double latitude;
    private LatLng latLng;
    private StringBuilder mAdressStringBuilder;
    private Marker mMarker;
    private double[] mLatLongFromPreviousPick;
    private Marker mPreviouslySelectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mGeocoder = new Geocoder(this, Locale.getDefault());

        mEventsDatabaseReference = FirebaseDatabase.getInstance().getReference(EVENTS_CHILD);

        // Get the previous location from shared preferences
        mLatLongFromPreviousPick = Utility.getSelectedLatLngFromSharedPrefs(this);
        Log.d(TAG, "" + mLatLongFromPreviousPick[0]);

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if  (mLatLongFromPreviousPick != null && mLatLongFromPreviousPick[0] != 0.0 &&
                mLatLongFromPreviousPick[1] != 0.0) {
            LatLng previousPickedLocation = new LatLng(mLatLongFromPreviousPick[0],
                    mLatLongFromPreviousPick[1]);

            mPreviouslySelectedMarker = mMap.addMarker(new MarkerOptions().position(previousPickedLocation).title("Marker Bitch"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(previousPickedLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            mMap.setLatLngBoundsForCameraTarget(new LatLngBounds(
                    new LatLng(53.94322574436461, 21.04769211262464), // southeast
                    new LatLng(56.07548457909273, 26.81183036416769))); // northwest
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                latLng = point;
                latitude = point.latitude;
                longitude = point.longitude;


                List<Address> addresses = new ArrayList<>();
                try {
                    addresses = mGeocoder.getFromLocation(point.latitude, point.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // If you click a see location, there will be no adress
                if (addresses.size() > 0){
                    android.location.Address address = addresses.get(0);
                    if (address != null) {
                        mAdressStringBuilder = new StringBuilder();
                        Log.d(TAG, "" + address);
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            mAdressStringBuilder.append(address.getAddressLine(i) + "\n");
                        }
                        Toast.makeText(MapsActivity.this, mAdressStringBuilder.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }




                //remove previously placed Marker
                if (mMarker != null) {
                    mMarker.remove();
                }

                // remove the old latLng marker
                if  (mPreviouslySelectedMarker != null) {
                    mPreviouslySelectedMarker.remove();

                }

                //place marker where user just clicked
                mMarker = mMap.addMarker(new MarkerOptions().position(point).title(mAdressStringBuilder.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            }
        });

        mLocationListener = mEventsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    StreetEvent streetEvent = postSnapshot.getValue(StreetEvent.class);
                    LatLng latLngFromDatabase = new LatLng(streetEvent.getLat(), streetEvent.getLng());
                    mMap.addMarker(new MarkerOptions().position(latLngFromDatabase).title(streetEvent.getBandName())
                            .snippet("Suck my dick"));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onLocationPicked (View v){

        // Return the selected latitudes and longitutes back to the CreateStreetEvent Activity
        double[] latLong = new double[]{latitude, longitude};
        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_KEY_LAT_LONG, latLong);
        returnIntent.putExtra(INTENT_KEY_RESULT_ADRESS, mAdressStringBuilder.toString());
        Log.d("latitude is", String.valueOf(latitude));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
