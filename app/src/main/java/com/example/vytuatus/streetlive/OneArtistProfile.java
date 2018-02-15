package com.example.vytuatus.streetlive;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.Band;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import static com.example.vytuatus.streetlive.MainActivity.BAND_LIST;
import static com.example.vytuatus.streetlive.MainActivity.USERS_CHILD;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OneArtistProfile.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OneArtistProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OneArtistProfile extends Fragment {

    private static final String TAG = OneArtistProfile.class.getSimpleName();

    public static final String EVENTS_IN_BAND = "eventsInBand";
    private String mBandName;
    public static final String LAUNCH_CREATE_EVENT_INTENT_KEY = "launchCreateEventIntentKey";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    private RecyclerView mEventsRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private OneArtistFirebaseAdapter mOneArtistFirebaseAdapter;

    private FloatingActionButton mCreateEventFabButton;
    private FloatingActionButton mCreateNewBandFabButton;
    private TextView mBandNameTextView;
    private TextView mGenreTextView;
    private TextView mBandDescriptionTextView;
    private ImageView mImageView;

    public OneArtistProfile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OneArtistProfile.
     */
    // TODO: Rename and change types and number of parameters
    public static OneArtistProfile newInstance(String bandName) {
        OneArtistProfile fragment = new OneArtistProfile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, bandName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBandName = getArguments().getString(ARG_PARAM1);
        }

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_one_artist_profile, container, false);
        mBandNameTextView = rootView.findViewById(R.id.bandName_textView);
        mGenreTextView = rootView.findViewById(R.id.bandGenre_textView);
        mBandDescriptionTextView = rootView.findViewById(R.id.bandDescription_textView);
        mImageView = rootView.findViewById(R.id.band_imageView);
        mCreateEventFabButton = rootView.findViewById(R.id.create_event_fab_button);
        mCreateNewBandFabButton = rootView.findViewById(R.id.create_band_fab_button);

        // Load recycler view for band events
        loadEventsRecyclerView(rootView);

        // fetch band information from Firebase Database
        fetchBandInfoFromFirebase();
        // This part handles fetching the band info from Firebase Database

        // This part handles clicking on fab Button and creating an event for the band
        mCreateEventFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch CreateStreetEvent Activity
                Intent intent = new Intent(getActivity(), CreateStreetEvent.class);
                intent.putExtra(LAUNCH_CREATE_EVENT_INTENT_KEY, mBandName);
                startActivity(intent);

            }
        });

        mCreateNewBandFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateBand.class);
                startActivity(intent);
            }
        });

        return rootView;


    }

    // Fetch band info from Firebase and update the UI
    private void fetchBandInfoFromFirebase() {
        DatabaseReference bandDatabaseReference = mFirebaseDatabaseReference
                .child(USERS_CHILD)
                .child(mFirebaseUser.getUid())
                .child(BAND_LIST)
                .child(mBandName);
        bandDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Band band = dataSnapshot.getValue(Band.class);
//                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
//                    band = postSnapshot.getValue(Band.class);
//                }

//                mBandName = band.getBandName();
                mBandNameTextView.setText(mBandName);
                Toast.makeText(getActivity(), band.getGenre(), Toast.LENGTH_SHORT).show();
                mGenreTextView.setText(band.getGenre  ());
                mBandDescriptionTextView.setText(band.getDescription());

                final String imageUrl = band.getPhotoUrl();

                // If url starts with "gs" it means there is an image stored in Firebase for this user
                if (imageUrl != null){
                    // If Firebase holds the image, then
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl);
                    storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                //String downloadUrl = task.getResult().getScheme();
                                Log.d(TAG, imageUrl);
                                Glide.with(getActivity())
                                        .load(imageUrl)
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
                    Glide.with(getActivity())
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

    // Load Event Recycler View information after band information is loaded
    private void loadEventsRecyclerView(View rootView) {
        // This Part handles fetching the Events from Firebase that are under that specific band
        Log.d(TAG, "RecyclerLoaded");
        SnapshotParser<StreetEvent> parser = new SnapshotParser<StreetEvent>() {
            @Override
            public StreetEvent parseSnapshot(DataSnapshot dataSnapshot) {
                StreetEvent streetEvent = dataSnapshot.getValue(StreetEvent.class);
                if (streetEvent != null){
                    streetEvent.setId(dataSnapshot.getKey());
                }
                return streetEvent;
            }
        };

        mEventsRecyclerView = rootView.findViewById(R.id.oneArtistEventsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mEventsRecyclerView.setLayoutManager(mLinearLayoutManager);

        DatabaseReference userEventReference = mFirebaseDatabaseReference.
                child(USERS_CHILD).
                child(mFirebaseUser.getUid()).
                child(BAND_LIST).
                child(mBandName).
                child(EVENTS_IN_BAND);
        Log.d(TAG, userEventReference.toString());

        FirebaseRecyclerOptions<StreetEvent> options =
                new FirebaseRecyclerOptions.Builder<StreetEvent>()
                        .setQuery(userEventReference, parser)
                        .build();

        mOneArtistFirebaseAdapter = new OneArtistFirebaseAdapter(options, getContext());

        mOneArtistFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {

                //mEmptyView.setVisibility(View.INVISIBLE);
                mEventsRecyclerView.smoothScrollToPosition(positionStart);
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

        mEventsRecyclerView.setAdapter(mOneArtistFirebaseAdapter);

        //add the listener for the single value event that will function
        //like a completion listener for initial data load of the FirebaseRecyclerAdapter
        userEventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //onDataChange called so remove progress bar
                mCreateEventFabButton.setVisibility(View.VISIBLE);
                mCreateNewBandFabButton.setVisibility(View.VISIBLE);
                Log.d(TAG, "RecyclerView loaded");
                //make a call to dataSnapshot.hasChildren() and based
                //on returned value show/hide empty view
                if(!dataSnapshot.hasChildren()){
                    //mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "RecyclerView has Children");
                } else {
                    //mEmptyView.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //mOneArtistFirebaseAdapter.startListening();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mOneArtistFirebaseAdapter.startListening();

    }

    @Override
    public void onStop() {
        mOneArtistFirebaseAdapter.stopListening();
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
