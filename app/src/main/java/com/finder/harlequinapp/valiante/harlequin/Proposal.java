package com.finder.harlequinapp.valiante.harlequin;

/**
 * Created by akain on 13/06/2017.
 */

public class Proposal {

    public String title,places,description,argument,creator;
    public String city;
    public Integer likes;
    public Long creationTime;
    public Boolean isAnonymous;
    public String submitterId;




    public Proposal(){

    }

    public Proposal(String title, String places, String description, String argument, String creator, Integer likes, Long creationTime,String city,Boolean isAnonymous,String submitterId) {
        this.title = title;
        this.places = places;
        this.description = description;
        this.argument = argument;
        this.creator = creator;
        this.likes = likes;
        this.creationTime = creationTime;
        this.city = city;
        this.isAnonymous = isAnonymous;
        this.submitterId = submitterId;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public Boolean getAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        isAnonymous = anonymous;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlaces() {
        return places;
    }

    public void setPlaces(String places) {
        this.places = places;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }
}
