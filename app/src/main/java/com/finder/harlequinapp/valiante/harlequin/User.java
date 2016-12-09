package com.finder.harlequinapp.valiante.harlequin;


import java.util.Calendar;

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
    public String anonymousName;


    public User (){
        //costruttore di default che serve al database per compiere azioni tipo
        //DataSnapshot.getValue(User.class)
    }

    public User(String userName, String userEmail, String userAge, String userCity, String userSurname, String profileImage,
                String userRelationship, String userGender, String facebookProfile,String anonymousName) {
        this.userName = userName;
        this.userAge = userAge;
        this.userEmail = userEmail;
        this.userCity = userCity;
        this.userSurname = userSurname;
        this.profileImage = profileImage;
        this.userGender = userGender;
        this.userRelationship = userRelationship;
        this.facebookProfile = facebookProfile;
        this.anonymousName = anonymousName;

    }

    public String getAnonymousName() {
        return anonymousName;
    }

    public void setAnonymousName(String anonymousName) {
        this.anonymousName = anonymousName;
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

    public Integer getAge (String birthdate){
        //estrae i numeri dalla stringa
        String parts [] = birthdate.split("/");
        //li casta in interi
        Integer day = Integer.parseInt(parts[0]);
        Integer month = Integer.parseInt(parts[1]);
        Integer year = Integer.parseInt(parts[2]);

        //oggetto per l'anno di nascita
        Calendar dob = Calendar.getInstance();
        //oggetto per l'anno corrente
        Calendar today = Calendar.getInstance();

        //setta anno di nascita in formato data
        dob.set(year,month,day);
        //calcola l'anno
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        //controlla che il giorno attuale sia minore del giorno del compleanno
        //nel caso in cui fosse vero allora il compleanno non è ancora passato e il conteggio degli anni viene diminuito
        if (today.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        //restituisce l'età sotto forma numerica utile per calcolare l'età media dei partecipanti ad un evento
        return age;

    }


}
