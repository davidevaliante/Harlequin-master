package com.finder.harlequinapp.valiante.harlequin;

/**
 * Created by akain on 02/11/2016.
 */

public class Event {

    public String eventName,creatorName,description;
    public String eventDate;
    public String eventTime;
    public String creatorId;
    public String eventImagePath;
    public String creatorAvatarPath;
    public String eventKey;
    public Integer likes = 0;
    public Integer rLikes = 0;
    public Boolean eventIsFree ;
    public Integer eventPrice = 0;

    public Event(){

    }

    public Event(String eventName, String creatorName, String description,String eventDate,
                 String eventTime, String creatorId, String eventImagePath, String creatorAvatarPath, Integer likes, Integer rLikes,
                 Boolean eventIsFree, Integer eventPrice){

        this.eventName = eventName;
        this.creatorName = creatorName;
        this.description = description;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.creatorId = creatorId;
        this.eventImagePath = eventImagePath;
        this.creatorAvatarPath = creatorAvatarPath;
        this.likes = likes;
        this.rLikes = rLikes;
        this.eventIsFree = eventIsFree;
        this.eventPrice = eventPrice;

    }

    public Boolean getEventIsFree() {
        return eventIsFree;
    }

    public void setEventIsFree(Boolean eventIsFree) {
        this.eventIsFree = eventIsFree;
    }

    public Integer getEventPrice() {
        return eventPrice;
    }

    public void setEventPrice(Integer eventPrice) {
        this.eventPrice = eventPrice;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public Integer getrLikes() {
        return rLikes;
    }

    public void setrLikes(Integer rLikes) {
        this.rLikes = rLikes;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getCreatorAvatarPath() {return creatorAvatarPath;
    }

    public void setCreatorAvatarPath(String creatorAvatarPath) {this.creatorAvatarPath = creatorAvatarPath;
    }

    public String getEventImagePath() {
        return eventImagePath;
    }

    public void setEventImagePath(String eventImagePath) {
        this.eventImagePath = eventImagePath;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }
}
