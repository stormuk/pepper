package com.storm.pepper;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.storm.posh.BaseBehaviourLibrary;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.Sense;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class AnnoyBehaviourLibrary extends BaseBehaviourLibrary {
    private static final String TAG = AnnoyBehaviourLibrary.class.getSimpleName();

    private boolean doNotAnnoy = false;
    private boolean heardStop = false;
    private boolean haveWavedLeft = false;
    private boolean haveWavedRight = false;

    private String[] humPhrases = {"hmmm", "humm", "doo-be doo", "hum", "What was that?", "Who's there?", "Tee hee"};
    private Date nextHumTime;


    private Future<ListenResult> listenFuture;

    public AnnoyBehaviourLibrary() {
        setInstance();
    }

    public void reset() {
        super.reset();

        haveWavedLeft = false;
        haveWavedRight = false;
        doNotAnnoy = false;
        heardStop = false;
    }

    public boolean getBooleanSense(Sense sense) {
//        pepperLog.appendLog(TAG, String.format("Getting boolean sense: %s", sense));
        boolean senseValue;

        switch (sense.getNameOfElement()) {
            case "HaveWavedLeft":
                senseValue = haveWavedLeft;
                break;
            case "HaveWavedRight":
                senseValue = haveWavedRight;
                break;
            case "HeardStop":
                senseValue = heardStop;
                break;
            case "DoNotAnnoy":
                senseValue = doNotAnnoy;
                break;

            default:
                senseValue = super.getBooleanSense(sense);
                break;
        }

        pepperLog.checkedBooleanSense(TAG, sense, senseValue);

        return senseValue;
    }

    public double getDoubleSense(Sense sense) {
        return super.getDoubleSense(sense);
    }

    public void executeAction(ActionEvent action) {
        pepperLog.appendLog(TAG, "Performing action: " + action);

        switch (action.getNameOfElement()) {
            case "WaveLeft":
                waveLeft();
                break;

            case "WaveRight":
                waveRight();
                break;

            case "ClearWaving":
                clearWaving();
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

            case "ForgetAnnoy":
                this.doNotAnnoy = false;
                break;

            case "Hum":
                hum();
                break;

            default:
                super.executeAction(action);
                break;
        }
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
        int humDelay = ThreadLocalRandom.current().nextInt(8, 15);
        pepperLog.appendLog(TAG, String.format("Next hum in %d seconds", humDelay));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, humDelay);
        nextHumTime = calendar.getTime();

        // choose a phrase to hum
        int index = ThreadLocalRandom.current().nextInt(humPhrases.length);
        System.out.println("\nIndex :" + index);
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

            this.listenFuture = listen.async().run();

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
                        listenFuture.requestCancellation();
                        pepperLog.appendLog(TAG, "Heard Stop");
                    }

                } catch (ExecutionException e) {
                    pepperLog.appendLog(TAG, "Error occurred when listening for stop");
                } catch (CancellationException e) {
                    pepperLog.appendLog(TAG, "Listening for stop was cancelled");
                }
            });
        });
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
        super.removeListeners();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        super.onRobotFocusGained(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        super.onRobotFocusLost();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
        super.onRobotFocusRefused(reason);
    }

    public void setHaveWavedLeft(boolean state) {
        this.haveWavedLeft = state;
    }

    public void setHaveWavedRight(boolean state) {
        this.haveWavedRight = state;
    }


}
