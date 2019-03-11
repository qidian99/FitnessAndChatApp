package edu.ucsd.cse110.googlefitapp.dialog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import edu.ucsd.cse110.googlefitapp.Activity;
import edu.ucsd.cse110.googlefitapp.R;
import edu.ucsd.cse110.googlefitapp.chatroom.utils.MyUtils;
import edu.ucsd.cse110.googlefitapp.chatroom.views.ChatActivity;

public class UserProfileDialog extends DialogFragment {
    private static final String TAG = "UserProfileDialog";
    private String userEmail;
    private String friendEmail;
    private Activity activity;
    private Button chatBtn;
    private Button statsBtn;
    private Button deleteBtn;
    private Button deleteYesBtn;
    private Button deleteNoBtn;
    private TextView emailText;
    private TextView deleteText;

    public UserProfileDialog() {}

    @SuppressLint("ValidFragment")
    public UserProfileDialog(String userEmail, String friendEmail, Activity activity) {
        this.activity = activity;
        this.userEmail = userEmail;
        this.friendEmail = friendEmail;
    }

    public static UserProfileDialog newInstance(String title, String userEmail, String friendEmail, Activity activity) {
        UserProfileDialog frag = new UserProfileDialog(userEmail, friendEmail, activity);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setStyle(0, R.style.Dialog);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = null;
        try {
            v = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.fragment_user_profile, container, false);
            getDialog().setTitle(getString(R.string.user_profile));
            Log.d(TAG, "onCreateView Success");
        } catch (Exception e) {
            Log.d(TAG, "onCreateView Fail: " + e.toString());
            e.printStackTrace();
        }
        return v;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            emailText = view.findViewById(R.id.user_email);
            String friendName = friendEmail.split("@")[0];
            String newText = emailText.getText().toString() + " " + friendName;
            emailText.setText(newText);
            chatBtn = view.findViewById(R.id.goChat);
            statsBtn = view.findViewById(R.id.goStats);
            deleteBtn = view.findViewById(R.id.deleteFriend);
            deleteYesBtn = view.findViewById(R.id.deleteYes);
            deleteNoBtn = view.findViewById(R.id.deleteNo);
            deleteText = view.findViewById(R.id.deleteConfirmText);
            String newDeleteText = deleteText.getText().toString() + "\n\"" + friendName + "\"?";
            deleteText.setText(newDeleteText);

            chatBtn.setOnClickListener(v -> {
                Log.d(TAG, "onViewCreated click chat");
                dismiss();
                openChat();
            });

            statsBtn.setOnClickListener(v -> {
                Log.d(TAG, "onViewCreated click stats");
            });

            deleteBtn.setOnClickListener(v -> {
                toggleDeleteFriendDialog();
                Log.d(TAG, "onViewCreated click delete friend");
            });

            deleteYesBtn.setOnClickListener(v -> {
                deleteFriend();
                Log.d(TAG, "onViewCreated confirm delete friend");
            });

            deleteNoBtn.setOnClickListener(v -> {
                toggleDeleteFriendDialog();
                Log.d(TAG, "onViewCreated cancel delete friend");
            });
        } catch (Exception e) {
            Log.d(TAG, "onViewCreated Fail: " + e.toString());
            e.printStackTrace();
        }
    }

    private void deleteFriend() {

    }

    private void openChat() {
        // open Chat
        String chatroomName = userEmail.compareTo(friendEmail) > 0 ? friendEmail + "TO" + userEmail : userEmail + "TO" + friendEmail;
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Error connecting to chat room");
                        } else {
                            Intent intent=new Intent(activity, ChatActivity.class);
                            intent.putExtra(MyUtils.EXTRA_ROOM_NAME, chatroomName);
                            intent.putExtra("friend", friendEmail);
                            intent.putExtra("from", userEmail);
                            intent.putExtra("to", friendEmail);
                            activity.startActivity(intent);                                                            }
                    }
                });
    }

    private void toggleDeleteFriendDialog() {
        if(emailText.getVisibility() == View.VISIBLE) {
            emailText.setVisibility(View.GONE);
            chatBtn.setVisibility(View.GONE);
            statsBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            deleteYesBtn.setVisibility(View.VISIBLE);
            deleteNoBtn.setVisibility(View.VISIBLE);
            deleteText.setVisibility(View.VISIBLE);
        } else {
            emailText.setVisibility(View.VISIBLE);
            chatBtn.setVisibility(View.VISIBLE);
            statsBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
            deleteYesBtn.setVisibility(View.GONE);
            deleteNoBtn.setVisibility(View.GONE);
            deleteText.setVisibility(View.GONE);
        }
    }
}
