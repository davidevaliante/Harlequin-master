package com.finder.harlequinapp.valiante.harlequin;

/**
 * Created by akain on 02/11/2016.
 */

public class Event {

    public String eventName,creatorName,description;
    public String eventDate;
    public String eventTime;
    public String creatorId;

    public Event(){

    }

    public Event(String eventName, String creatorName, String description,String eventDate, String eventTime, String creatorId){

        this.eventName = eventName;
        this.creatorName = creatorName;
        this.description = description;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.creatorId = creatorId;

    }



}
