package com.softwareengineering2019.silverkisses;

import com.google.android.gms.maps.model.LatLng;

public class Bathroom {
    //private LatLng location;
    private String name;

    public void setRating(int rating) {
        this.rating = rating;
    }

    private int rating;
    private double lat;
    private  double lng;

    public Bathroom(LatLng location, String name, int rating) {
        this.lng=location.longitude;
        this.lat=location.latitude;
        this.name = name;
        this.rating=0;
    }
    public Bathroom(){}

    public double getLat() {
        return lat;
    }
    public double getLng(){
        return lng;
    }
    public String getName() {
        return name;
    }

    public int getRating(){
        return rating;
    }

}
