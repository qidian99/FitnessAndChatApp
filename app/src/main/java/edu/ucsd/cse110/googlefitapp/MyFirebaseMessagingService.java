package edu.ucsd.cse110.googlefitapp;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private AppCompatActivity activity;
    public MyFirebaseMessagingService(AppCompatActivity activity){
        this.activity = activity;
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("NEW_MSG", remoteMessage.toString());

    }
}