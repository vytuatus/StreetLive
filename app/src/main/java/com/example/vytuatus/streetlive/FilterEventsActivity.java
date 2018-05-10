package com.example.vytuatus.streetlive;

import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
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
    private Spinner mSelectDaySpinner;

    private String mCityName;
    private String mSelectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_events);
        setupActionBar();

        mSelectedDay = Utility.getSelectedDayFilterFromSharedPrefs(this);
        mCityName = Utility.getSelectedCityNameFromSharedPrefs(this);

        initiateCitiesFilter();
        initiateDaysfilter();


    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    // Initiates the days filter. find views by Id and implements their functionality
    private void initiateDaysfilter() {
        // This part handles autocomplete textView
        final String[] daysArray = getResources().getStringArray(R.array.days);
        ArrayAdapter<String> autocompletetextAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, daysArray);
        mSelectDaySpinner = findViewById(R.id.select_day_autoCompleteTextView);
        mSelectDaySpinner.setAdapter(autocompletetextAdapter);
        // Show previously selected day
        if (!mSelectedDay.equals(getString(R.string.daySelected_pref_default))){
            int spinnerPosition = autocompletetextAdapter.getPosition(mSelectedDay);
            mSelectDaySpinner.setSelection(spinnerPosition);
        }

        mSelectDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(FilterEventsActivity.this, adapterView.getSelectedItem().toString(),
                        Toast.LENGTH_SHORT).show();
                // Save the selected day in the sharedPrefs
                Utility.saveSelectedDayFilterToSharedPrefs(FilterEventsActivity.this,
                        adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    // Initiates the cities filter. find views by Id and implements their functionality
    private void initiateCitiesFilter() {
        // This part handles autocomplete textView
        final String[] citiesArray = getResources().getStringArray(R.array.cities);
        ArrayAdapter<String> autocompletetextAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, citiesArray);
        mSelectCityAutoCompleteTextView = findViewById(R.id.select_city_autoCompleteTextView);
        mSelectedCityTextView = findViewById(R.id.selected_city_TextView);
        mSelectCityAutoCompleteTextView.setAdapter(autocompletetextAdapter);
        // show previously selected city
        if (!mCityName.equals(getString(R.string.cityName_pref_default))){

            mSelectedCityTextView.setText(mCityName);
        }

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
