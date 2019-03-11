package edu.ucsd.cse110.googlefitapp.chatroom.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import edu.ucsd.cse110.googlefitapp.chatroom.interfaces.FirebaseCallBacks;
import edu.ucsd.cse110.googlefitapp.chatroom.presenters.ChatPresenter;


/**
 * Created by saksham on 26/6/17.
 */

public class FirebaseManager implements ChildEventListener{
    private volatile static FirebaseManager sFirebaseManager;
    private CollectionReference chat;
    private FirebaseCallBacks mCallbacks;
    private String token;

    public static synchronized FirebaseManager getInstance(String roomName, FirebaseCallBacks callBacks) {
        if(sFirebaseManager == null) {
            synchronized (FirebaseManager.class) {
                sFirebaseManager = new FirebaseManager(roomName,callBacks);
            }
        }
        return sFirebaseManager;
    }

    private FirebaseManager(String roomName, FirebaseCallBacks callBacks){
//        chat = FirebaseDatabase.getInstance().getReference().child(roomName);
        chat = FirebaseFirestore.getInstance().collection("chatroom").document(roomName).collection("messages");
        this.mCallbacks = callBacks;
    }

//    public void addMessageListeners(){
//        chat.addChildEventListener(this);
//    }
//
//    public void removeListeners(){
//        chat.removeEventListener(this);
//    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        mCallbacks.onNewMessage(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public void sendMessageToFirebase(String message) {
        Map<String,Object> map=new HashMap<>();
        map.put("text",message);
        map.put("time",System.currentTimeMillis());
        map.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());

//        String keyToPush= chat.push().getKey();
//        chat.child(keyToPush).setValue(map);
        chat.add(map);
//        Log.e("FBMANAGER", keyToPush);
        Log.e("FBMANAGER", map.toString());
    }

    public void sendMessageToFirebase(String message, String from, String to, ChatPresenter chatPresenter) {
        token = null;
        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> map = document.getData();
                    if (map.get("email").equals(to)) {
                        token = (String) map.get("token");
                    }
                }

                Map<String, Object> map = new HashMap<>();
                map.put("text", message);
                map.put("time", System.currentTimeMillis());
                map.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                map.put("from", from);
                map.put("to", to);
                map.put("token", token);
                Log.e("UID", "GOT USER TOKEN: " + token);
                chat.add(map).addOnSuccessListener(result -> {
                }).addOnFailureListener(error -> {
                });
//        Log.e("FBMANAGER", keyToPush);
                Log.e("FBMANAGER", map.toString());
            }
        });
    }

    public void destroy() {
        sFirebaseManager=null;
        mCallbacks =null;
    }
}
