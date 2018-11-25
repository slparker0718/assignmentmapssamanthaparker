package edu.psu.slparker.assignment_maps_samanthaparker;

import java.io.Serializable;

public class Maps implements Serializable{

    private String location;
    private Double latitude;
    private Double longitude;

    public Maps()
    {
    }

    public Maps(String location, Double latitude, Double longitude) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String toString() {

        return "location: "+ location + ", latitude: "+ latitude + ", longitude: " + longitude;

    }
}
