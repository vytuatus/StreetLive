package com.example.vytuatus.streetlive.model;

import java.util.HashMap;

/**
 * Created by vytuatus on 1/13/18.
 */

public class StreetEvent {

    private String id;
    private String bandName;
    private String genre;
    private String description;
    private String photoUrl;
    private double lat;
    private double lng;
    private HashMap<String, Object> timestampCreated;

    public StreetEvent() {
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public StreetEvent(String bandName, String genre, String description,
                       String photoUrl, double lat, double lng, HashMap<String, Object> timestampCreated) {

        this.bandName = bandName;
        this.genre = genre;
        this.description = description;
        this.photoUrl = photoUrl;
        this.lat = lat;
        this.lng = lng;
        this.timestampCreated = timestampCreated;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(HashMap<String, Object> timestampCreated) {
        this.timestampCreated = timestampCreated;
    }
}
