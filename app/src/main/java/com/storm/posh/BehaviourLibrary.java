package com.storm.posh;

import android.util.Log;

import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.Sense;

public class BehaviourLibrary {
    private static final String TAG = BehaviourLibrary.class.getSimpleName();

    private boolean haveWavedLeft = false;
    private boolean haveWavedRight = false;

    public void reset() {
        haveWavedLeft = false;
        haveWavedRight = false;
    }

    public boolean getBooleanSense(Sense sense) {
        Log.d(TAG, String.format("Getting boolean sense: %s", sense));
        switch(sense.getNameOfElement()) {
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
        Log.d(TAG, "Getting double sense: "+sense);
        return 3.6;
    }
    public void executeAction(ActionEvent action) {
        Log.d(TAG, "Performing action: "+action);
        switch(action.getNameOfElement()) {
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
