package com.storm.posh.plan.reader.lap.builders;

import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.competence.CompetenceElement;
import com.storm.posh.plan.planelements.drives.DriveElement;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: @Andreas.
 * Date : @13/01/2016
 */
public class ElementBuilder {

    private Pattern PATTERN = Pattern.compile("\\((.+?)\\)");

    public CompetenceElement createCompetenceElementFromLAPLine(String elementAsString) {
        String[] elementAsStringArray = getSpaceSplit(elementAsString);

        return new CompetenceElement(getElementName(elementAsStringArray), getSenses(elementAsString),
                getTriggeredElement(elementAsStringArray), getRetries(elementAsStringArray));
    }

    public DriveElement createDriveElement(String driveElementAsString) {
        String[] driveElementAsStringArray = getSpaceSplit(driveElementAsString);

        List<Sense> driveElementSenses = getSenses(driveElementAsString);
        // Remove the time of the AP/C found by the matcher wrongly
        driveElementSenses.remove(driveElementSenses.size() - 1);

        return new DriveElement(getElementName(driveElementAsStringArray), driveElementSenses,
                getACTrigger(driveElementAsStringArray), getFrequencyUnits(driveElementAsStringArray), getFrequencyValue(driveElementAsStringArray));
    }

    private List<Sense> getSenses(String elementAsString) {
        Matcher matcher = PATTERN.matcher(elementAsString.substring(1));
        List<Sense> senses = new LinkedList<>();
        String matchFound;
        String[] matchFoundAsArray;
        String predicateFound = null;
        String valueFound = null;
        int counter = 0;
        while (matcher.find()) {
            if (counter == 0)
                matchFound = getFirstSense(matcher.group(1));
            else
                matchFound = matcher.group(1);
            matchFoundAsArray = getSpaceSplit(matchFound);
            if (hasPredicateAndValue(matchFoundAsArray)) {
                matchFound = getSenseName(matchFoundAsArray);
                valueFound = getSenseValue(matchFoundAsArray);
                predicateFound = getSensePredicate(matchFoundAsArray);
            }
            senses.add(new Sense(matchFound, predicateFound, valueFound));
            predicateFound = null;
            valueFound = null;
            counter++;
        }

        return senses;
    }

    private String[] getSpaceSplit(String elementAsString) {
        return elementAsString.split(" ");
    }

    private String getElementName(String[] driveElementAsStringArray) {
        return driveElementAsStringArray[0].substring(1);
    }

    private String getSenseName(String[] matchFoundAsArray) {
        return matchFoundAsArray[0];
    }

    private String getSenseValue(String[] matchFoundAsArray) {
        return matchFoundAsArray[1];
    }

    private String getSensePredicate(String[] matchFoundAsArray) {
        return matchFoundAsArray[2];
    }

    private boolean hasPredicateAndValue(String[] matchFoundAsArray) {
        return matchFoundAsArray.length == 3;
    }

    /**
     * Removes the 10 digits of "trigger ((" detected by the Matcher
     *
     * @param elementAsString A string containing the sense as an unprocessed, LAP-formatted line.
     * @return A string containing the first sense detected in an element.
     */
    private String getFirstSense(String elementAsString) {
        return elementAsString.substring(9).replaceAll("\\(", "");
    }

    private Integer getRetries(String[] elementAsStringArray) {
        try {
            Integer retries = Integer.valueOf(elementAsStringArray[elementAsStringArray.length - 1].replaceAll("\\)", ""));
            return retries;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private PlanElement getTriggeredElement(String[] elementAsStringArray) {
        PlanElement actionPatternOrCompetence = Plan.getInstance().findActionPatternOrCompetence(elementAsStringArray[elementAsStringArray.length - 2]);
        if (actionPatternOrCompetence != null)
            return actionPatternOrCompetence;
        else
            return Plan.getInstance().createAction(elementAsStringArray[elementAsStringArray.length - 2]);
    }

    private PlanElement getACTrigger(String[] driveElementAsStringArray) {
        String ac = driveElementAsStringArray[driveElementAsStringArray.length - 2].split("\\(")[0];
        PlanElement actionPatternOrCompetence = Plan.getInstance().findActionPatternOrCompetence(ac);
        if (actionPatternOrCompetence != null)
            return actionPatternOrCompetence;
        else
            return Plan.getInstance().createAction(ac);
    }

    private Double getFrequencyValue(String[] driveElementAsStringArray) {
        try {
            return Double.parseDouble(driveElementAsStringArray[driveElementAsStringArray.length - 1].replaceAll("\\)", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getFrequencyUnits(String[] driveElementAsStringArray) {
        return driveElementAsStringArray[driveElementAsStringArray.length - 2].split("\\(")[1];
    }
}