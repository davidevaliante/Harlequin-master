package com.finder.harlequinapp.valiante.harlequin;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;


public class MapInfo {


    public String placeName;
    public String placeLocation;
    public String placePhone;
    public String placeId;
    public String eventId;
    public Double latitude;
    public Double longitude;

    public MapInfo(){

    }

    public MapInfo (String placeName,String placeLocation,String placePhone
                    ,String placeId,String eventId,Double latitude,Double longitude){


        this.placeName = placeName;
        this.placeLocation = placeLocation;
        this.placePhone = placePhone;
        this.placeId = placeId;
        this.eventId = eventId;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getPlaceLocation() {
        return placeLocation;
    }

    public void setPlaceLocation(String placeLocation) {
        this.placeLocation = placeLocation;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }





    public String getPlacePhone() {
        return placePhone;
    }

    public void setPlacePhone(String placePhone) {
        this.placePhone = placePhone;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
