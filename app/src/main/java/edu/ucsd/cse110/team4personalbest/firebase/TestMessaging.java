package edu.ucsd.cse110.team4personalbest.firebase;

import android.widget.Toast;

import edu.ucsd.cse110.team4personalbest.FriendChat;

public class TestMessaging implements ChatMessaging {
    @Override
    public void subscribe(FriendChat activity) {
        String msg = activity.DOCUMENT_KEY;
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
