package edu.ucsd.cse110.googlefitapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.core.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import edu.ucsd.cse110.googlefitapp.chatroom.utils.MyUtils;
import edu.ucsd.cse110.googlefitapp.chatroom.views.ChatActivity;

import static edu.ucsd.cse110.googlefitapp.chatroom.utils.MyUtils.EXTRA_ROOM_NAME;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);

        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = notification.getTitle();
        String message = notification.getBody();
        String click_action = notification.getClickAction();

//
//        Intent intent = new Intent(click_action);
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "chatroom").setContentTitle(title).setContentText(message).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setContentIntent(pendingIntent);
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notificationBuilder.build());


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = null;
            notificationChannel = new NotificationChannel("chatroom", "chatroom", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            notificationManager.createNotificationChannel(notificationChannel);
        }


        Map<String, String> data = remoteMessage.getData();
//        Log.e("Class Context", getApplicationContext().getClass().toString());
//        !getApplicationContext().getClass().equals(ChatActivity.class) &&
        if (data.containsKey("click_action")) {
            Log.e("RECEIVE_NTFCT", notification.getTitle() + ", " + remoteMessage.getFrom());
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getApplicationContext(), notification.getTitle(), Toast.LENGTH_SHORT).show());
            //        sendNotification(notification.getTitle(), notification.getBody(), map);
            Log.e("FBService", "Room Name: " + data.get("roomName"));
            Log.e("FBService", "From " + data.get("roomName").substring(0, data.get("roomName").indexOf("TO")));
            Log.e("FBService", "To " + data.get("roomName"));
            Intent intent = new Intent(this, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(EXTRA_ROOM_NAME, data.get("roomName"));
            intent.putExtra("from", data.get("roomName").substring(0, data.get("roomName").indexOf("TO")));
            intent.putExtra("to", data.get("to"));
            intent.putExtra("friend", data.get("roomName").substring(0, data.get("roomName").indexOf("TO")));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "chatroom").setContentTitle(title).setContentText(message).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setContentIntent(pendingIntent);
            notificationManager.notify(0, notificationBuilder.build());
        }
//        Map<String, String> data = remoteMessage.getData();
//        if (data.containsKey("click_action")) {
//            ClickActionHelper.startActivity(data.get("click_action"), null, this);
//        }
    }

    private void sendNotification(String title, String body, Map<String, String> map) {
        Log.e("SEND_NTFCT", title);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(title)
                .setLargeIcon(icon)
                .setSmallIcon(R.mipmap.ic_launcher);

        try {
            String picture_url = map.get("picture_url");
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(body));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        notificationBuilder.setLights(Color.YELLOW, 1000, 300);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}

class ClickActionHelper {
    public static void startActivity(String className, Bundle extras, Context context){
        Class cls = ChatActivity.class;
        String roomName = (String)extras.get("roomName");
        String to = (String)extras.get("to");
        Intent i = new Intent(context, cls);
        i.putExtra("from", roomName.substring(0, roomName.indexOf("TO")));
        i.putExtra("to", to);
        i.putExtra("friend", roomName.substring(0, roomName.indexOf("TO")));
        i.putExtra(EXTRA_ROOM_NAME, roomName);

        context.startActivity(i);
    }
}