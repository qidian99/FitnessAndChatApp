package edu.ucsd.cse110.googlefitapp.firebase;

import android.widget.Toast;

import edu.ucsd.cse110.googlefitapp.MainActivity;

public class TestMessaging implements ChatMessaging {
    @Override
    public void subscribe(MainActivity activity) {
        String msg = MainActivity.DOCUMENT_KEY;
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
