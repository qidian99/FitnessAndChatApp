package edu.ucsd.cse110.googlefitapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class NewFriendSignUpActivity extends AppCompatActivity {
    public static final String TAG = "NEW_FRIEND_ACTIVITY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

    }

    public void goBackToHomeScreen(View view) {
        finish();
        overridePendingTransition(R.anim.slide_l_to_r_enter, R.anim.slide_l_to_r_exit);
    }

    public void addFriend(View view){
        EditText friendEmailTxt = findViewById(R.id.enterFriendEmail);
        String friendEmail = friendEmailTxt.getText().toString();

        // Check of valid email
        if(isValidEmail(friendEmail)){
            // TODO: show some alert, etc
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
                                // TODO: show some alert, etc
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
        // TODO: add more rules!
        return true;
    }

    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(R.anim.slide_l_to_r_enter, R.anim.slide_l_to_r_exit);
    }
}