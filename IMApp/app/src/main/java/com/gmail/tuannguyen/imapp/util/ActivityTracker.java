package com.gmail.tuannguyen.imapp.util;

/**
 * Created by tuannguyen on 4/29/16.
 */
public class ActivityTracker {

    private static int numActiveActivities = 0;
    public static boolean lockScreenRequired = true;
    public static boolean wholeAppPaused = false;
    public static void activityStarted() {
        numActiveActivities++;

        if (numActiveActivities == 0) {
            //When the whole application started
            wholeAppPaused = false;
        }
    }

    public static void activityStopped() {
        numActiveActivities--;
        if (numActiveActivities == 0) {
            //When the whole application paused
            wholeAppPaused = true;
            lockScreenRequired = true;
        }
    }
}
