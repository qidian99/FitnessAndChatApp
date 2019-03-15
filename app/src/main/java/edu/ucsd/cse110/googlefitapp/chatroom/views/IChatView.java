package edu.ucsd.cse110.googlefitapp.chatroom.views;

import java.util.ArrayList;

import edu.ucsd.cse110.googlefitapp.chatroom.models.ChatPojo;

/**
 * Created by saksham on 26/6/17.
 */

public interface IChatView {
    void updateList(ArrayList<ChatPojo> list);

    void clearEditText();
}
