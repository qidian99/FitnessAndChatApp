package edu.ucsd.cse110.googlefitapp.chatroom.views;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import edu.ucsd.cse110.googlefitapp.R;
import edu.ucsd.cse110.googlefitapp.chatroom.models.ChatPojo;
import edu.ucsd.cse110.googlefitapp.chatroom.presenters.ChatPresenter;
import edu.ucsd.cse110.googlefitapp.chatroom.utils.MyUtils;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,IChatView {

    private EditText mEdittextChat;
    private ChatPresenter mChatPresenter;
    private RecyclerView mRecyclerView;
    private String mRoomName;
    private String from;
    private String to;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRoomName=getIntent().getStringExtra(MyUtils.EXTRA_ROOM_NAME);
        from=getIntent().getStringExtra("from");
        to=getIntent().getStringExtra("to");
        String friendEmail = getIntent().getStringExtra("friend");
        ((TextView)findViewById(R.id.text_header)).setText(friendEmail);
        mChatPresenter =new ChatPresenter(this);
//        mChatPresenter.setListener(mRoomName);
        CollectionReference chat = FirebaseFirestore.getInstance()
                .collection("chatroom")
                .document(mRoomName)
                .collection("messages");
        mChatPresenter.setChat(chat);
        mEdittextChat=(EditText) findViewById(R.id.edittext_chat_message);
        mRecyclerView=(RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        findViewById(R.id.button_send_message).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_send_message:
                mChatPresenter.sendMessageToFirebase(mRoomName,mEdittextChat.getText().toString(), from, to);
                break;
        }
    }

    @Override
    public void updateList(ArrayList<ChatPojo> list) {
        ChatAdapter chatAdapter=new ChatAdapter(this,list);
        mRecyclerView.setAdapter(chatAdapter);
        mRecyclerView.scrollToPosition(list.size()-1);
    }

    @Override
    public void clearEditText() {
        mEdittextChat.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatPresenter.onDestory(mRoomName);
    }
}
