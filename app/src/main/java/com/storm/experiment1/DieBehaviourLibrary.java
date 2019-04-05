package com.storm.experiment1;

import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.storm.posh.BaseBehaviourLibrary;
import com.storm.posh.BehaviourLibrary;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.action.ActionEvent;

import java.util.concurrent.TimeUnit;

public class DieBehaviourLibrary extends BaseBehaviourLibrary {
    private static final String TAG = DieBehaviourLibrary.class.getSimpleName();

    private boolean readyToStart;
    private boolean dieRolled;
    private double rollResult;


    @Override
    public void reset() {
        super.reset();

        readyToStart = false;
        dieRolled = false;
        rollResult = 0;
    }

    @Override
    public boolean getBooleanSense(Sense sense) {
        //        pepperLog.appendLog(TAG, String.format("Getting boolean sense: %s", sense));
        boolean senseValue;

        switch (sense.getNameOfElement()) {
            case "ReadyToStart":
                senseValue = readyToStart;
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

    public void askToStart() {
        if (talking) {
            pepperLog.appendLog(TAG,"Cannot askToStart as already talking");
            return;
        }

        setActive();

        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume((ignore) -> {
            this.talking = true;
            Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Would you like to play a game?") // Set the text to say.
                    .build(); // Build the say action.
//                    .withBodyLanguageOption(BodyLanguageOption.DISABLED)

            say.run();

            this.talking = false;
        });
    }

    public void approachTable() {

    }

    public void checkReady() {

    }

    public void rollDie() {

    }

    public void askForResult() {

    }
}
