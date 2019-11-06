package com.softwareengineering2019.silverkisses;

import com.google.android.gms.maps.model.LatLng;

public class Bathroom {
    private LatLng location;
    private String name;
    private int rating;

    public Bathroom(LatLng location, String name) {
        this.location = location;
        this.name = name;
        this.rating=0;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public int getRating(){
        return rating;
    }
}
