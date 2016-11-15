package com.finder.harlequinapp.valiante.harlequin;


public class ChatMessage {

    public String message;
    public String userName;
    public String messageAvatar;

    public ChatMessage(){

    }

    public ChatMessage (String message, String userName, String messageAvatar){
        this.message = message;
        this.userName = userName;
        this.messageAvatar = messageAvatar;
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
