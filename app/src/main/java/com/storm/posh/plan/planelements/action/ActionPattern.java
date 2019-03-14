package com.storm.posh.plan.planelements.action;

import android.util.Log;

import com.storm.posh.Planner;
import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.ElementWithTrigger;
import com.storm.posh.plan.planelements.TimeUnits;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: Andreas
 * Date: 18/01/2016.
 */
public class ActionPattern extends ElementWithTrigger {

    private static final String TAG = ActionPattern.class.getSimpleName();

    private double timeValue;

    private TimeUnits timeUnits = TimeUnits.SECONDS;

    private List<ActionPatternElement> actionPatternElements = new LinkedList<>();

    private List<ActionEvent> actionEvents = new LinkedList<>();

    public ActionPattern(String nameOfElement) {
        super(nameOfElement);
    }

    public ActionPattern(String name,List<ActionEvent> actions) {
        super(name);

        Log.d(TAG, String.format("Creating %s with %d actions", name, actions.size()));

        this.actionEvents = actions;
    }

    public ActionPattern(String name, double timeValue, TimeUnits timeUnits, List<ActionEvent> actions) {
        super(name);

        Log.d(TAG, String.format("Creating %s with %d actions (and time)", name, actions.size()));

        this.timeValue = timeValue;
        this.timeUnits = timeUnits;
        this.actionEvents = actions;
    }

    public void addAction(ActionEvent actionEvent) {
        Log.d(TAG, "Adding action "+actionEvent.getNameOfElement());
        if (Plan.getInstance().getActionEvents().contains(actionEvent))
            actionEvents.add(actionEvent);
        else {
            Plan.getInstance().getActionEvents().add(actionEvent);
            actionEvents.add(actionEvent);
        }
    }

    public void addActionPatternElement(ActionPatternElement actionPatternElement) {
        actionPatternElements.add(actionPatternElement);
    }

    public List<ActionEvent> getActionEvents() {
        return actionEvents;
    }

    public List<ActionPatternElement> getActionPatternElements() {
        return actionPatternElements;
    }

    public void setActions(List<ActionEvent> actions) {
        this.actionEvents = actions;
    }

    public void setTimeValue(double timeValue) {
        this.timeValue = timeValue;
    }

    public void setTimeUnits(TimeUnits timeUnits) {
        this.timeUnits = timeUnits;
    }

    public double getTimeValue() {
        return timeValue;
    }

    public TimeUnits getTimeUnits() {
        return timeUnits;
    }

    public void clearActionEvents() {
        actionEvents.clear();
    }

    public void removeActionEvent(ActionEvent actionEvent) {
        actionEvents.remove(actionEvent);
    }

}