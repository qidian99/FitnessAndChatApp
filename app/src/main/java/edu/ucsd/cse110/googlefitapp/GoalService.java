package edu.ucsd.cse110.googlefitapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;

public class GoalService extends Service {

    private static boolean running = false;

    public static boolean isRunning() {
        return running;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("GoalService", "GoalService Started");
        Thread thread = new Thread(new MyThread(startId));
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.i("GoalService", "GoalService Stopped");
        super.onDestroy();
    }

    final class MyThread implements Runnable {
        int startId;

        public MyThread(int startId) {
            this.startId = startId;
        }

        @Override
        public void run() {
            synchronized (this) {
                running = true;
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

                while (running) {
                    if (!sharedPreferences.getBoolean("goalChangeable", false)) {
                        try {
                            wait(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("GoalService", "Cannot change goal");
                        continue;
                    }

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


                        Intent intent = new Intent(GoalService.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("goalReached", true);
                        PendingIntent pendingIntent = PendingIntent.getActivity(GoalService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(GoalService.this, "goal").setContentTitle("Congratulation!").setContentText("You have reached your goal! Click to set a new one.").setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setContentIntent(pendingIntent);

                        notificationManager.notify(0, notificationBuilder.build());
                        stopSelf(startId);
                        running = false;
                    }


                    try {
                        wait(5000);
                        Log.d("GoalService", "Check if can change goal");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}