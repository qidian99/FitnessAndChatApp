package edu.ucsd.cse110.googlefitapp.chatroom.models;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

/**
 * Created by saksham on 26/6/17.
 */

public class ChatPojo {
    private String msgKey;
    private long timeStamp;
    private String message;
    private String senderId;


    public ChatPojo(QueryDocumentSnapshot queryDocumentSnapshot){
        this.msgKey= queryDocumentSnapshot.getId();
        this.message=queryDocumentSnapshot.get("text").toString();
        this.senderId=queryDocumentSnapshot.get("senderId").toString();
        this.timeStamp=Long.parseLong(queryDocumentSnapshot.get("time").toString());
        Log.e("POJO", "New chat history added: " + queryDocumentSnapshot.get("text"));
    }

    public ChatPojo(DataSnapshot dataSnapshot){
        HashMap<String, Object> object = (HashMap<String, Object>) dataSnapshot.getValue();
        this.msgKey=dataSnapshot.getKey();
        this.message=object.get("text").toString();
        this.senderId=object.get("senderId").toString();
        this.timeStamp=Long.parseLong(object.get("time").toString());
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

}
