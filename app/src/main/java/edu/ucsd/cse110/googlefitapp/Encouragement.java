package edu.ucsd.cse110.googlefitapp;

import android.app.Activity;
import android.widget.Toast;

public class Encouragement {

    private Activity activity;
    private static boolean turnOn;
    private static final int QUARTER_MSG = 0;
    private static final int HALFWAY_MSG = 1;
    private static final int CLOSE_MSG = 2;
    private String[] msgs =
            {"You are on your way. Keep it up!",
            "You are halfway to your goal!",
            "You are really close to your goal!",
            "You have nearly doubled your steps since yesterday!"};

    public Encouragement(Activity activity) {
        this.activity = activity;
    }

    public void turnOnEncouragement() {
        turnOn = true;
    }

    public void turnOffEncouragement() {
        turnOn = false;
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
        if (60 < percent && percent <= 60) {
            return msgs[CLOSE_MSG];
        }
        return msgs[QUARTER_MSG];
    }

    private String determinePassiveMessage(long current, long goal) {
        double percent = getPercent(current,goal);
        if (0 < percent && percent <= 40) {
            return msgs[QUARTER_MSG];
        }
        if (40 < percent && percent <= 60) {
            return msgs[HALFWAY_MSG];
        }
        if (60 < percent && percent <= 60) {
            return msgs[CLOSE_MSG];
        }
        return msgs[QUARTER_MSG];
    }

    private double getPercent(long current, long goal) {
        return current/(double)goal;
    }
}
