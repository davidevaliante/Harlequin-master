package com.finder.harlequinapp.valiante.harlequin;


public class ChatMessage {

    public String userId;
    public String message;
    public String userName;
    public String messageAvatar;
    public String messageTime;


    public ChatMessage(){

    }

    public ChatMessage (String message, String userName, String messageAvatar,  String userId, String messageTime){
        this.message = message;
        this.userName = userName;
        this.messageAvatar = messageAvatar;
        this.userId = userId;
        this.messageTime = messageTime;

    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
