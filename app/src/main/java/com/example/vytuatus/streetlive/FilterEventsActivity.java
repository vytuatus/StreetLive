package com.example.vytuatus.streetlive;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import java.util.Arrays;



public class FilterEventsActivity extends AppCompatActivity {

    private static final String TAG = FilterEventsActivity.class.getSimpleName();

    private AutoCompleteTextView mSelectCityAutoCompleteTextView;
    private TextView mSelectedCityTextView;
    private Button mConfirmSelectedCityButton;
    private String mCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_events);

        // This part handles autocomplete textView
        final String[] citiesArray = getResources().getStringArray(R.array.cities);
        ArrayAdapter<String> autocompletetextAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, citiesArray);
        mSelectCityAutoCompleteTextView = findViewById(R.id.select_city_autoCompleteTextView);
        mSelectedCityTextView = findViewById(R.id.selected_city_TextView);
        mSelectCityAutoCompleteTextView.setAdapter(autocompletetextAdapter);
        mSelectCityAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    mSelectCityAutoCompleteTextView.showDropDown();
                }
            }
        });

        mSelectCityAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Save the selected/typed city in member variable
                mCityName = editable.toString();
            }
        });

        // This part handles button clicks
        mConfirmSelectedCityButton = findViewById(R.id.select_city_button);
        mConfirmSelectedCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make sure that user selected the city
                String cityName = Utility.getSelectedCityNameFromSharedPrefs(FilterEventsActivity.this);
                Log.d(TAG, cityName);
                // Check if typed city is in the right format (as it is in the drop down list)
                if (Arrays.asList(citiesArray).contains(mCityName)){
                    // Save city in shared preferences and display it on the screen
                    Utility.saveSelectedCityNameToSharedPrefs(FilterEventsActivity.this, mCityName);
                    mSelectedCityTextView.setText(mCityName);

                } else {
                    Toast.makeText(FilterEventsActivity.this, "Have you selected the city?",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
