package edu.ucsd.cse110.googlefitapp.firebase;

import android.widget.EditText;
import android.widget.TextView;

import edu.ucsd.cse110.googlefitapp.FriendChat;
import edu.ucsd.cse110.googlefitapp.R;

public class TestStore implements StoreUnit {
    TextView chatView;
    FriendChat activity;

    public TestStore(FriendChat activity){
        this.activity = activity;
        this.chatView = activity.findViewById(R.id.chat);
    }

    @Override
    public void initMessageUpdateListener() {
        return; // We don't need to fetch any message when testing
    }

    @Override
    public void sendMessage() {
        StringBuilder sb = new StringBuilder();
        EditText messageView = activity.findViewById(R.id.text_message);

        sb.append(messageView.getText().toString());
        sb.append("\n");

        chatView.append(sb.toString());
    }
}
