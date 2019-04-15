package com.storm.posh;

import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.builder.LookAtBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.AttachedFrame;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.object.actuation.LookAtMovementPolicy;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.EngageHuman;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.storm.pepper.MainActivity;
import com.storm.pepper.PepperLog;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.Sense;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BaseBehaviourLibrary implements BehaviourLibrary, RobotLifecycleCallbacks {
    private static final String TAG = BaseBehaviourLibrary.class.getSimpleName();

    private static BaseBehaviourLibrary instance = null;

    protected PepperLog pepperLog;

    protected QiContext qiContext;
    protected MainActivity activity;

    protected String currentAction;

    protected boolean batteryLow = false;
    protected boolean batteryCharging = false;

    protected boolean talking = false;
    protected boolean listening = false;
    protected boolean animating = false;

    protected List<Human> humans;
    protected boolean humanPresent = false;
    protected boolean humanEngaged = false;
    protected boolean facingNearHuman = false;

    protected boolean safeToMap = true;
    protected boolean mappingInProgress = false;
    protected boolean mappingComplete = false;

    // Store the saved locations.
    private Map<Integer, FreeFrame> savedLocations = new HashMap<>();

    // Store the HumanAwareness service.
    protected HumanAwareness humanAwareness;

    // Store the EngageHuman action.
    protected EngageHuman engageHuman;

    protected Human recommendedHumanToEngage;


    // A boolean used to store the abilities status.
    private boolean abilitiesHeld = false;
    // The holder for the abilities.
    private Holder holder;
    // Store the GoTo action.
    private GoTo goTo;
    // Store the Actuation service.
    protected Actuation actuation;
    // Store the Mapping service.
    protected Mapping mapping;
    // Store the LocalizeAndMap action.
    protected LocalizeAndMap localizeAndMap;
    // Store the map.
    protected ExplorationMap explorationMap;
    // Store the LocalizeAndMap execution.
    protected Future<Void> localizationAndMapping;
    // Store the Localize action.
    protected Localize localize;
    protected Future<Void> localization;
    // Store the LookAt action
    protected Future<Void> lookAtFuture;

    protected Chat chat;

    protected Date lastActive;

    protected int batteryPercent = 75;

    protected Future<ListenResult> listenFuture;

    public BaseBehaviourLibrary() {
        setInstance();
    }

    protected void setInstance() {
        if (instance == null) {
            instance = this;
        } else {
            Log.d(TAG, String.format("Behaviour library singleton already initialised as a %s", instance.getClass().getSimpleName()));
        }
    }

    public static BaseBehaviourLibrary getInstance() {
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
;
        batteryLow = false;
        batteryCharging = false;
        talking = false;
        listening = false;
        animating = false;
        facingNearHuman = false;
        safeToMap = true;

        // Don't reset these
//        humanPresent = false;
//        humanEngaged = false;
    }

    public boolean getBooleanSense(Sense sense) {
//        pepperLog.appendLog(TAG, String.format("Getting boolean sense: %s", sense));
        boolean senseValue;

        switch(sense.getNameOfElement()) {
            case "BatteryLow":
                senseValue = isBatteryLow();
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
            case "HumanClose":
                senseValue = isHumanClose();
            case "HumanEngaged":
                senseValue = humanEngaged;
                break;
            case "FacingNearHuman":
                senseValue = facingNearHuman;
                break;
            case "MappingComplete":
                senseValue = mappingComplete;
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

    protected void setActive() {
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
            case "DoExplore":
                doExplore();
                break;

            case "PromptForBatteryCharge":
                promptForBatteryCharge();
                break;

            case "DismissHumans":
                dismissHumans();
                break;

            case "StopListening":
                stopListening();
                break;

            default:
                Log.d(TAG, "UNKNOWN ACTION");
                break;
        }
    }

    public void saveLocation() {
        // Get the robot frame asynchronously.
        Future<Frame> robotFrameFuture = actuation.async().robotFrame();
        robotFrameFuture.andThenConsume(robotFrame -> {
            // Create a FreeFrame representing the current robot frame.
            FreeFrame locationFrame = mapping.makeFreeFrame();
            Transform transform = TransformBuilder.create().fromXTranslation(0);
            locationFrame.update(robotFrame, transform, 0L);

            // Store the FreeFrame in the next slot.
            // A Map is used here so we can easily switch to named slots in future
            // but for now they have to be added in the correct order the behaviour library needs.
            savedLocations.put(savedLocations.size(), locationFrame);
            activity.updateLocationsCount(savedLocations.size());
        });
    }

    public void clearLocations() {
        savedLocations.clear();
        activity.updateLocationsCount(savedLocations.size());
    }

    protected void locationReached(int id) {
        // placeholder for other behaviour libraries to extend
    }

    public void goToLocation(int id) {
        if (!savedLocations.containsKey(id)) {
            pepperLog.appendLog(TAG, String.format("No location saved with id: %d", id));
            return;
        } else if (animating) {
            pepperLog.appendLog(TAG, String.format("Already animating, cannot go to location with id: %d", id));
            return;
        }

        setActive();
        this.animating = true;

        // Extract the Frame asynchronously.
        Future<Frame> frameFuture = savedLocations.get(id).async().frame();
        frameFuture.andThenCompose(frame -> {
            // Create a GoTo action.
            goTo = GoToBuilder.with(qiContext)
                    .withFrame(frame)
                    .build();

            // Display text when the GoTo action starts.
            goTo.addOnStartedListener(() -> pepperLog.appendLog(TAG, "Moving..."));

            // Execute the GoTo action asynchronously.
            return goTo.async().run();
        }).thenConsume(future -> {
            this.animating = false;

            if (future.isSuccess()) {
                pepperLog.appendLog(TAG, String.format("Location %d reached", id));
                locationReached(id);

            } else if (future.hasError()) {
                pepperLog.appendLog(TAG, String.format("Go to location error", future.getError()));
            }

        });
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
            say.async().run();

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

    public void stopListening() {
        if (!listening || listenFuture == null || listenFuture.isDone()) {
            pepperLog.appendLog(TAG, "Not listening, can't stop");
            return;
        }
        pepperLog.appendLog(TAG, "Requesting cancellation");
        listenFuture.requestCancellation();
        pepperLog.appendLog(TAG, "Cancellation requested!");
    }

    protected void handleFuture(Future future, String label) {
        pepperLog.appendLog(TAG, "Handling future");
        if (future.isSuccess()) {
            pepperLog.appendLog(TAG,String.format("Future %s done", label));
        } else if (future.hasError()) {
            pepperLog.appendLog(TAG, String.format("Future %s error: %s", label, future.getErrorMessage()));
        } else if (future.isCancelled()) {
            pepperLog.appendLog(TAG, String.format("Future %s cancelled", label));
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

        if (humanAwareness != null) {
            humanAwareness.removeAllOnHumansAroundChangedListeners();
            humanAwareness.removeAllOnEngagedHumanChangedListeners();
        }

        this.humanPresent = false;
        humanAwareness = qiContext.getHumanAwareness();

        humanAwareness.addOnHumansAroundChangedListener(this::updateHumansAround);
        humanAwareness.addOnRecommendedHumanToEngageChangedListener(this::updateHumanToEngage);
    }

    private void updateHumansAround(List<Human> humansAround) {
        this.humans = humansAround;

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
            pepperLog.appendLog(TAG, "Updating humanToEngage - none!");
            this.humanEngaged = false;
        } else {
            pepperLog.appendLog(TAG, "Updating humanToEngage - found!");
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
                            .withText("I've finished mapping. You can come back now.") // Set the text to say.
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

    public void holdAwareness() {
        // Build and store the holder for the abilities.
        holder = HolderBuilder.with(qiContext)
            .withAutonomousAbilities(AutonomousAbilitiesType.BASIC_AWARENESS)
            .build();

        // Hold the abilities asynchronously.
        Future<Void> holdFuture = holder.async().hold();

        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            // Store the abilities status.
            abilitiesHeld = true;
        }));
    }

    public void releaseAwareness() {
        // Release the holder asynchronously.
        Future<Void> releaseFuture = holder.async().release();

        // Chain the release with a lambda on the UI thread.
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            // Store the abilities status.
            abilitiesHeld = false;
        }));
    }

    // tidy up listeners
    public void removeListeners() {
        // Remove on started listeners from the GoTo action.
        if (goTo != null) {
            goTo.removeAllOnStartedListeners();
        }

        if (chat != null) {
            chat.removeAllOnStartedListeners();
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
        actuation = qiContext.getActuation();
        mapping = qiContext.getMapping();

        FutureUtils
            .wait(0, TimeUnit.SECONDS)
            .andThenConsume(ignore -> doHumans())
            .andThenConsume(ignore -> doMapping());
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
        pepperLog.appendLog(TAG, String.format("Robot focus refused: %s", reason));
    }

    public void setAnimating(boolean state) {
        pepperLog.appendLog(TAG, String.format("Animating: %b", state));
        this.animating = state;
    }


    // Store the action execution future.
    private Future<Void> goToFuture;

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

    protected void lookAtHuman() {
        lookAtHuman(LookAtMovementPolicy.HEAD_AND_BASE);
    }
    protected void lookAtHuman(LookAtMovementPolicy policy) {
        if (animating) {
            pepperLog.appendLog(TAG, "Already animating. Cannot look at human");
            return;
        }

        Human targetHuman = null;
        if (recommendedHumanToEngage != null) {
            targetHuman = recommendedHumanToEngage;
        } else if (humans.size() > 0) {
            targetHuman = getClosestHuman(this.humans);
        }

        if (targetHuman == null) {
            pepperLog.appendLog(TAG, "No human to look at");
            return;
        }

        setAnimating(true);

        Frame targetFrame = createTargetFrame(targetHuman);

        LookAt lookAt = LookAtBuilder.with(qiContext)
            .withFrame(targetFrame)
            .build();

        lookAt.setPolicy(policy);

        lookAtFuture = lookAt.async().run();

        lookAtFuture.andThenConsume(ignore -> {
            pepperLog.appendLog(TAG, "Done looking");
            setAnimating(false);
        });
    }

    private boolean isHumanClose() {
        pepperLog.appendLog(TAG, "Is human close? Always yes");
        return true;
//        if (humans.isEmpty()) {
//            pepperLog.appendLog(TAG, "No humans, no close");
//            return false;
//        }
//
//        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume(ignore -> {
//            pepperLog.appendLog(TAG, "STEP 1");
//            // Get the robot frame.
//            Frame robotFrame = qiContext.getActuation().robotFrame();
//
//            pepperLog.appendLog(TAG, "STEP 2");
//            Human closestHuman = getClosestHuman(humans);
//
//            pepperLog.appendLog(TAG, "STEP 3");
//            double distance = getDistance(robotFrame, closestHuman);
//            pepperLog.appendLog(TAG, String.format("Human distance is %f", distance));
//
//            pepperLog.appendLog(TAG, "STEP 4");
//        });
//        return false;
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

    private void followHuman(Human human) {
        pepperLog.appendLog(TAG, "Follow human?");
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
        pepperLog.appendLog(TAG, "Create target frame 1?");
        // Get the human head frame.
        Frame humanFrame = humanToFollow.getHeadFrame();
        pepperLog.appendLog(TAG, "Create target frame 2?");
        // Create a transform for Pepper to stay at 1 meter in front of the human.
        Transform transform = TransformBuilder.create().fromXTranslation(0.9);
        pepperLog.appendLog(TAG, "Create target frame 3?");
        // Create an AttachedFrame that automatically updates with the human frame.
        AttachedFrame attachedFrame = humanFrame.makeAttachedFrame(transform);
        pepperLog.appendLog(TAG, "Create target frame 4?");
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
