package edu.ucsd.cse110.googlefitapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class Encouragement {

    private static boolean turnOn;
    private static boolean onlyDaily = false;
    private static final int QUARTER_MSG = 0;
    private static final int HALFWAY_MSG = 1;
    private static final int CLOSE_MSG = 2;
    private Activity activity;

    private String[] msgs =
            {"You are on your way. Keep it up!",
            "You are halfway to your goal!",
            "You are close to your goal!",
            "You have nearly doubled your steps since yesterday!"};

    /* ctor */
    public Encouragement(Activity activity, boolean onlyDaily) {
        this.activity = activity;
        this.onlyDaily = onlyDaily;
    }

    public static void turnOnEncouragement() {
        turnOn = true;
    }

    public static void turnOffEncouragement() {
        turnOn = false;
    }

    public static void setOnlyDaily() {
        onlyDaily = true;

    }

    public static void setEveryLaunch() {
        onlyDaily = false;
    }

    /*public boolean isEncouragementOn() {}*/

    public void getActiveEncouragement(long current, long goal) {
        if (current < goal) {
            Toast.makeText(activity,determineActiveMessage(current,goal),Toast.LENGTH_SHORT).show();
        }
    }

    public void getPassiveEncouragement(long current, long goal) {
        if (current < goal) {
            Toast.makeText(activity,determinePassiveMessage(current,goal),Toast.LENGTH_SHORT).show();
        }
    }

    private String determineActiveMessage(long current, long goal) {
        double percent = getPercent(current,goal);
        if (0 < percent && percent <= 40) {
            return msgs[QUARTER_MSG];
        }
        if (40 < percent && percent <= 60) {
            return msgs[HALFWAY_MSG];
        }
        if (80 < percent && percent <= 95) {
            return msgs[CLOSE_MSG];
        }
        return msgs[QUARTER_MSG];
    }

    private String determinePassiveMessage(long current, long goal) {

        double percent = getPercent(current,goal);
        /*Toast.makeText(activity,new String(Double.toString(percent)),Toast.LENGTH_LONG).show();*/
        if (0 < percent && percent <= 40) {
            return msgs[QUARTER_MSG];
        }
        if (40 < percent && percent <= 60) {
            return msgs[HALFWAY_MSG];
        }
        if (80 < percent && percent <= 95) {
            return msgs[CLOSE_MSG];
        }
        return msgs[QUARTER_MSG];
    }

    private double getPercent(long current, long goal) {
        return ((double)current/goal) * 100;
    }
}
