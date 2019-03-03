package edu.ucsd.cse110.googlefitapp.chatroom.models;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import edu.ucsd.cse110.googlefitapp.chatroom.interfaces.ModelCallBacks;


/**
 * Created by saksham on 26/6/17.
 */

public class MessageModel {
    private ArrayList<ChatPojo> mMessages;

    public void addMessages(DataSnapshot dataSnapshot, ModelCallBacks callBacks){
        if (mMessages==null){
            mMessages= new ArrayList<>();
        }
        ChatPojo chatPojo=new ChatPojo(dataSnapshot);
        mMessages.add(chatPojo);
        callBacks.onModelUpdated(mMessages);
    }

    public void addMessages(ChatPojo pojo, ModelCallBacks callBacks){
        if (mMessages==null){
            mMessages= new ArrayList<>();
        }
        mMessages.add(pojo);
        callBacks.onModelUpdated(mMessages);
    }
}
