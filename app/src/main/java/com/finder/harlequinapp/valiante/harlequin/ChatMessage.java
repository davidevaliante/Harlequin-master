package com.finder.harlequinapp.valiante.harlequin;


public class ChatMessage {

    private String message;
    private String userName;

    public ChatMessage(){

    }

    public ChatMessage (String message, String userName){
        this.message = message;
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
