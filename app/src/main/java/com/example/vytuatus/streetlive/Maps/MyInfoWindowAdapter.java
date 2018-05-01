package com.example.vytuatus.streetlive.Maps;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vytuatus.streetlive.R;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.view.Gravity.CENTER_HORIZONTAL;

/**
 * Created by vytuatus on 1/4/17.
 */

public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = MyInfoWindowAdapter.class.getSimpleName();
    private final View mContentsView;
    private Activity mActivity;

    public MyInfoWindowAdapter(Activity activity){
        this.mActivity = activity;
        LayoutInflater inflater = mActivity.getLayoutInflater();
        mContentsView = inflater.inflate(R.layout.custom_info_window_layout, null);
    }

    @Override
    public View getInfoContents(Marker marker) {

        TextView tvTitle = mContentsView.findViewById(R.id.title);
        tvTitle.setText(marker.getTitle());
        TextView tvSnippet = mContentsView.findViewById(R.id.snippet);
        tvSnippet.setText(marker.getSnippet());
        final ImageView imageView = mContentsView.findViewById(R.id.info_window_imageView);

        StreetEvent streetEvent = (StreetEvent) marker.getTag();
        String bandImageUrl = streetEvent.getPhotoUrl();

        // If url starts with "gs" it means there is an image stored in Firebase for this band
        if (bandImageUrl.startsWith("gs://")){
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(bandImageUrl);
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        String downloadUrl = task.getResult().getScheme();
                        Glide.with(imageView.getContext())
                                .load(downloadUrl)
                                .into(imageView);
                    } else {
                        Log.w(TAG, "Getting download url was not successful.",
                                task.getException());
                    }
                }
            });

        } else {
            //If it doesn't start with "gs" - not Firebase image. Still fetch and load from net
            Glide.with(imageView.getContext())
                    .load(streetEvent.getPhotoUrl())
                    .into(imageView);
        }

        if (streetEvent.getPhotoUrl() == null) {
            imageView.setImageDrawable(ContextCompat.getDrawable(
                    mActivity,
                    R.drawable.ic_account_circle_black_36dp));
        } else {
            Glide.with(mActivity)
                    .load(streetEvent.getPhotoUrl())
                    .into(imageView);
        }

        return mContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO Auto-generated method stub
        return null;
    }

}
