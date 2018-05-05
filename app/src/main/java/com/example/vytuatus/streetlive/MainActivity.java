package com.example.vytuatus.streetlive;

import android.*;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.Band;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener
{

    private static final String TAG = "MainActivity";
    public static final String USERS_CHILD = "users";
    public static final String BAND_LIST = "bandList";
    public static final String EVENTS_CHILD = "events";
    public static final String EVENTS_IN_BAND = "eventsInBand";
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mUserType;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    private Location mUsersLastKnownLocation;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;
    private TextView mEmptyView;

    private String mCityName;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mFirebaseDatabaseReference;
    private SnapshotParser<StreetEvent> mParser;
    private CustomFirebaseAdapter mFirebaseAdapter;
    private CustomFirebaseAdapter.CustomFirebaseAdapterOnClickHandler mCustomFirebaseClickHandler =
            new CustomFirebaseAdapter.CustomFirebaseAdapterOnClickHandler() {
        @Override
        public void onEventLocationClick(final int position, final String eventId) {
            Log.d(TAG, "Firebase Event ID: " + eventId);
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.INTENT_KEY_EVENT_ID, eventId);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserType = Utility.getUserSignInTypeFromSharedPrefs(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (mFirebaseUser == null) {
            if (mUserType.equals(getString(R.string.signInType_pref_active_user))){
                // If user is an active user, then we need to prompt him to log in screen
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return;
            } else if (mUserType.equals(getString(R.string.signInType_pref_guest_user))) {
                // If user is just a guest, inflate guest navigation menu drawer
                navigationView.inflateMenu(R.menu.activity_main_drawer_guest_user);

            }

        } else {
            // User sign in, meaning user is an active user
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }

            // Updates shared pref which holds info on which fragment should be loaded in ArtistProfile
            Utility.updateNumberOfBands(mFirebaseUser, mFirebaseDatabaseReference, MainActivity.this);

            // If user is active user, inflate active user navigation menu drawer
            navigationView.inflateMenu(R.menu.activity_main_drawer);
        }

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = findViewById(R.id.progressBar);
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
        mEmptyView = findViewById(R.id.empty_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        // New Child Entries
        mParser = new SnapshotParser<StreetEvent>() {
            @Override
            public StreetEvent parseSnapshot(DataSnapshot dataSnapshot) {
                StreetEvent streetEvent = dataSnapshot.getValue(StreetEvent.class);
                if (streetEvent != null) {
                    streetEvent.setId(dataSnapshot.getKey());
                }
                return streetEvent;
            }
        };

        // Get the stored previously selected city Name by user
        mCityName = Utility.getSelectedCityNameFromSharedPrefs(this);
        Query messagesRef;
        if (mCityName.length() > 0 && mCityName != null){
            messagesRef = mFirebaseDatabaseReference.child(EVENTS_CHILD).child(mCityName);
            Log.d(TAG, "City is: " + mCityName);
        } else {
            messagesRef = mFirebaseDatabaseReference.child(EVENTS_CHILD).child("Vilnius");
        }

        FirebaseRecyclerOptions<StreetEvent> options =
                new FirebaseRecyclerOptions.Builder<StreetEvent>()
                        .setQuery(messagesRef, mParser)
                        .build();

        // Instantiate a new Firebase Adapter and also handle the click events on different
        // Recycler view objects
        mFirebaseAdapter = new CustomFirebaseAdapter(options, MainActivity.this,
                mCustomFirebaseClickHandler);

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {

                mEmptyView.setVisibility(View.INVISIBLE);
                mMessageRecyclerView.smoothScrollToPosition(positionStart);
//                super.onItemRangeInserted(positionStart, itemCount);
//                int streetEventCount = mFirebaseAdapter.getItemCount();
//
//                int lastVisiblePosition =
//                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
//                Log.d(TAG, "positionStart" + positionStart);
//                Log.d(TAG, "friendlyMessageCount" + streetEventCount);
//                Log.d(TAG, "lastVisiblePosition" + lastVisiblePosition);
//                // If the recycler view is initially being loaded or the
//                // user is at the bottom of the list, scroll to the bottom
//                // of the list to show the newly added message.
//                if (lastVisiblePosition == -1 ||
//                        (positionStart >= (streetEventCount - 1) &&
//                                lastVisiblePosition == (positionStart - 1))) {
//                    mMessageRecyclerView.scrollToPosition(positionStart);
//                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        Toast.makeText(this, "onCreatedLoadedAgain", Toast.LENGTH_SHORT).show();

        //add the listener for the single value event that will function
        //like a completion listener for initial data load of the FirebaseRecyclerAdapter
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //onDataChange called so remove progress bar
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                //make a call to dataSnapshot.hasChildren() and based
                //on returned value show/hide empty view
                if (!dataSnapshot.hasChildren()) {
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
        mFirebaseAdapter.stopListening();

    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(this);
        Log.d(TAG, "OnResumeCalled");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // if user is active user, load active user's navigation view items
        if (mUserType.equals(getString(R.string.signInType_pref_active_user))){
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_artist) {
                // Go to the Artist page
                Intent intent = new Intent(MainActivity.this, ArtistProfile.class);
                startActivity(intent);

            } else if (id == R.id.nav_create_band) {

                // Create new band
                Intent intent = new Intent(MainActivity.this, CreateBand.class);
                startActivity(intent);

            } else if (id == R.id.nav_filter) {
                // Go to Filter Page
                // Create new band
                Intent intent = new Intent(MainActivity.this, FilterEventsActivity.class);
                startActivity(intent);

            } else if (id == R.id.nav_sign_out) {

                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            } else if (id == R.id.nav_share) {

            } else if (id == R.id.nav_send) {

            }

        } else if (mUserType.equals(getString(R.string.signInType_pref_guest_user))){
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_create_account) {
                // Go to sign in page
                startActivity(new Intent(this, SignInActivity.class));
                finish();

            } else if (id == R.id.nav_filter) {
                // Go to Filter Page
                // Create new band
                Intent intent = new Intent(MainActivity.this, FilterEventsActivity.class);
                startActivity(intent);

            } else if (id == R.id.nav_sign_out) {

                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            } else if (id == R.id.nav_share) {

            } else if (id == R.id.nav_send) {

            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    //Used to re-query events from database based on Filters
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        Toast.makeText(this, "Pref has changed. key is: " + key, Toast.LENGTH_SHORT).show();

        // Get the stored previously selected city Name by user
        if (key.equals(getString(R.string.cityName_pref_key))) {
            mCityName = sharedPreferences.getString(key, "Vilnius");
        }
        long timeInterval[] = new long[2];
        if (key.equals(getString(R.string.daySelected_pref_key))) {
            timeInterval = Utility.getSelectedDayFilterInterval(sharedPreferences.getString(key, "Today"));
        }
        // Construct a query that will filter out city and time1
        Query messagesRef = mFirebaseDatabaseReference.child(EVENTS_CHILD).child(mCityName)
                .orderByChild("startTime").startAt(timeInterval[0]).endAt(timeInterval[1]);
        Log.d(TAG, "startTime: " + timeInterval[0]);
        Log.d(TAG, "endTime: " + timeInterval[1]);

        FirebaseRecyclerOptions<StreetEvent> options =
                new FirebaseRecyclerOptions.Builder<StreetEvent>()
                        .setQuery(messagesRef, mParser)
                        .build();

        mMessageRecyclerView.setAdapter(null);
        // Instantiate a new Firebase Adapter and also handle the click events on different
        // Recycler view objects
        mFirebaseAdapter = new CustomFirebaseAdapter(options, MainActivity.this,
                mCustomFirebaseClickHandler);

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        //add the listener for the single value event that will function
        //like a completion listener for initial data load of the FirebaseRecyclerAdapter
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //onDataChange called so remove progress bar
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                //make a call to dataSnapshot.hasChildren() and based
                //on returned value show/hide empty view
                if (!dataSnapshot.hasChildren()) {
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
