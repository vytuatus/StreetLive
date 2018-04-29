package com.example.vytuatus.streetlive;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.example.vytuatus.streetlive.Utils.Utility;
import com.example.vytuatus.streetlive.model.StreetEvent;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by vytuatus on 1/5/18.
 */

public class CustomFirebaseAdapter extends FirebaseRecyclerAdapter<StreetEvent,
        CustomFirebaseAdapter.StreetViewHolder> {

    private Context mContext;
    private final String TAG = CustomFirebaseAdapter.class.getSimpleName();
    private final CustomFirebaseAdapterOnClickHandler mEventLocationClickHandler;
    public static interface CustomFirebaseAdapterOnClickHandler{
        void onEventLocationClick(int position, String eventFirebaseId);
    }


    public CustomFirebaseAdapter(FirebaseRecyclerOptions<StreetEvent> options, Context context,
                                 CustomFirebaseAdapterOnClickHandler clickHandler) {
        super(options);
        mContext = context;
        mEventLocationClickHandler = clickHandler;
    }

    @Override
    public StreetViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_message, viewGroup, false);
        return new StreetViewHolder(view);

    }

    @Override
    protected void onBindViewHolder(final StreetViewHolder holder, int position, StreetEvent streetEvent) {

        holder.bandNameTextView.setText(streetEvent.getBandName());
        holder.genreTextView.setText(streetEvent.getGenre());

        // Set the event time
        long eventStartTimeLocally = Utility.gmttoLocalDate(streetEvent.getStartTime());
        long eventEndTimeLocally = Utility.gmttoLocalDate(streetEvent.getEndTime());
        holder.eventStartTimeTextView.setText(Utility.getFriendlyTime(eventStartTimeLocally));
        holder.eventEndTimeTextView.setText(Utility.getFriendlyTime(eventEndTimeLocally));
        holder.eventDateTextView.setText(Utility.getFriendlyDate(eventEndTimeLocally));

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

    public class StreetViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        TextView bandNameTextView;
        TextView genreTextView;
        TextView eventStartTimeTextView;
        TextView eventEndTimeTextView;
        TextView eventDateTextView;
        ImageView bandImageView;
        ImageView mapImageView;

        public StreetViewHolder(View v) {
            super(v);
            bandNameTextView = itemView.findViewById(R.id.band_name_textView);
            genreTextView = itemView.findViewById(R.id.genre_textView);
            eventStartTimeTextView = itemView.findViewById(R.id.event_start_time_textView);
            eventEndTimeTextView = itemView.findViewById(R.id.event_end_time_textView);
            eventDateTextView = itemView.findViewById(R.id.event_date_textView);
            bandImageView = itemView.findViewById(R.id.band_imageView);
            mapImageView = itemView.findViewById(R.id.map_imageView);
            itemView.setOnClickListener(this);
            mapImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Map Clicked", Toast.LENGTH_SHORT).show();
                        mEventLocationClickHandler.onEventLocationClick(getAdapterPosition(),
                                getItem(getAdapterPosition()).getId());
        }
    }



}
