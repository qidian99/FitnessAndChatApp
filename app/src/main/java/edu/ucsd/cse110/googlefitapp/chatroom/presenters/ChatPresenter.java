package edu.ucsd.cse110.googlefitapp.chatroom.presenters;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import edu.ucsd.cse110.googlefitapp.chatroom.interfaces.FirebaseCallBacks;
import edu.ucsd.cse110.googlefitapp.chatroom.interfaces.ModelCallBacks;
import edu.ucsd.cse110.googlefitapp.chatroom.models.ChatPojo;
import edu.ucsd.cse110.googlefitapp.chatroom.models.MessageModel;
import edu.ucsd.cse110.googlefitapp.chatroom.utils.FirebaseManager;
import edu.ucsd.cse110.googlefitapp.chatroom.views.IChatView;


/**
 * Created by saksham on 26/6/17.
 */
public class ChatPresenter implements FirebaseCallBacks, ModelCallBacks {

    private IChatView mIChatView;
    private MessageModel mModel;

    public ChatPresenter(IChatView iChatView){
        this.mIChatView=iChatView;
        this.mModel=new MessageModel();
    }

    public void sendMessageToFirebase(String roomName, String message) {
        if (!message.trim().equals("")){
            FirebaseManager.getInstance(roomName,this).sendMessageToFirebase(message);
        }
        mIChatView.clearEditText();
    }

    public void setListener(String roomName) {
        FirebaseManager.getInstance(roomName,this).addMessageListeners();
    }

    public void onDestory(String roomName) {
        FirebaseManager.getInstance(roomName,this).removeListeners();
        FirebaseManager.getInstance(roomName,this).destroy();
        mIChatView=null;
    }

    @Override
    public void onNewMessage(DataSnapshot dataSnapshot) {
        mModel.addMessages(dataSnapshot,this);
    }

    @Override
    public void onModelUpdated(ArrayList<ChatPojo> messages) {
        if (messages.size()>0) {
            mIChatView.updateList(messages);
        }
    }
}
