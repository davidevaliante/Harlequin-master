package com.finder.harlequinapp.valiante.harlequin;


public class User {

    public String userName;
    public String userEmail;
    public String userAge;

    public String userCity;
    public String userSurname;
    public String profileImage;

    public String userRelationship;
    public String userGender;
    public String facebookProfile;


    public User (){
        //costruttore di default che serve al database per compiere azioni tipo
        //DataSnapshot.getValue(User.class)
    }

    public User(String userName, String userEmail, String userAge, String userCity, String userSurname, String profileImage,
                String userRelationship, String userGender, String facebookProfile) {
        this.userName = userName;
        this.userAge = userAge;
        this.userEmail = userEmail;
        this.userCity = userCity;
        this.userSurname = userSurname;
        this.profileImage = profileImage;
        this.userGender = userGender;
        this.userRelationship = userRelationship;
        this.facebookProfile = facebookProfile;

    }

    public String getFacebookProfile() {
        return facebookProfile;
    }

    public void setFacebookProfile(String facebookProfile) {
        this.facebookProfile = facebookProfile;
    }

    public String getUserRelationship() {
        return userRelationship;
    }

    public void setUserRelationship(String userRelationship) {
        this.userRelationship = userRelationship;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserSurname() {
        return userSurname;
    }

    public void setUserSurname(String userSurname) {
        this.userSurname = userSurname;
    }

    public String getProfileImage() {return profileImage;}

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
