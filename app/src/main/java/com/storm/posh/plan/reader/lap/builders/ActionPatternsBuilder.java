package com.storm.posh.plan.reader.lap.builders;

import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.TimeUnits;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.action.ActionPattern;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: @Andreas.
 * Date : @29/12/2015
 */
public class ActionPatternsBuilder {

    private Pattern PATTERN = Pattern.compile("\\((.+?)\\)");

    public ActionPattern actionPatternElementBuilder(String elementAsString) {
        String name = getElementName(elementAsString);
        Queue<String> timeAndActions = getTimeAndActions(elementAsString);
        // First item in the queue is the time, get that and leave rest
        String[] time = getSpaceSplit(timeAndActions.poll());
        TimeUnits timeUnits = TimeUnits.getTimeUnits(time[0]);
        double timeValue = Double.valueOf(time[1]);
        return new ActionPattern(name, timeValue, timeUnits, getActions(timeAndActions.poll()));
    }

    private String[] getSpaceSplit(String elementAsString) {
        return elementAsString.split(" ");
    }

    private List<ActionEvent> getActions(String actionsString) {
        List<ActionEvent> actions = new ArrayList<>();

        String[] actionsArray = getSpaceSplit(actionsString);
        if (actionsArray.length > 0) {
            for (int i = 0; i < actionsArray.length; i++) {
                actions.add(Plan.getInstance().createAction(actionsString));
            }
            return actions;
        } else
            return new ArrayList<>();
    }

    private String getElementName(String elementAsString) {
        String[] lineAsArray = elementAsString.split(" ");
        // We get position 1, as 0 is AP
        return lineAsArray[1];
    }

    private Queue<String> getTimeAndActions(String elementAsString) {
        Queue<String> actions = new ArrayDeque<>();
        Matcher matcher = PATTERN.matcher(elementAsString.substring(1));
        while (matcher.find()) {
            actions.add(matcher.group(1));
        }
        return actions;
    }
}