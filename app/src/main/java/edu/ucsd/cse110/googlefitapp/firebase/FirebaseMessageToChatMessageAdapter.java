package edu.ucsd.cse110.googlefitapp.firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import edu.ucsd.cse110.googlefitapp.MainActivity;

public class FirebaseMessageToChatMessageAdapter implements ChatMessaging {
    String TAG = FirebaseMessageToChatMessageAdapter.class.getSimpleName();

    @Override
    public void subscribe(MainActivity activity) {
        FirebaseMessaging.getInstance().subscribeToTopic(MainActivity.DOCUMENT_KEY)
                .addOnCompleteListener(task -> {
                            String msg = "Subscribed to notifications";
                            if (!task.isSuccessful()) {
                                msg = "Subscribe to notifications failed";
                            }
                            Log.d(TAG, msg);
//                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                        }
                );
    }
}
