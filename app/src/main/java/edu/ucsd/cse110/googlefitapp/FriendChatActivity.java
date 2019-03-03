package edu.ucsd.cse110.googlefitapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import edu.ucsd.cse110.googlefitapp.firebase.ChatMessaging;
import edu.ucsd.cse110.googlefitapp.firebase.FirebaseMessageToChatMessageAdapter;
import edu.ucsd.cse110.googlefitapp.firebase.FirebaseStoreToStoreUnitAdapter;
import edu.ucsd.cse110.googlefitapp.firebase.StoreUnit;
import edu.ucsd.cse110.googlefitapp.firebase.TestMessaging;
import edu.ucsd.cse110.googlefitapp.firebase.TestStore;

public class FriendChatActivity extends AppCompatActivity {

    public static final String TAG = "FRIEND_CHAT";
    public static final String COLLECTION_KEY = "chats";
    public static final String DOCUMENT_KEY = "chat1";
    public static final String MESSAGES_KEY = "messages";
    public String FROM_KEY = "from";
    public String TEXT_KEY = "text";
    public String TIMESTAMP_KEY = "timestamp";

    StoreUnit chatStore;
    public String from;

    ChatMessaging chatMessaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_chat);
//
//        boolean test = getIntent().getBooleanExtra("TEST", false);
//        if(test){
//            chatMessaging = new TestMessaging();
//            chatStore = new TestStore(this);
//        } else {
//            chatMessaging = new FirebaseMessageToChatMessageAdapter();
//            chatStore = new FirebaseStoreToStoreUnitAdapter(this);
//        }

        chatStore = new FirebaseStoreToStoreUnitAdapter(this);

        final SharedPreferences sharedpreferences = getSharedPreferences("FirebaseLabApp", Context.MODE_PRIVATE);


        from = sharedpreferences.getString(FROM_KEY, null);
//        subscribeToNotificationsTopic(chatMessaging);
        chatStore.initMessageUpdateListener();
        findViewById(R.id.btn_send).setOnClickListener(view -> chatStore.sendMessage());

        EditText nameView = findViewById((R.id.user_name));
        nameView.setText(from);
        nameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                from = s.toString();
                sharedpreferences.edit().putString(FROM_KEY, from).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

//    private void subscribeToNotificationsTopic(ChatMessaging chatMessaging) {
//        chatMessaging.subscribe(this);
//    }
}
