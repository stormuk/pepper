package com.storm.posh;

import android.util.Log;

import com.storm.experiment1.PepperLog;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.Sense;

public class BehaviourLibrary {
    private static final String TAG = BehaviourLibrary.class.getSimpleName();

    private PepperLog pepperLog;

    private boolean haveWavedLeft = false;
    private boolean haveWavedRight = false;

    public BehaviourLibrary(PepperLog pepperLog) {
        this.pepperLog = pepperLog;
    }

    public void reset() {
        haveWavedLeft = false;
        haveWavedRight = false;
    }

    public boolean getBooleanSense(Sense sense) {
        pepperLog.appendLog(TAG, String.format("Getting boolean sense: %s", sense));
        switch(sense.getNameOfElement()) {
            case "HaveWavedLeft":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", haveWavedLeft));
                return haveWavedLeft;
            case "HaveWavedRight":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", haveWavedRight));
                return haveWavedRight;
        }
        return false;
    }
    public double getDoubleSense(Sense sense) {
        pepperLog.appendLog(TAG, "Getting double sense: "+sense);
        return 3.6;
    }
    public void executeAction(ActionEvent action) {
        pepperLog.appendLog(TAG, "Performing action: "+action);
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
        pepperLog.appendLog(TAG, "WAVING LEFT");
        this.haveWavedLeft = true;
    }

    private void waveRight() {
        pepperLog.appendLog(TAG, "WAVING RIGHT");
        this.haveWavedRight = true;
    }
}
