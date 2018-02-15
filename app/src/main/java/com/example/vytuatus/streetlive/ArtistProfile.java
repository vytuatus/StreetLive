package com.example.vytuatus.streetlive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vytuatus.streetlive.Utils.Utility;
import com.google.firebase.database.DatabaseReference;

public class ArtistProfile extends AppCompatActivity implements
        OneArtistProfile.OnFragmentInteractionListener {

    public static final String ARTIST_PROFILE_INTENT = "StartArtistProfileIntent";
    public static final int SHOW_NO_BAND_INFO_MESSAGE = 0;
    public static final int SHOW_SINGLE_ARTIST_FRAGMENT = 1;
    public static final int SHOW_MULTIPLE_ARTIST_FRAGMENT = 2;
    private TextView mNoBandInfoMessage;
    private TextView mNoBandCreateLink;
    private static final String TAG = ArtistProfile.class.getSimpleName();
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private String[] mBandNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_profile);

        // Get shared preferences and get the bandNames from there
        mBandNames = Utility.getNumberOfBands(this);

        // Based on the number of bands update the int that will be used to determine which fragment
        // to Load
        int whichFragmentToLoad;
        if (mBandNames.length > 1){
            whichFragmentToLoad = SHOW_MULTIPLE_ARTIST_FRAGMENT;
        } else if (mBandNames.length == 1){
            whichFragmentToLoad = SHOW_SINGLE_ARTIST_FRAGMENT;
        } else {
            whichFragmentToLoad = SHOW_NO_BAND_INFO_MESSAGE;
        }

        mNoBandInfoMessage = findViewById(R.id.no_bands_info_textView);
        mNoBandCreateLink = findViewById(R.id.create_new_band_link);
        ViewPager mViewPager = findViewById(R.id.pager);
        TabLayout mTabLayout = findViewById(R.id.tablayout);
        mNoBandCreateLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ArtistProfile.this, CreateBand.class);
                startActivity(intent);
            }
        });

        switch (whichFragmentToLoad){
            case SHOW_NO_BAND_INFO_MESSAGE:
                // Do nothing
                break;
            case SHOW_SINGLE_ARTIST_FRAGMENT:
                mNoBandInfoMessage.setVisibility(View.GONE);
                mNoBandCreateLink.setVisibility(View.GONE);
                OneArtistProfile oneArtistFragment = new OneArtistProfile();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, oneArtistFragment).commit();
                break;
            case SHOW_MULTIPLE_ARTIST_FRAGMENT:
                mNoBandInfoMessage.setVisibility(View.GONE);
                mNoBandCreateLink.setVisibility(View.GONE);
                //creating the sectionPagerAdapter which is responsible for controlling how pages are displayed and how many
                SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
                mViewPager.setOffscreenPageLimit(2);
                mViewPager.setAdapter(adapter);
                mTabLayout.setupWithViewPager(mViewPager);

//                MultipleArtistProfile multipleArtistFragment = new MultipleArtistProfile();
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        multipleArtistFragment).commit();
                break;

        }

    }

    public class SectionPagerAdapter extends FragmentStatePagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            //set fragment to different fragments depending on the position in ViewPager
            /**
             * Set fragment to different fragments depending on position in ViewPager
             */
            switch (position) {
                case 0:
                    OneArtistProfile fragment1 = OneArtistProfile.newInstance(mBandNames[0]);
                    return fragment1;
                case 1:
                    OneArtistProfile fragment2 = OneArtistProfile.newInstance(mBandNames[1]);
                    return fragment2;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return 2;
        }
        /**
         * Set string resources as titles for each fragment by it's position
         *
         * @param position
         */
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0:
                    return mBandNames[0];
                case 1:
                default:
                    return mBandNames[1];
            }
        }
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
