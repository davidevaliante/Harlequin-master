package com.finder.harlequinapp.valiante.harlequin;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

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
    public Integer maleFav = 0;
    public Integer femaleFav = 0;
    public Integer totalAge = 0;
    public Integer numberOfSingles = 0;
    public Integer numberOfEngaged = 0;
    public long dateAndTimeInMillis = 0 ;

    public Event(){

    }

    public Event(String eventName, String creatorName, String description,String eventDate,
                 String eventTime, String creatorId, String eventImagePath, String creatorAvatarPath, Integer likes, Integer rLikes,
                 Boolean eventIsFree, Integer eventPrice, Integer maleFav,Integer femaleFav, Integer totalAge, Integer numberOfSingles,
                 Integer numberOfEngaged, long dateAndTimeInMillis){

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
        this.maleFav = maleFav;
        this.femaleFav = femaleFav;
        this.totalAge = totalAge;
        this.numberOfSingles = numberOfSingles;
        this.numberOfEngaged = numberOfEngaged;
        this.dateAndTimeInMillis = dateAndTimeInMillis;

    }

    public long getDateAndTimeInMillis() {
        return dateAndTimeInMillis;
    }

    public void setDateAndTimeInMillis(long dateAndTimeInMillis) {
        this.dateAndTimeInMillis = dateAndTimeInMillis;
    }

    public Integer getNumberOfEngaged() {
        return numberOfEngaged;
    }


    public void setNumberOfEngaged(Integer numberOfEngaged) {
        this.numberOfEngaged = numberOfEngaged;
    }

    public Integer getNumberOfSingles() {
        return numberOfSingles;
    }

    public void setNumberOfSingles(Integer numberOfSingles) {
        this.numberOfSingles = numberOfSingles;
    }

    public Integer getTotalAge() {
        return totalAge;
    }

    public void setTotalAge(Integer totalAge) {
        this.totalAge = totalAge;
    }

    public Integer getFemaleFav() {
        return femaleFav;
    }

    public void setFemaleFav(Integer femaleFav) {
        this.femaleFav = femaleFav;
    }

    public Integer getMaleFav() {
        return maleFav;
    }

    public void setMaleFav(Integer maleFav) {
        this.maleFav = maleFav;
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
