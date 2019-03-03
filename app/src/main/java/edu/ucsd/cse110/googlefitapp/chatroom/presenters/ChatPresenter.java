package edu.ucsd.cse110.googlefitapp.chatroom.presenters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ucsd.cse110.googlefitapp.R;
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

    private static final String TAG = "CHAT_PRESENTER";
    private IChatView mIChatView;
    private MessageModel mModel;
    public CollectionReference chat;

    public ChatPresenter(IChatView iChatView){
        this.mIChatView=iChatView;
        this.mModel=new MessageModel();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setChat(CollectionReference chat){
        this.chat = chat;
        initMessageUpdateListener();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initMessageUpdateListener() {
        chat.orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((newChatSnapShot, error) -> {
                    if (error != null) {
                        Log.e(TAG, error.getLocalizedMessage());
                        return;
                    }

                    if (newChatSnapShot != null && !newChatSnapShot.isEmpty()) {
                        List<DocumentChange> documentChanges = newChatSnapShot.getDocumentChanges();
                        documentChanges.forEach(change -> {
                            QueryDocumentSnapshot document = change.getDocument();
                            ChatPojo pojo = new ChatPojo(document);
                            mModel.addMessages(pojo, this);
                        });
                    }
                });

    }
//        chat.get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        int index = 0;
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//
//                        }
//
//                    }

    public void sendMessageToFirebase(String roomName, String message) {
        if (!message.trim().equals("")){
            FirebaseManager.getInstance(roomName,this).sendMessageToFirebase(message, this);
        }
        mIChatView.clearEditText();
    }

    public void setListener(String roomName) {
//        FirebaseManager.getInstance(roomName,this).addMessageListeners();
    }

    public void onDestory(String roomName) {
//        FirebaseManager.getInstance(roomName,this).removeListeners();
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
