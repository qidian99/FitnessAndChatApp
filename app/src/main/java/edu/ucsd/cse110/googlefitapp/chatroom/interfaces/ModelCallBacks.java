package edu.ucsd.cse110.googlefitapp.chatroom.interfaces;

import java.util.ArrayList;

import edu.ucsd.cse110.googlefitapp.chatroom.models.ChatPojo;


/**
 * Created by saksham on 26/6/17.
 */

public interface ModelCallBacks {
    void onModelUpdated(ArrayList<ChatPojo> messages);
}
