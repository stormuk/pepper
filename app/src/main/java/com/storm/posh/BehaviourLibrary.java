package com.storm.posh;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.storm.experiment1.PepperLog;
import com.storm.experiment1.PepperServer;
import com.storm.experiment1.R;
import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.Sense;

public class BehaviourLibrary implements RobotLifecycleCallbacks {
    private static final String TAG = BehaviourLibrary.class.getSimpleName();

    private static BehaviourLibrary instance = null;

    private PepperLog pepperLog;

    private QiContext qiContext;

    private boolean animating = false;
    private boolean haveWavedLeft = false;
    private boolean haveWavedRight = false;

    private Animate animate = null;

    public BehaviourLibrary() { }

    public static BehaviourLibrary getInstance() {
        if (instance == null) {
            instance = new BehaviourLibrary();
        }
        return instance;
    }

    public void setPepperLog(PepperLog pepperLog) {
        this.pepperLog = pepperLog;
    }

    public void setQiContext(QiContext qiContext) {
        this.qiContext = qiContext;
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

    public void waveLeft() {
        if (animating) {
            pepperLog.appendLog(TAG, "WAVING IN PROGRESS");
            return;
        }

        pepperLog.appendLog(TAG, "WAVING LEFT: starting");
        setAnimating(true);

        // Create an animation object.
        Future<Animation> myAnimationFuture = AnimationBuilder.with(qiContext)
                .withResources(R.raw.raise_left_hand_b007)
                .buildAsync();

        myAnimationFuture.andThenConsume(myAnimation -> {
            Animate animate = AnimateBuilder.with(qiContext)
                    .withAnimation(myAnimation)
                    .build();

            // Run the action synchronously in this thread
            animate.run();

            pepperLog.appendLog(TAG, "WAVING LEFT: finished");
            setHaveWavedLeft(true);
            setAnimating(false);
        });
    }

    private void waveRight() {
        if (animating) {
            pepperLog.appendLog(TAG, "WAVING IN PROGRESS");
            return;
        }

        pepperLog.appendLog(TAG, "WAVING RIGHT: starting");
        setAnimating(true);

        // Create an animation object.
        Future<Animation> myAnimationFuture = AnimationBuilder.with(qiContext)
                .withResources(R.raw.raise_right_hand_b007)
                .buildAsync();

        myAnimationFuture.andThenConsume(myAnimation -> {
            Animate animate = AnimateBuilder.with(qiContext)
                    .withAnimation(myAnimation)
                    .build();

            // Run the action synchronously in this thread
            animate.run();

            pepperLog.appendLog(TAG, "WAVING RIGHT: finished");
            setHaveWavedRight(true);
            setAnimating(false);
        });
    }

    // tidy up listeners
    public void removeListeners() {

    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        pepperLog.appendLog(TAG, "GAINED FOCUS");
        BehaviourLibrary.getInstance().setQiContext(qiContext);

        waveLeft();

        // The robot focus is gained.
//
//        // Create a new say action.
//        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
//                .withText("Hello human!") // Set the text to say.
//                .build(); // Build the say action.
//
//        // Execute the action.
//        say.run();
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        BehaviourLibrary.getInstance().removeListeners();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    public void setAnimating(boolean state) {
        this.animating = state;
    }

    public void setHaveWavedLeft(boolean state) {
        this.haveWavedLeft = state;
    }
    public void setHaveWavedRight(boolean state) {
        this.haveWavedRight = state;
    }
}
