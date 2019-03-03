package edu.ucsd.cse110.googlefitapp.firebase;

import android.widget.Toast;

import edu.ucsd.cse110.googlefitapp.FriendChat;

public class TestMessaging implements ChatMessaging {
    @Override
    public void subscribe(FriendChat activity) {
        String msg = activity.DOCUMENT_KEY;
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
