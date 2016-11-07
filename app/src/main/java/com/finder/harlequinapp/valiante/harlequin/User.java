package com.finder.harlequinapp.valiante.harlequin;

/**
 * Created by akain on 29/10/2016.
 */

public class User {

    public String userName;
    public String userEmail;
    public int userAge;
    public String userCity;
    public String userSurname;

    public User (){
        //costruttore di default che serve al database per compiere azioni tipo
        //DataSnapshot.getValue(User.class)
    }

    public User(String userName, String userEmail, int userAge, String userCity, String userSurname) {
        this.userName = userName;
        this.userAge = userAge;
        this.userEmail = userEmail;
        this.userCity = userCity;
        this.userSurname = userSurname;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
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
}
