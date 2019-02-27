package com.storm.posh;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.AttachedFrame;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.EngageHuman;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.storm.experiment1.MainActivity;
import com.storm.experiment1.PepperLog;
import com.storm.experiment1.PepperServer;
import com.storm.experiment1.R;
import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.Sense;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BehaviourLibrary implements RobotLifecycleCallbacks {
    private static final String TAG = BehaviourLibrary.class.getSimpleName();

    private static BehaviourLibrary instance = null;

    private PepperLog pepperLog;

    private QiContext qiContext;
    private MainActivity activity;

    private boolean talking = false;
    private boolean listening = false;
    private boolean animating = false;

    private boolean humanPresent = false;
    private boolean facingNearHuman = false;
    private boolean doNotAnnoy = false;

    private boolean heardStop = false;

    private boolean haveWavedLeft = false;
    private boolean haveWavedRight = false;

    private boolean safeToMap = true;
    private boolean mappingStarted = false;
    private boolean mappingComplete = false;

    // Store the HumanAwareness service.
    private HumanAwareness humanAwareness;

    // Store the EngageHuman action.
    private EngageHuman engageHuman;

    // Store the LocalizeAndMap action.
    private LocalizeAndMap localizeAndMap;
    // Store the map.
    private ExplorationMap explorationMap;
    // Store the LocalizeAndMap execution.
    private Future<Void> localizationAndMapping;
    // Store the Localize action.
    private Localize localize;
    private Future<Void> localization;

    private Date idleTimerStart;

    private int batteryPercent = 75;


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

    public void setActivity(MainActivity activity) { this.activity = activity; }

    public void setQiContext(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    public boolean hasQiContext() {
        return (qiContext != null);
    }

    public void reset() {
        removeListeners();
        haveWavedLeft = false;
        haveWavedRight = false;
    }

    public boolean getBooleanSense(Sense sense) {
//        pepperLog.appendLog(TAG, String.format("Getting boolean sense: %s", sense));
        switch(sense.getNameOfElement()) {
            case "HaveWavedLeft":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", haveWavedLeft));
                return haveWavedLeft;
            case "HaveWavedRight":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", haveWavedRight));
                return haveWavedRight;
            case "Talking":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", talking));
                return talking;
            case "Listening":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", listening));
                return listening;
            case "Animating":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", animating));
                return animating;
            case "HumanPresent":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", humanPresent));
                return humanPresent;
            case "FacingNearHuman":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", facingNearHuman));
                return facingNearHuman;
            case "HeardStop":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", heardStop));
                return heardStop;
            case "MappingComplete":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", mappingComplete));
                return mappingComplete;
            case "DoNotAnnoy":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", doNotAnnoy));
                return doNotAnnoy;
            case "SafeToMap":
                pepperLog.appendLog(TAG, String.format("Sense value is: %b", safeToMap));
                return safeToMap;
        }
        return false;
    }
    public double getDoubleSense(Sense sense) {
//        pepperLog.appendLog(TAG, String.format("Getting double sense: %s", sense));
        switch(sense.getNameOfElement()) {
            case "IdleTime":
                double idleTime = getIdleTime();
                pepperLog.appendLog(TAG, String.format("Sense value is: %d", idleTime));
                return idleTime;
            case "BatteryPercent":
                double batteryPercent = getBatteryPercent();
                pepperLog.appendLog(TAG, String.format("Sense value is: %d", batteryPercent));
                return batteryPercent;
        }

        return 0;
    }

    private double getIdleTime() {
        if (idleTimerStart == null) {
            return -1;
        } else {
            Date now = new Date();
            return (now.getTime() - idleTimerStart.getTime()) / 1000;
        }
    }

    private double getBatteryPercent() {
        return Double.valueOf(this.batteryPercent);
    }

    private boolean getHumansPresent() {
        return false;
//        return (Math.random() < 0.4);
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

            case "PromptForBatteryCharge":
                promptForBatteryCharge();
                break;

            case "ApproachHuman":
                approachHuman();
                break;

            case "ListenForStop":
                listenForStop();

            case "DoNotAnnoy":
                this.doNotAnnoy = true;
                break;

            case "DoMapping":
                doMapping();
                break;

            case "StartIdleTiming":
                this.idleTimerStart = new Date();
                break;

            case "ForgetMap":
                forgetMap();
                break;

            case "ForgetAnnoy":
                this.doNotAnnoy = false;
                break;

            case "Hum":
                hum();
                break;

            default:
                Log.d(TAG, "UNKNOWN ACTION");
                break;
        }
    }

    public void promptForBatteryCharge() {
        if (talking) {
            pepperLog.appendLog(TAG, "Cannot prompt, already talking");
            return;
        }

        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("My battery is low, please plug me in") // Set the text to say.
                .build(); // Build the say action.

        say.addOnStartedListener(() -> {
            this.talking = true;
            pepperLog.appendLog(TAG, "Starting prompting");
        });

        // Execute the action.
        Future<Void> sayFuture = say.async().run();

        sayFuture.thenConsume(future -> {
           this.talking = false;
           handleFuture(future, "battery_prompt");
        });

    }

    public void listenForStop() {
        pepperLog.appendLog(TAG, "Listen for stop?");
        if (listening) {
            pepperLog.appendLog(TAG, "Already listening");
            return;
        }

        this.heardStop = false;

        PhraseSet phraseSet = PhraseSetBuilder.with(qiContext).withTexts("Stop", "Go Away", "No").build();
        Listen listen = ListenBuilder.with(qiContext).withPhraseSet(phraseSet).build();

        listen.addOnStartedListener(() -> {
            pepperLog.appendLog("Started listening...");
        });

        Future<ListenResult> listenFuture = listen.async().run();

        listenFuture.thenConsume(future -> {
            this.listening = false;
            handleFuture(future, "listen_for_stop");

            try {
                ListenResult result = future.get();

                PhraseSet heardPhraseSet = result.getMatchedPhraseSet();
                Phrase heardPhrase = result.getHeardPhrase();

                pepperLog.appendLog(TAG, String.format("Phrase was: %s", heardPhrase));

                if (heardPhrase.getText() == "Stop") {
                    heardStop = true;
                }

                if (result.equals("Stop")) {
                    pepperLog.appendLog(TAG, "Heard equals Stop");
                }

            } catch (ExecutionException e) {
                pepperLog.appendLog(TAG, "Error occurred when listening for stop");
            } catch (CancellationException e) {
                pepperLog.appendLog(TAG, "Listening for stop was cancelled");
            }
        });
    }

    private void handleFuture(Future future, String label) {
        if (future.isSuccess()) {
            pepperLog.appendLog(TAG,String.format("Future %s done", label));
        } else if (future.hasError()) {
            pepperLog.appendLog(TAG, String.format("Future %s error: %s", label, future.getErrorMessage()));
        } else if (future.isCancelled()) {
            pepperLog.appendLog(TAG, String.format("Future %s cancelled", label));
        }
    }

    public void approachHuman() {
        pepperLog.appendLog(TAG, "Approach human?");
        if (!humanPresent) {
            pepperLog.appendLog(TAG, "Cannot approach when no human present");
            return;
        } else if (animating){
            pepperLog.appendLog(TAG, "Cannot approach, already animating");
            return;
        }

        humanAwareness = qiContext.getHumanAwareness();
        Human recommendedHuman = humanAwareness.getRecommendedHumanToEngage();

        if (recommendedHuman != null) {
            pepperLog.appendLog(TAG, String.format("Recommended human: %s", recommendedHuman));

            followHuman(recommendedHuman);

        } else {
            pepperLog.appendLog(TAG, "No recommended human");
        }
    }

    public void doHumans() {
        pepperLog.appendLog(TAG, "Initialising human tracking");
        this.humanPresent = false;
        humanAwareness = qiContext.getHumanAwareness();

        humanAwareness.removeAllOnHumansAroundChangedListeners();
        humanAwareness.addOnHumansAroundChangedListener(this::updateHumansAround);
    }

    private void updateHumansAround(List<Human> humansAround) {
        if (humansAround.isEmpty()) {
            this.humanPresent = false;
            pepperLog.appendLog(TAG, "No humans around");
        } else {
            this.humanPresent = true;
//            approachHuman();

            if (humansAround.size() == 1) {
                pepperLog.appendLog(TAG, "There is one human near");
            } else {
                pepperLog.appendLog(TAG, String.format("There are %d humans near", humansAround.size()));
            }
        }
    }

    public void doMapping() {
        pepperLog.appendLog(TAG, "Do mapping?");
        if (mappingStarted) {
            pepperLog.appendLog(TAG, "Mapping in progress");
            return;
        } else if (animating) {
            pepperLog.appendLog(TAG, "Cannot map, already animating");
        }

        setAnimating(true);


        // Create a LocalizeAndMap action.
        pepperLog.appendLog(TAG, qiContext.toString());
        localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build();

        // Add an on status changed listener on the LocalizeAndMap action for the robot to say when he is localized.
        localizeAndMap.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    // Dump the ExplorationMap.
                    explorationMap = localizeAndMap.dumpMap();

                    String message = "Robot has mapped his environment.";
                    pepperLog.appendLog(TAG, message);

                    pepperLog.appendLog(TAG, "I now have a map of my environment. I will use this map to localize myself.");

                    int mapElementCount = explorationMap.serialize().split(" ").length;
                    pepperLog.appendLog(TAG, String.format("Map has %d elements", mapElementCount));

                    // Cancel the LocalizeAndMap action.
                    localizationAndMapping.requestCancellation();
                    break;
                default:
                    pepperLog.appendLog(TAG, String.format("Mapping status: %s", status));
            }
        });

        String message = "Mapping...";
        this.mappingStarted = true;
        pepperLog.appendLog(TAG, message);

        // Execute the LocalizeAndMap action asynchronously.
        localizationAndMapping = localizeAndMap.async().run();

        // Add a lambda to the action execution.
        localizationAndMapping.thenConsume(future -> {
            if (future.hasError()) {
                String errorMessage = "LocalizeAndMap action finished with error.";
                pepperLog.appendLog(TAG, String.format("%s: %s", errorMessage, future.getError()));
                this.mappingStarted = false;
            } else if (future.isCancelled()) {
                // cancelled means we're done mapping the environment and can use it
                this.mappingComplete = true;
                startLocalizing(qiContext);
            }

            setAnimating(false);
        });
    }

    private void startLocalizing(QiContext qiContext) {
        // Create a Localize action.
        localize = LocalizeBuilder.with(qiContext)
                .withMap(explorationMap)
                .build();

        // Add an on status changed listener on the Localize action for the robot to say when he is localized.
        localize.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    pepperLog.appendLog(TAG, "Localization successful!");
                    break;
            }
        });

        String message = "Localizing...";
        pepperLog.appendLog(TAG, message);

        // Execute the Localize action asynchronously.
        localization = localize.async().run();

        // Add a lambda to the action execution.
        localization.thenConsume(future -> {
            if (future.hasError()) {
                String errorMessage = "Localize action finished with error.";
                pepperLog.appendLog(TAG, String.format("%s: %s", errorMessage, future.getError()));
            } else if (future.isCancelled()) {
                pepperLog.appendLog(TAG, "Localization is now cancelled");
            }
        });
    }

    public void forgetMap() {
        pepperLog.appendLog(TAG, "Forgetting map");
        if (localization != null) {
            pepperLog.appendLog(TAG, "Requesting localization cancellation...");
            localization.requestCancellation();
        }
        this.explorationMap = null;
        this.mappingComplete = false;
    }

    public void hum() {
        if (talking) {
            return;
        }

        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Do bee doo") // Set the text to say.
                .build(); // Build the say action.

        say.addOnStartedListener(() -> {
            this.talking = true;
            pepperLog.appendLog(TAG, "Starting humming");
        });

        // Execute the action.
        Future<Void> sayFuture = say.async().run();

        sayFuture.thenConsume(future -> {
            this.talking = false;
            handleFuture(future, "hum");
        });
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
                .withResources(R.raw.left_hand_high_b001)
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
                .withResources(R.raw.right_hand_high_b001)
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
        // Remove on started listeners from the GoTo action.
        if (goTo != null) {
            goTo.removeAllOnStartedListeners();
        }

        // Remove on status changed listeners from the LocalizeAndMap action.
        if (localizeAndMap != null) {
            localizeAndMap.removeAllOnStatusChangedListeners();
        }
        // Remove on status changed listeners from the Localize action.
        if (localize != null) {
            localize.removeAllOnStatusChangedListeners();
        }

        if (humanAwareness != null) {
            humanAwareness.removeAllOnHumansAroundChangedListeners();
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        pepperLog.appendLog(TAG, "GAINED FOCUS");
        setQiContext(qiContext);

        FutureUtils
                .wait(3, TimeUnit.SECONDS)
                .andThenConsume(ignore -> doMapping())
                .andThenConsume(ignore -> doHumans());

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
        pepperLog.appendLog(TAG, "LOST FOCUS");
        removeListeners();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    public void setAnimating(boolean state) {
        pepperLog.appendLog(TAG, String.format("Animating: %b", state));
        this.animating = state;
    }

    public void setHaveWavedLeft(boolean state) {
        this.haveWavedLeft = state;
    }
    public void setHaveWavedRight(boolean state) {
        this.haveWavedRight = state;
    }


    // Store the action execution future.
    private Future<Void> goToFuture;
    // Store the GoTo action.
    private GoTo goTo;

    public void searchHumans() {
        HumanAwareness humanAwareness = qiContext.getHumanAwareness();
        Future<List<Human>> humansAroundFuture = humanAwareness.async().getHumansAround();
        humansAroundFuture.andThenConsume(humans -> {
            // If humans found, follow the closest one.
            if (!humans.isEmpty()) {
                pepperLog.appendLog(TAG, "Human found.");
                Human humanToFollow = getClosestHuman(humans);
                followHuman(humanToFollow);
            } else {
                pepperLog.appendLog(TAG, "No human.");
                enterWaitingForOrderState();
            }
        });
    }

    private Human getClosestHuman(List<Human> humans) {
        // Get the robot frame.
        final Frame robotFrame = qiContext.getActuation().robotFrame();

        // Compare humans using the distance.
        Comparator<Human> comparator = (human1, human2) -> Double.compare(getDistance(robotFrame, human1), getDistance(robotFrame, human2));

        // Return the closest human.
        return Collections.min(humans, comparator);
    }

    private void followHuman(Human human) {
        // Create the target frame from the human.
        Frame targetFrame = createTargetFrame(human);

        // Create a GoTo action.
        goTo = GoToBuilder.with(qiContext)
                .withFrame(targetFrame)
                .build();

        goTo.addOnStartedListener(() -> {
            setAnimating(true);
            pepperLog.appendLog(TAG, "Follow started");
        });

        // Execute the GoTo action asynchronously.
        goToFuture = goTo.async().run();

        goToFuture.thenConsume(future -> {
           if (future.isSuccess()) {
               pepperLog.appendLog(TAG,"Follow done");
               this.facingNearHuman = true;
           } else if (future.hasError()) {
               pepperLog.appendLog(TAG, String.format("Follow error: %s", future.getErrorMessage()));
           } else if (future.isCancelled()) {
               pepperLog.appendLog(TAG, "Follow cancelled");
           }

           setAnimating(false);
        });
    }

    private double getDistance(Frame robotFrame, Human human) {
        // Get the human head frame.
        Frame humanFrame = human.getHeadFrame();
        // Retrieve the translation between the robot and the human.
        Vector3 translation = humanFrame.computeTransform(robotFrame).getTransform().getTranslation();
        // Get the translation coordinates.
        double x = translation.getX();
        double y = translation.getY();
        // Compute and return the distance.
        return Math.sqrt(x*x + y*y);
    }

    public void enterWaitingForOrderState() {
        pepperLog.appendLog(TAG, "Waiting for order...");
    }

    public void enterMovingState() {
        pepperLog.appendLog(TAG, "Moving...");
    }

    public void stopMoving() {
        // Cancel the GoTo action asynchronously.
        if (goToFuture != null) {
            goToFuture.requestCancellation();
        }
    }

    private Frame createTargetFrame(Human humanToFollow) {
        // Get the human head frame.
        Frame humanFrame = humanToFollow.getHeadFrame();
        // Create a transform for Pepper to stay at 1 meter in front of the human.
        Transform transform = TransformBuilder.create().fromXTranslation(1);
        // Create an AttachedFrame that automatically updates with the human frame.
        AttachedFrame attachedFrame = humanFrame.makeAttachedFrame(transform);
        // Returns the corresponding Frame.
        return attachedFrame.frame();
    }
}
