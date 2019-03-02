package edu.ucsd.cse110.googlefitapp.firebase;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucsd.cse110.googlefitapp.FriendChat;
import edu.ucsd.cse110.googlefitapp.R;

public class FirebaseStoreToStoreUnitAdapter implements StoreUnit {
    CollectionReference chat;
    FriendChat activity;
    public FirebaseStoreToStoreUnitAdapter(FriendChat activity){
        chat = FirebaseFirestore.getInstance()
                .collection(activity.COLLECTION_KEY)
                .document(activity.DOCUMENT_KEY)
                .collection(activity.MESSAGES_KEY);
        this.activity = activity;
        FirebaseApp.initializeApp(activity);

    }

    @Override
    public void initMessageUpdateListener() {
        chat.orderBy(activity.TIMESTAMP_KEY, Query.Direction.ASCENDING)
                .addSnapshotListener((newChatSnapShot, error) -> {
                    if (error != null) {
                        Log.e(activity.TAG, error.getLocalizedMessage());
                        return;
                    }

                    if (newChatSnapShot != null && !newChatSnapShot.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        List<DocumentChange> documentChanges = newChatSnapShot.getDocumentChanges();
                        documentChanges.forEach(change -> {
                            QueryDocumentSnapshot document = change.getDocument();
                            sb.append(document.get(activity.FROM_KEY));
                            sb.append(":\n");
                            sb.append(document.get(activity.TEXT_KEY));
                            sb.append("\n");
                            sb.append("---\n");
                        });


                        TextView chatView = activity.findViewById(R.id.chat);
                        chatView.append(sb.toString());
                    }
                });
    }

    @Override
    public void sendMessage() {
        if (activity.from == null || activity.from.isEmpty()) {
            Toast.makeText(activity, "Enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText messageView = activity.findViewById(R.id.text_message);

        Map<String, String> newMessage = new HashMap<>();
        newMessage.put(activity.FROM_KEY, activity.from);
        newMessage.put(activity.TEXT_KEY, messageView.getText().toString());

        chat.add(newMessage).addOnSuccessListener(result -> {
            messageView.setText("");
        }).addOnFailureListener(error -> {
            Log.e(activity.TAG, error.getLocalizedMessage());
        });
    }
}
