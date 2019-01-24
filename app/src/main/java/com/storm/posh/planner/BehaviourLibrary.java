package com.storm.posh.planner;

import android.util.Log;

import com.storm.posh.planner.planelements.Action;
import com.storm.posh.planner.planelements.Sense;

public class BehaviourLibrary {
    private static final String TAG = BehaviourLibrary.class.getSimpleName();

    private boolean haveWavedLeft = false;
    private boolean haveWavedRight = false;

    public void reset() {
        haveWavedLeft = false;
        haveWavedRight = false;
    }

    public boolean getBooleanSense(Sense sense) {
        Log.d(TAG, String.format("Getting boolean sense: %s", sense.name));
        switch(sense.name) {
            case "HaveWavedLeft":
                Log.d(TAG, String.format("Sense value is: %b", haveWavedLeft));
                return haveWavedLeft;
            case "HaveWavedRight":
                Log.d(TAG, String.format("Sense value is: %b", haveWavedRight));
                return haveWavedRight;
        }
        return false;
    }
    public double getDoubleSense(Sense sense) {
        Log.d(TAG, "Getting double sense: "+sense.name);
        return 3.6;
    }
    public void executeAction(Action action) {
        Log.d(TAG, "Performing action: "+action.name);
        switch(action.name) {
            case "WaveLeft":
                waveLeft();
                break;

            case "WaveRight":
                waveRight();
                break;

            default:
                Log.d(TAG, "UNKNOWN ACTION");
                break;
        }
    }

    private void waveLeft() {
        Log.d(TAG, "WAVING LEFT");
        this.haveWavedLeft = true;
    }

    private void waveRight() {
        Log.d(TAG, "WAVING RIGHT");
        this.haveWavedRight = true;
    }
}
