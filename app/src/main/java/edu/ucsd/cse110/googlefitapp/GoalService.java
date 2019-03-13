package edu.ucsd.cse110.googlefitapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.fitness.data.Goal;

import java.util.Map;

import edu.ucsd.cse110.googlefitapp.chatroom.views.ChatActivity;

import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;
import static edu.ucsd.cse110.googlefitapp.chatroom.utils.MyUtils.EXTRA_ROOM_NAME;

public class GoalService extends Service {

    private static boolean running = false;
    final class MyThread implements Runnable
    {
        int startId;
        public MyThread(int startId){
            this.startId = startId;
        }

        @Override
        public void run() {
            synchronized (this)
            {
                running = true;
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

//                boolean goalChangeable = sharedPreferences.getBoolean("goalChangeable", false);
//                if(!goalChangeable){
//                    stopSelf(startId);
//                    running = false;
//                    return;
//                }

                while(running) {
                    int goal = sharedPreferences.getInt("goal", 5000);
                    int step = sharedPreferences.getInt("step", 0);

                    if (step >= goal) {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            int importance = NotificationManager.IMPORTANCE_LOW;
                            NotificationChannel notificationChannel = null;
                            notificationChannel = new NotificationChannel("goal", "goal", importance);
                            notificationChannel.enableLights(true);
                            notificationChannel.setLightColor(Color.RED);
                            notificationChannel.enableVibration(true);
                            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                            notificationManager.createNotificationChannel(notificationChannel);
                        }


                        //        Log.e("Class Context", getApplicationContext(
                        Intent intent = new Intent(GoalService.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("goalReached", true);
                        //                        intent.putExtra("from", data.get("roomName").substring(0, data.get("roomName").indexOf("TO")));
                        //                        intent.putExtra("to", data.get("to"));
                        //                        intent.putExtra("friend", data.get("roomName").substring(0, data.get("roomName").indexOf("TO")));
                        PendingIntent pendingIntent = PendingIntent.getActivity(GoalService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                        //                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "chatroom").setContentTitle("Congratulation!").setContentText("You have reached your goal! Click to set a new one.").setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setContentIntent(pendingIntent);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(GoalService.this, "goal").setContentTitle("Congratulation!").setContentText("You have reached your goal! Click to set a new one.").setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setContentIntent(pendingIntent);

                        notificationManager.notify(0, notificationBuilder.build());
                        stopSelf(startId);
                        running = false;
                    }


                    try {
                        wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
//
//            stopSelf(startId);
//            running = false;
        }
    }


    public static boolean isRunning() {
        return running;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(GoalService.this, "Service Started", Toast.LENGTH_SHORT).show();
        Thread thread = new Thread(new MyThread(startId));
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy(){
        Toast.makeText(GoalService.this, "Service Stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}