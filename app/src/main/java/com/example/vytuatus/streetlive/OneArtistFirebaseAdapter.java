package com.example.vytuatus.streetlive;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by vytuatus on 2/11/18.
 */

public class OneArtistFirebaseAdapter extends FirebaseRecyclerAdapter<StreetEvent,
        OneArtistFirebaseAdapter.BandEventViewHolder> {

    private Context mContext;
    private final String TAG = OneArtistFirebaseAdapter.class.getSimpleName();

    public OneArtistFirebaseAdapter(FirebaseRecyclerOptions<StreetEvent> options,
                                    Context context) {
        super(options);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(final BandEventViewHolder holder, int position, StreetEvent streetEvent) {
        holder.bandNameTextView.setText(streetEvent.getBandName());
        holder.genreTextView.setText(streetEvent.getGenre());

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
                        Glide.with(holder.bandImageView.getContext())
                                .load(downloadUrl)
                                .into(holder.bandImageView);
                    } else {
                        Log.w(TAG, "Getting download url was not successful.",
                                task.getException());
                    }
                }
            });

        } else {
            //If it doesn't start with "gs" - not Firebase image. Still fetch and load from net
            Glide.with(holder.bandImageView.getContext())
                    .load(streetEvent.getPhotoUrl())
                    .into(holder.bandImageView);
        }

        if (streetEvent.getPhotoUrl() == null) {
            holder.bandImageView.setImageDrawable(ContextCompat.getDrawable(
                    mContext,
                    R.drawable.ic_account_circle_black_36dp));
        } else {
            Glide.with(mContext)
                    .load(streetEvent.getPhotoUrl())
                    .into(holder.bandImageView);
        }
    }

    @Override
    public BandEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.band_event_item_layout, parent, false);
        Log.d(TAG, "Recycler is called!");
        return new OneArtistFirebaseAdapter.BandEventViewHolder(view);
    }

    public static class BandEventViewHolder extends RecyclerView.ViewHolder {
        TextView bandNameTextView;
        TextView genreTextView;
        ImageView bandImageView;
        ImageView mapImageView;

        public BandEventViewHolder(View v) {
            super(v);
            bandNameTextView = itemView.findViewById(R.id.band_name_textView);
            genreTextView = itemView.findViewById(R.id.genre_textView);
            bandImageView = itemView.findViewById(R.id.band_imageView);
            mapImageView = itemView.findViewById(R.id.map_imageView);
        }
    }
}
