package edu.ucsd.cse110.team4personalbest;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucsd.cse110.team4personalbest.firebase.ChatMessaging;
import edu.ucsd.cse110.team4personalbest.firebase.FirebaseMessageToChatMessageAdapter;
import edu.ucsd.cse110.team4personalbest.firebase.FirebaseStoreToStoreUnitAdapter;
import edu.ucsd.cse110.team4personalbest.firebase.StoreUnit;
import edu.ucsd.cse110.team4personalbest.firebase.TestMessaging;
import edu.ucsd.cse110.team4personalbest.firebase.TestStore;

public class FriendChat extends AppCompatActivity {

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

        boolean test = getIntent().getBooleanExtra("TEST", false);
        if(test){
            chatMessaging = new TestMessaging();
            chatStore = new TestStore(this);
        } else {
            chatMessaging = new FirebaseMessageToChatMessageAdapter();
            chatStore = new FirebaseStoreToStoreUnitAdapter(this);
        }

        final SharedPreferences sharedpreferences = getSharedPreferences("FirebaseLabApp", Context.MODE_PRIVATE);


        from = sharedpreferences.getString(FROM_KEY, null);
        subscribeToNotificationsTopic(chatMessaging);
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

    private void subscribeToNotificationsTopic(ChatMessaging chatMessaging) {
        chatMessaging.subscribe(this);
    }
}
