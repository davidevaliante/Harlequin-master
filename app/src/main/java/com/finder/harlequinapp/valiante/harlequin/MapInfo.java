package com.finder.harlequinapp.valiante.harlequin;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;


public class MapInfo {


    public String placeName;
    public String placeLocation;
    public String placePhone;
    public LatLng placeLatLng;
    public String placeId;
    public String eventId;

    public MapInfo(){

    }

    public MapInfo (String placeName,String placeLocation,String placePhone
                    , LatLng placeLatLng,String placeId,String eventId){


        this.placeName = placeName;
        this.placeLocation = placeLocation;
        this.placePhone = placePhone;
        this.placeLatLng = placeLatLng;
        this.placeId = placeId;
        this.eventId = eventId;

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



    public LatLng getPlaceLatLng() {
        return placeLatLng;
    }

    public void setPlaceLatLng(LatLng placeLatLng) {
        this.placeLatLng = placeLatLng;
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
