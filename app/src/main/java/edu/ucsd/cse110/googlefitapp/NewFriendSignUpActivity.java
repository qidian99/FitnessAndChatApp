package edu.ucsd.cse110.googlefitapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucsd.cse110.googlefitapp.dialog.HeightDialog;

public class NewFriendSignUpActivity extends AppCompatActivity {
    public static final String TAG = "NEW_FRIEND_ACTIVITY";
    EditText friendEmailTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        friendEmailTxt = findViewById(R.id.enterFriendEmail);
        friendEmailTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
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
                                Log.e(TAG, document.getId() + " => " + document.getData());
                                Log.e(TAG, " => ");
                                Log.e(TAG, "Requested friend's email => " + friendEmail);

                                if(document.getData().get("email").equals(friendEmail) ){
                                    Log.e(TAG, "User exists!");
                                    userExist = true;
                                    friendUid = (String) document.getData().get("id");
                                }
                            }

                            if(!userExist){
                                showToast("User does not exist");
                                return;
                            }

                            DocumentReference friendship = FirebaseFirestore.getInstance()
                                    .collection("friendship")
//                                    .document("user")
                                    .document(NewFriendSignUpActivity.this.getIntent().getStringExtra("uid"));
//                                    .collection(NewFriendSignUpActivity.this.getIntent().getStringExtra("uid"));
                            Map<String, Object> friend = new HashMap<>();
                            friend.put(friendUid, true);
                            friend.put("email", NewFriendSignUpActivity.this.getIntent().getStringExtra("email"));
//                            friend.put("email", friendEmail);
                            friendship.set(friend).addOnSuccessListener(result -> {
                                Log.e(TAG, "Friend request sent successfully");
                                friendEmailTxt.setText("");
                                showToast("Request Send");
                            }).addOnFailureListener(error -> {
                                Log.e(TAG, error.getLocalizedMessage());
                            });
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
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
}