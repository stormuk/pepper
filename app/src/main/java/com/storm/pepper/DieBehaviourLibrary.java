package com.storm.pepper;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.storm.posh.BaseBehaviourLibrary;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.action.ActionEvent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DieBehaviourLibrary extends BaseBehaviourLibrary {
    private static final String TAG = DieBehaviourLibrary.class.getSimpleName();

    private boolean atStart;
    private boolean okToStart;
    private boolean atTable;
    private boolean humanReady;
    private boolean dieRolled;
    private double rollResult;


    @Override
    public void reset() {
        super.reset();

        atStart = false;
        atTable = false;
        okToStart = false;
        humanReady = false;
        dieRolled = false;
        rollResult = 0;
    }

    @Override
    public boolean getBooleanSense(Sense sense) {
        //        pepperLog.appendLog(TAG, String.format("Getting boolean sense: %s", sense));
        boolean senseValue;

        switch (sense.getNameOfElement()) {
            case "AtStart":
                senseValue = atStart;
                break;
            case "OkToStart":
                senseValue = okToStart;
                break;
            case "AtTable":
                senseValue = atTable;
                break;
            case "HumanReady":
                senseValue = humanReady;
                break;
            case "DieRolled":
                senseValue = dieRolled;
                break;

            default:
                senseValue = super.getBooleanSense(sense);
                break;
        }

        pepperLog.checkedBooleanSense(TAG, sense, senseValue);

        return senseValue;
    }

    @Override
    public double getDoubleSense(Sense sense) {
        double senseValue;

        switch(sense.getNameOfElement()) {
            case "RollResult":
                senseValue = rollResult;
                break;

            default:
                senseValue = super.getDoubleSense(sense);
                break;
        }

        pepperLog.checkedDoubleSense(TAG, sense, senseValue);

        return senseValue;
    }

    @Override
    public void executeAction(ActionEvent action) {
        pepperLog.appendLog(TAG, "Performing action: " + action);

        if (action.getNameOfElement() == currentAction) {
            // already performing this action
            pepperLog.appendLog(TAG, String.format("Action still in progress: %s", currentAction));
            return;
        }

        switch (action.getNameOfElement()) {
            case "GoToStart":
                goToStart();
                break;

            case "AskToStart":
                askToStart();
                break;

            case "ApproachTable":
                approachTable();
                break;

            case "CheckReady":
                checkReady();
                break;

            case "RollDie":
                rollDie();
                break;

            case "AskForResult":
                askForResult();
                break;

            default:
                super.executeAction(action);
                break;
        }
    }

    public void goToStart() {
        pepperLog.appendLog("Go To Start");
        goToLocation(0);
    }

    public void askToStart() {
        if (talking) {
            pepperLog.appendLog(TAG,"Cannot askToStart as already talking");
            return;
        } else if (listening) {
            pepperLog.appendLog(TAG, "Cannot askToStart as already listening");
            return;
        }

        setActive();
        this.talking = true;
        this.listening = true;

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Would you like to play a game?") // Set the text to say.
                    .build(); // Build the say action.
//                    .withBodyLanguageOption(BodyLanguageOption.DISABLED)

            say.run();

            this.talking = false;

            pepperLog.appendLog(TAG, "1");

            FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore2) -> {
                // TODO: Only init this once
                pepperLog.appendLog(TAG, "2");
                PhraseSet phraseSet = PhraseSetBuilder.with(qiContext).withTexts("Yes", "No", "Yes please", "No thank you").build();
                pepperLog.appendLog(TAG, "3");
                Listen listen = ListenBuilder.with(qiContext).withPhraseSet(phraseSet).build();

                listen.addOnStartedListener(() -> {
                    pepperLog.appendLog("Started listening...");
                    pepperLog.appendLog(TAG, "4");
                });

                this.listenFuture = listen.async().run();

                listenFuture.thenConsume(future -> {
                    this.listening = false;
                    pepperLog.appendLog(TAG, "5");
                    handleFuture(future, "listen_for_answer");

                    try {
                        ListenResult result = future.get();

                        Phrase heardPhrase = result.getHeardPhrase();

                        pepperLog.appendLog(TAG, String.format("Phrase was: %s", heardPhrase));

                        if (heardPhrase.getText().equals("Yes") || heardPhrase.getText().equals("Yes please")) {
                            this.okToStart = true;
                            pepperLog.appendLog(TAG, "Heard the OK!");
                        }

                        listenFuture.requestCancellation();

                    } catch (ExecutionException e) {
                        pepperLog.appendLog(TAG, "Error occurred when listening for answer");
                    } catch (CancellationException e) {
                        pepperLog.appendLog(TAG, "Listening for answer was cancelled");
                    }
                });
            });
        });
    }

    public void approachTable() {
        pepperLog.appendLog("APPROACH TABLE");
        goToLocation(1);
    }

    @Override
    protected void locationReached(int id) {
        pepperLog.appendLog(TAG, String.format("Reached: %d", id));
        if (id == 0) {
            reachedStart();
        } else if (id == 1) {
            reachedTable();
        } else {
            pepperLog.appendLog(TAG, "Unknown location reached");
        }
    }

    private void reachedStart() {
        this.atStart = true;
        pepperLog.appendLog(TAG, String.format("Reached: Start"));
    }

    private void reachedTable() {
        this.atTable = true;
        pepperLog.appendLog(TAG, String.format("Reached: Table"));
        lookAtHuman();
    }

    public void checkReady() {
        pepperLog.appendLog("CHECK READY");
        if (talking) {
            pepperLog.appendLog(TAG,"Cannot checkReady as already talking");
            return;
        } else if (listening) {
            pepperLog.appendLog(TAG, "Cannot checkReady as already listening");
            return;
        }

        setActive();

        this.talking = true;
        this.listening = true;

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Are you ready to continue?") // Set the text to say.
                    .build(); // Build the say action.
//                    .withBodyLanguageOption(BodyLanguageOption.DISABLED)

            say.run();

            this.talking = false;

            FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore2) -> {
                // TODO: Only init this once
                PhraseSet phraseSet = PhraseSetBuilder.with(qiContext).withTexts("Yes", "No").build();
                Listen listen = ListenBuilder.with(qiContext).withPhraseSet(phraseSet).build();

                this.listenFuture = listen.async().run();

                listenFuture.thenConsume(future -> {
                    this.listening = false;

                    try {
                        ListenResult result = future.get();

                        Phrase heardPhrase = result.getHeardPhrase();

                        if (heardPhrase.getText().equals("Yes")) {
                            this.humanReady = true;
                        }

                        listenFuture.requestCancellation();

                    } catch (ExecutionException e) {
                        pepperLog.appendLog(TAG, "Error occurred when listening for answer");
                    } catch (CancellationException e) {
                        pepperLog.appendLog(TAG, "Listening for answer was cancelled");
                    }
                });
            });
        });
    }

    public void rollDie() {
        pepperLog.appendLog("ROLL DIE");

        setAnimating(true);

        // Create an animation object.
        Future<Animation> myAnimationFuture = AnimationBuilder.with(qiContext)
                .withResources(R.raw.roll)
                .buildAsync();

        myAnimationFuture.andThenConsume(myAnimation -> {
            Animate animate = AnimateBuilder.with(qiContext)
                    .withAnimation(myAnimation)
                    .build();

            // Run the action synchronously in this thread
            animate.run();

            pepperLog.appendLog(TAG, "HAVE ROLLED");
            this.dieRolled = true;
            setAnimating(false);
        });

    }

    public void askForResult() {
        pepperLog.appendLog("ASK FOR RESULT");
        if (talking) {
            pepperLog.appendLog(TAG,"Cannot askForResult as already talking");
            return;
        } else if (listening) {
            pepperLog.appendLog(TAG, "Cannot askForResult as already listening");
            return;
        }

        setActive();
        this.talking = true;
        this.listening = true;

        if (lookAtFuture != null) {
            pepperLog.appendLog(TAG, "Stop looking");
            lookAtFuture.requestCancellation();
        }

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("What is the number on the dice?") // Set the text to say.
                    .build(); // Build the say action.
//                    .withBodyLanguageOption(BodyLanguageOption.DISABLED)

            say.run();

            // Create a topic.
            Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.roll_result) // Set the topic resource.
                .build(); // Build the topic.

            // Create a new QiChatbot.
            QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

            QiChatVariable chatRollResult = qiChatbot.variable("rollResult");

            chatRollResult.addOnValueChangedListener(currentValue -> {
                Log.i(TAG, "chatRollResult: " + String.valueOf(currentValue));
                this.rollResult = Double.valueOf(currentValue);
                this.reset();
            });


            // Create a new Chat action.
            chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

            // Add an on started listener to the Chat action.
            chat.addOnStartedListener(() -> Log.d(TAG, "Chat started."));

            // Run the Chat action asynchronously.
            Future<Void> chatFuture = chat.async().run();

            // Stop the chat when done
            qiChatbot.addOnEndedListener(endReason -> {
                pepperLog.appendLog(TAG, String.format("Chat ended: %s", endReason));
                chatFuture.requestCancellation();
            });

            // Add a lambda to the action execution.
            chatFuture.thenConsume(future -> {
                pepperLog.appendLog(TAG, "Chat completed?");
                this.talking = false;
                this.listening = false;
                if (future.hasError()) {
                    Log.d(TAG, "Discussion finished with error.", future.getError());
                }
            });
        });
    }
}
