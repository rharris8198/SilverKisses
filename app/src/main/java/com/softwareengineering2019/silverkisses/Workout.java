package com.softwareengineering2019.silverkisses;

public class Workout {
    private Double distance;
    private String duration;
    private Double pace;
    private String date;

    public Workout(Double distance, String duration, String date) {
        this.distance = distance;
        this.duration = duration;
        this.date = date;

    }
    public Workout(){

    }

    public Double getDistance() {


        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public Double getPace() {
        return pace;
    }

    public String getDate() {
        return date;
    }
}
