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
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BehaviourLibrary implements RobotLifecycleCallbacks {
    private static final String TAG = BehaviourLibrary.class.getSimpleName();

    private static BehaviourLibrary instance = null;

    private PepperLog pepperLog;

    private QiContext qiContext;
    private MainActivity activity;

    private boolean batteryLow = false;
    private boolean batteryCharging = false;

    private boolean talking = false;
    private boolean listening = false;
    private boolean animating = false;

    private boolean humanPresent = false;
    private boolean humanEngaged = false;
    private boolean facingNearHuman = false;
    private boolean doNotAnnoy = false;

    private boolean heardStop = false;

    private boolean haveWavedLeft = false;
    private boolean haveWavedRight = false;

    private boolean safeToMap = true;
    private boolean mappingInProgress = false;
    private boolean mappingComplete = false;

    // Store the HumanAwareness service.
    private HumanAwareness humanAwareness;

    // Store the EngageHuman action.
    private EngageHuman engageHuman;

    private Human recommendedHumanToEngage;

    // Store the LocalizeAndMap action.
    private LocalizeAndMap localizeAndMap;
    // Store the map.
    private ExplorationMap explorationMap;
    // Store the LocalizeAndMap execution.
    private Future<Void> localizationAndMapping;
    // Store the Localize action.
    private Localize localize;
    private Future<Void> localization;

    private Date lastActive;
    private Date nextHumTime;

    private int batteryPercent = 75;

    private String[] humPhrases = {"hmmm", "humm", "doo-be doo", "hum", "What was that?", "Who's there?", "Tee hee"};


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
        lastActive = new Date();

        haveWavedLeft = false;
        haveWavedRight = false;
        batteryLow = false;
        batteryCharging = false;
        talking = false;
        listening = false;
        animating = false;
        humanPresent = false;
        humanEngaged = false;
        facingNearHuman = false;
        doNotAnnoy = false;
        heardStop = false;
        haveWavedLeft = false;
        haveWavedRight = false;
        safeToMap = true;
        mappingInProgress = false;
        mappingComplete = false;
    }

    public boolean getBooleanSense(Sense sense) {
//        pepperLog.appendLog(TAG, String.format("Getting boolean sense: %s", sense));
        boolean senseValue;

        switch(sense.getNameOfElement()) {
            case "BatteryLow":
                senseValue = isBatteryLow();
                break;
            case "HaveWavedLeft":
                senseValue = haveWavedLeft;
                break;
            case "HaveWavedRight":
                senseValue = haveWavedRight;
                break;
            case "Talking":
                senseValue = talking;
                break;
            case "Listening":
                senseValue = listening;
                break;
            case "Animating":
                senseValue = animating;
                break;
            case "HumanPresent":
                senseValue = humanPresent;
                break;
            case "HumanEngaged":
                senseValue = humanEngaged;
                break;
            case "FacingNearHuman":
                senseValue = facingNearHuman;
                break;
            case "HeardStop":
                senseValue = heardStop;
                break;
            case "MappingComplete":
                senseValue = mappingComplete;
                break;
            case "DoNotAnnoy":
                senseValue = doNotAnnoy;
                break;
            case "SafeToMap":
                senseValue = safeToMap;
                break;

            default:
                senseValue = false;
                break;
        }

        pepperLog.checkedBooleanSense(TAG, sense, senseValue);

        return senseValue;
    }
    public double getDoubleSense(Sense sense) {
        double senseValue;

        switch(sense.getNameOfElement()) {
            case "IdleTime":
                senseValue = getIdleTime();
                break;
            case "BatteryPercent":
                senseValue = getBatteryPercent();
                break;

            default:
                senseValue = 0;
                break;
        }

        pepperLog.checkedDoubleSense(TAG, sense, senseValue);

        return senseValue;
    }

    private void setActive() {
        this.lastActive = new Date();
    }

    public double getIdleTime() {
        Date now = new Date();

        if (lastActive == null) {
            lastActive = now;
        }

        int idleTime = (int)((now.getTime() - lastActive.getTime()) / 1000);

        return Double.valueOf(idleTime);
    }

    private double getBatteryPercent() {
        return Double.valueOf(this.batteryPercent);
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

            case "DoExplore":
                doExplore();
                break;

            case "ClearWaving":
                clearWaving();
                break;

            case "PromptForBatteryCharge":
                promptForBatteryCharge();
                break;

            case "DismissHumans":
                dismissHumans();
                break;

            case "ApproachHuman":
                approachHuman();
                break;

            case "ListenForStop":
                listenForStop();
                break;

            case "DoNotAnnoy":
                this.doNotAnnoy = true;
                break;

            case "DoMapping":
                doMapping();
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
        } else {
            setActive();
        }

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("My battery is low, please plug me in") // Set the text to say.
                    .build(); // Build the say action.

            this.talking = true;

            // Execute the action.
            say.run();

            this.talking = false;
        });
    }


    public void dismissHumans() {
        if (talking) {
            pepperLog.appendLog(TAG, "Cannot dismiss humans, already talking");
            return;
        } else {
            setActive();
        }

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("I need the room to myself for a few minutes, please leave") // Set the text to say.
                    .build(); // Build the say action.

            this.talking = true;

            // Execute the action.
            say.run();

            this.talking = false;
        });
    }

    public void listenForStop() {
        pepperLog.appendLog(TAG, "Listen for stop?");
        if (listening) {
            pepperLog.appendLog(TAG, "Already listening");
            return;
        } else if (heardStop) {
            pepperLog.appendLog(TAG, "Already heard stop");
            return;
        } else {
            setActive();
        }

        pepperLog.appendLog(TAG, "1");

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            // TODO: Only init this once
            pepperLog.appendLog(TAG, "2");
            PhraseSet phraseSet = PhraseSetBuilder.with(qiContext).withTexts("Stop", "Go Away", "No").build();
            pepperLog.appendLog(TAG, "3");
            Listen listen = ListenBuilder.with(qiContext).withPhraseSet(phraseSet).build();

            listen.addOnStartedListener(() -> {
                this.listening = true;
                pepperLog.appendLog("Started listening...");
                pepperLog.appendLog(TAG, "4");
            });

            Future<ListenResult> listenFuture = listen.async().run();

            listenFuture.thenConsume(future -> {
                this.listening = false;
                pepperLog.appendLog(TAG, "5");
                handleFuture(future, "listen_for_stop");

                try {
                    ListenResult result = future.get();

                    PhraseSet heardPhraseSet = result.getMatchedPhraseSet();
                    Phrase heardPhrase = result.getHeardPhrase();

                    pepperLog.appendLog(TAG, String.format("Phrase was: %s", heardPhrase));

                    if (heardPhrase.getText().equals("Stop")) {
                        this.heardStop = true;
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
        });
    }

    private void handleFuture(Future future, String label) {
        pepperLog.appendLog(TAG, "Handling future");
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
        } else {
            setActive();
        }

//        humanAwareness = qiContext.getHumanAwareness();
//        Human recommendedHuman = humanAwareness.getRecommendedHumanToEngage();

        if (recommendedHumanToEngage != null) {
            pepperLog.appendLog(TAG, "Approaching human...");
            pepperLog.appendLog(TAG, String.format("Recommended human: %s", recommendedHumanToEngage));

            followHuman(recommendedHumanToEngage);

        } else {
            pepperLog.appendLog(TAG, "No recommended human");
        }
    }

    public void doExplore() {
        if (animating) {
            pepperLog.appendLog(TAG, "Already animating, cannot explore");
        } else {
            pepperLog.appendLog(TAG, "Exploring");

            FutureUtils.wait(5, TimeUnit.SECONDS).andThenConsume(ignore -> {
                pepperLog.appendLog(TAG, "Done exploring");
               this.animating = false;
            });
        }
    }

    public void doHumans() {
        pepperLog.appendLog(TAG, "Initialising human tracking");
        this.humanPresent = false;
        humanAwareness = qiContext.getHumanAwareness();

        humanAwareness.addOnHumansAroundChangedListener(this::updateHumansAround);
        humanAwareness.addOnRecommendedHumanToEngageChangedListener(this::updateHumanToEngage);
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

    private void updateHumanToEngage(Human human) {
        recommendedHumanToEngage = human;
        if (human != null) {
//            pepperLog.appendLog(TAG, "Updating humanToEngage - none!");
            this.humanEngaged = false;
        } else {
//            pepperLog.appendLog(TAG, "Updating humanToEngage - found!");
            this.humanEngaged = true;
        }
    }

    public void doMapping() {
        if (mappingComplete) {
            pepperLog.appendLog(TAG, "Mapping already complete");
            return;
        } else if (mappingInProgress) {
            pepperLog.appendLog(TAG, "Mapping in progress");
            setActive();
            return;
        } else if (animating) {
            pepperLog.appendLog(TAG, "Cannot map, already animating");
        } else {
            setActive();
        }

        // Create a LocalizeAndMap action.
        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build();

            // Add an on status changed listener on the LocalizeAndMap action for the robot to say when he is localized.
            localizeAndMap.addOnStatusChangedListener(status -> {
                switch (status) {
                    case LOCALIZED:
                        // Dump the ExplorationMap.
                        explorationMap = localizeAndMap.dumpMap();

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
            this.mappingInProgress = true;

            setAnimating(true);

            pepperLog.appendLog(TAG, "Mapping started");

            // Execute the LocalizeAndMap action asynchronously.
            localizationAndMapping = localizeAndMap.async().run();

            // Add a lambda to the action execution.
            localizationAndMapping.thenConsume(future -> {
                if (future.hasError()) {
                    String errorMessage = "LocalizeAndMap action finished with error.";
                    pepperLog.appendLog(TAG, String.format("%s: %s", errorMessage, future.getError()));
                    this.mappingInProgress = false;
                } else if (future.isCancelled()) {
                    // cancelled means we're done mapping the environment and can use it
                    startLocalizing(qiContext);
                }

                setAnimating(false);
            });
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
                    this.mappingInProgress = false;
                    this.mappingComplete = true;

                    Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                            .withText("I've finished mapping, you can come back in now!") // Set the text to say.
                            .build(); // Build the say action.

                    this.talking = true;

                    // Execute the action.
                    say.run();

                    this.talking = false;

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

            this.mappingInProgress = false;
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
        this.mappingInProgress = false;
    }

    public void hum() {
        Date now = new Date();
        if (talking) {
            pepperLog.appendLog(TAG, "Already talking, can't hum now");
            return;
        } else if (nextHumTime != null && now.before(nextHumTime)) {
            pepperLog.appendLog(TAG, "Don't want to hum yet");
            return;
        } else {
            pepperLog.appendLog(TAG, "Bored, gonna hum!");
        }

        // set next time to hum
        int humDelay = ThreadLocalRandom.current().nextInt(8,15);
        pepperLog.appendLog(TAG, String.format("Next hum in %d seconds", humDelay));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, humDelay);
        nextHumTime = calendar.getTime();

        // choose a phrase to hum
        int index = ThreadLocalRandom.current().nextInt(humPhrases.length);
        System.out.println("\nIndex :" + index );
        String humPhrase = humPhrases[index];

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            this.talking = true;
            Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText(humPhrase) // Set the text to say.
                    .withBodyLanguageOption(BodyLanguageOption.DISABLED)
                    .build(); // Build the say action.

            say.run();

            this.talking = false;
        });



//        say.async().addOnStartedListener(() -> {
//           this.talking = true;
//           pepperLog.appendLog(TAG, "Starting 1111 humming?");
//        });
//
//        say.addOnStartedListener(() -> {
//            this.talking = true;
//            pepperLog.appendLog(TAG, "Starting 2222 humming");
//        });
//
//        // Execute the action.
//        Future<Void> sayFuture = say.async().run();
//
//        sayFuture.andThenConsume(future -> {
//            this.talking = false;
//            pepperLog.appendLog(TAG, "Done?");
////            handleFuture(future, "hum");
//        });
    }

    public void waveLeft() {
        setActive();

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
        setActive();

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

    public void clearWaving() {
        setHaveWavedLeft(false);
        setHaveWavedRight(false);
        pepperLog.appendLog(TAG, "WAVING CLEARED");
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
            humanAwareness.removeAllOnEngagedHumanChangedListeners();
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        pepperLog.appendLog(TAG, "GAINED FOCUS");
        setQiContext(qiContext);

        FutureUtils
                .wait(0, TimeUnit.SECONDS)
                .andThenConsume(ignore -> doHumans());
//                .andThenConsume(ignore -> doMapping())

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

        pepperLog.appendLog(TAG, "Follow human");

        FutureUtils
            .wait(0, TimeUnit.SECONDS)
            .andThenConsume(ignore -> {
                pepperLog.appendLog(TAG, "Follow start?");
                // Create a GoTo action.
                goTo = GoToBuilder.with(qiContext)
                        .withFrame(targetFrame)
                        .build();

                setAnimating(true);
                pepperLog.appendLog(TAG, "Follow started");

//                goTo.addOnStartedListener(() -> {
//                });

                goTo.run();

                pepperLog.appendLog(TAG, "DONE FOLLOWING");
                setAnimating(false);

//                // Execute the GoTo action asynchronously.
//                goToFuture = goTo.async().run();
//
//                goToFuture.thenConsume(future -> {
//                    if (future.isSuccess()) {
//                        pepperLog.appendLog(TAG,"Follow done");
//                        this.facingNearHuman = true;
//                    } else if (future.hasError()) {
//                        pepperLog.appendLog(TAG, String.format("Follow error: %s", future.getErrorMessage()));
//                    } else if (future.isCancelled()) {
//                        pepperLog.appendLog(TAG, "Follow cancelled");
//                    }
//
//                    setAnimating(false);
//                });
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

    public boolean isBatteryLow() {
        return (batteryLow && !isBatteryCharging());
    }

    public void setBatteryLow(boolean batteryLow) {
        this.batteryLow = batteryLow;
    }

    public boolean isBatteryCharging() {
        return batteryCharging;
    }

    public void setBatteryCharging(boolean batteryCharging) {
        this.batteryCharging = batteryCharging;
    }

    public boolean isHumanPresent() {
        return humanPresent;
    }
    public boolean isHumanEngaged() {
        return humanEngaged;
    }
    public boolean isMappingComplete() {
        return mappingComplete;
    }
    public boolean isMappingInProgress() {
        return mappingInProgress;
    }
}
