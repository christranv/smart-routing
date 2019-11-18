package com.example.smrt.model;

public class MapPoint {
    private String label;
    private double lat,lon;

    public MapPoint(String label, double lat, double lon) {
        this.label = label;
        this.lat = lat;
        this.lon = lon;
    }

    public String getLabel() {
        return label;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
