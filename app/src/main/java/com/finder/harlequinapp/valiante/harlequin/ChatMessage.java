package com.finder.harlequinapp.valiante.harlequin;


public class ChatMessage {

    public String userId;
    public String message;
    public String userName;
    public String messageAvatar;
    public Integer hour;
    public Integer minute;

    public ChatMessage(){

    }

    public ChatMessage (String message, String userName, String messageAvatar, Integer hour, Integer minute, String userId){
        this.message = message;
        this.userName = userName;
        this.messageAvatar = messageAvatar;
        this.hour = hour;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public String getMessageAvatar() {
        return messageAvatar;
    }

    public void setMessageAvatar(String messageAvatar) {
        this.messageAvatar = messageAvatar;
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
