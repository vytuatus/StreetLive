package com.example.vytuatus.streetlive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.Band;
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
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.vytuatus.streetlive.MainActivity.BAND_LIST;
import static com.example.vytuatus.streetlive.MainActivity.FIREBASE_PROPERTY_TIMESTAMP;
import static com.example.vytuatus.streetlive.MainActivity.USERS_CHILD;

public class CreateBand extends AppCompatActivity {

    private static final String TAG = CreateBand.class.getSimpleName();
    // Intent key for selecting Image for a band
    private static final int REQUEST_IMAGE = 1;

    private Uri mSelectedImageUri;
    private ImageView mCreateBandImageView;
    private EditText mBandNameEditText;
    private String mBandName;
    private AutoCompleteTextView mGenreAutoCompleteTextView;
    private String mGenre;
    private EditText mBandDescriptionEditText;
    private String mBandDescription;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_band);

        // configures the autoComplete for genre and saves the selected genre in a member variable
        String[] genresArray = getResources().getStringArray(R.array.genres);

        ArrayAdapter<String> autocompletetextAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, genresArray);

        mGenreAutoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        mGenreAutoCompleteTextView.setAdapter(autocompletetextAdapter);
        mGenreAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    mGenreAutoCompleteTextView.showDropDown();
                }
            }
        });
        mGenreAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mGenre = editable.toString();
                Toast.makeText(CreateBand.this, mGenre, Toast.LENGTH_SHORT).show();
            }
        });

        // launches intent to pick the band image
        mCreateBandImageView = findViewById(R.id.create_band_imageView);
        mCreateBandImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Select image for image message on click.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        // write band name, save to member variable
        mBandNameEditText = findViewById(R.id.bandName_editText);
        mBandNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBandName = editable.toString();
            }
        });

        // write band description, save to member variable
        mBandDescriptionEditText = findViewById(R.id.bandDescription_editText);
        mBandDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBandDescription = editable.toString();
            }
        });

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // by clicking on the button the band is saved in firebase
        Button createBandButton = findViewById(R.id.create_band_button);
        createBandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create band object
                Band band = new Band(
                        mBandName,
                        mGenre,
                        mBandDescription,
                        mSelectedImageUri.toString(),
                        null);

                // Upload band object to Firebase
                mFirebaseDatabaseReference.child(USERS_CHILD)
                        .child(mFirebaseUser.getUid())
                        .child(BAND_LIST)
                        .child(mBandName)
                        .setValue(band, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    // when band object reaches Firebase, start to upload the image to firebase Storage
                                    String key = databaseReference.getKey();
                                    // first you create a storage ref. example: gs://friendlychat-f995d.appspot.com/lrF7AZOA81S7VcNfHJqZRJ1u8oy1/-L2GEGRiwXYSDds2YHZC/image%3A8648
                                    StorageReference storageReference =
                                            FirebaseStorage.getInstance() //gs://streetmusic-a3c02.appspot.com
                                                    .getReference(mFirebaseUser.getUid()) // "lrF7AZOA81S7VcNfHJqZRJ1u8oy1". user id under authentication
                                                    .child(mBandName) // "-L2GEGRiwXYSDds2YHZC". so that image has unique identifier + you know to which database element image belong
                                                    .child(mSelectedImageUri.getLastPathSegment()); // "image%3A8648". so that image has some name. name of image localy stored on the phone

                                    storageReference.putFile(mSelectedImageUri).addOnCompleteListener(CreateBand.this,
                                            new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        // reupdate the photoUrl to a url of Firebase storage
                                                        // also store the timestamp when band is created
                                                        HashMap<String, Object> timestampCreated = new HashMap<>();
                                                        timestampCreated.put(FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
                                                        Band band = new Band(
                                                                mBandName,
                                                                mGenre,
                                                                mBandDescription,
                                                                task.getResult().getMetadata().getDownloadUrl().toString(),
                                                                timestampCreated);

                                                        mFirebaseDatabaseReference.child(USERS_CHILD)
                                                                .child(mFirebaseUser.getUid())
                                                                .child(BAND_LIST)
                                                                .child(mBandName)
                                                                .setValue(band);

                                                        // Update shared pref variable which hold info on if one artist or multiple artist fragment to load
                                                        // also, it launches the ArtistProfile Activity
                                                        Utility.updateNumberOfBands(
                                                                mFirebaseUser,
                                                                mFirebaseDatabaseReference,
                                                                CreateBand.this);
                                                        finish();

                                                    } else {
                                                        Log.w(TAG, "Image upload task was not successful.",
                                                                task.getException());
                                                    }
                                                }
                                            });
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                }
                            }
                        });

            }
        });
    }

    // Select the band picture and display it on the screen & save in member variable the uri
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE){
            if (resultCode == RESULT_OK){
                if (data != null){
                    mSelectedImageUri = data.getData();
                    mCreateBandImageView.setImageURI(mSelectedImageUri);
                }
            }
        }
    }
}
