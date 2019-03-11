package edu.ucsd.cse110.googlefitapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucsd.cse110.googlefitapp.dialog.HeightDialog;

public class NewFriendSignUpActivity extends AppCompatActivity {
    public static final String TAG = "NEW_FRIEND_ACTIVITY";
    EditText friendEmailTxt;
    private boolean test;
    private ArrayList<String> myFriendList = new ArrayList<>();
    private ArrayList<String> userList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        test = getIntent().getBooleanExtra("testkey", false);

        friendEmailTxt = findViewById(R.id.enterFriendEmail);
        friendEmailTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addFriend(v);
                    return true;
                }
                return false;
            }
        });
    }

    public void goBackToHomeScreen(View view) {
        finish();
        overridePendingTransition(R.anim.slide_l_to_r_enter, R.anim.slide_l_to_r_exit);
    }

    public void addFriend(View view){
        String friendEmail = friendEmailTxt.getText().toString();

        // Check of valid email
        if(!isValidEmail(friendEmail)){
            showToast("Email is invalid");
            return;
        }

        if(!test) {
            // Get all users from Database and match each of them with the email
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean userExist = false;
                                String friendUid = null;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    Log.d(TAG, " => ");
                                    Log.d(TAG, "Requested friend's email => " + friendEmail);

                                    if (document.getData().get("email").equals(friendEmail)) {
                                        Log.d(TAG, "User exists!");
                                        userExist = true;
                                        friendUid = (String) document.getData().get("id");
                                    }
                                }

                                if (!userExist) {
                                    showToast("User does not exist");
                                    return;
                                }

                                String uid = NewFriendSignUpActivity.this.getIntent().getStringExtra("uid");

                                Map<String, Object> friend = new HashMap<>();
                                friend.put(friendUid, true);
                                friend.put("email", NewFriendSignUpActivity.this.getIntent().getStringExtra("email"));
        //                            friend.put("email", friendEmail);

                                FirebaseFirestore.getInstance().collection("friendship").document(uid).set(friend, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Friend request sent successfully");
                                                friendEmailTxt.setText("");
                                                showToast("Request Send");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, e.getLocalizedMessage());
                                            }
                                        });
                            } else {
                                Log.e(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {
            if(userList.indexOf(friendEmail) != -1) {
                showToast("User exists!");
                myFriendList.add(friendEmail);
            } else {
                showToast("User does not exist!");
            }
        }
    }

    private boolean isValidEmail(String friendEmail) {
        // TODO: Use regex - edited by Enqi
        String regex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(friendEmail);
        return m.matches();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(R.anim.slide_l_to_r_enter, R.anim.slide_l_to_r_exit);
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }

    public void setFriendList(ArrayList<String> friendList) {
        this.myFriendList = friendList;
    }

    public String getUser(int i) {
        return this.userList.get(i);
    }

    public String getFriend(int i) {
        return this.myFriendList.get(i);
    }

    public ArrayList<String> getUserList() {
        return this.userList;
    }

    public ArrayList<String> getFriendList() {
        return this.myFriendList;
    }
}