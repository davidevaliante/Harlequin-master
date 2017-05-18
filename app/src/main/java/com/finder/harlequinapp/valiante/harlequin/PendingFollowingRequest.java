package com.finder.harlequinapp.valiante.harlequin;

/**
 * Created by akain on 18/05/2017.
 */

public class PendingFollowingRequest {
    public String senderToken, targetToken, senderId, targetId;
    public Long time;

    public PendingFollowingRequest(){
        //default empty constructor
    }

    public PendingFollowingRequest(String senderToken, String senderId,String targetToken,String targetId, Long time){
        this.senderToken = senderToken;
        this.senderId = senderId;
        this.targetToken = targetToken;
        this.targetId = targetId;
        this.time = time;

    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) {
        this.senderToken = senderToken;
    }

    public String getTargetToken() {
        return targetToken;
    }

    public void setTargetToken(String targetToken) {
        this.targetToken = targetToken;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}
