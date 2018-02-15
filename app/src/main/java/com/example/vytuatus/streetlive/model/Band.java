package com.example.vytuatus.streetlive.model;

import java.util.HashMap;

/**
 * Created by vytuatus on 1/24/18.
 */

public class Band {

    private String id;
    private String bandName;
    private String genre;
    private String description;
    private String photoUrl;
    private HashMap<String, Object> timestampCreated;

    // default constructor
    public Band() {}

    public Band(String bandName, String genre, String description, String photoUrl,
                HashMap<String, Object> timestampCreated) {
        this.bandName = bandName;
        this.genre = genre;
        this.description = description;
        this.photoUrl = photoUrl;
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

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(HashMap<String, Object> timestampCreated) {
        this.timestampCreated = timestampCreated;
    }
}
