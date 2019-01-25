package com.storm.posh.plan.reader.lap.builders;

import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.TimeUnits;
import com.storm.posh.plan.planelements.competence.Competence;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: @Andreas.
 * Date : @29/12/2015
 */
public class CompetencesBuilder {

    private Pattern PATTERN = Pattern.compile("\\((.+?)\\)");

    public Competence createCompetenceFromLAPLine(String competenceAsString) {
        String name = getCompetenceName(competenceAsString);
        List<Sense> goalElements = getGoalElements(competenceAsString);

        String[] time = getSpaceSplit(goalElements.get(0).getNameOfElement());
        TimeUnits timeUnits = TimeUnits.getTimeUnits(time[0]);
        double timeValue = Double.valueOf(time[1]);

        goalElements.remove(0);
        return new Competence(name, timeValue, timeUnits, goalElements, null);
    }

    private List<Sense> getGoalElements(String goalAsString) {
        List<Sense> goalElements = new LinkedList<>();
        Matcher matcher = PATTERN.matcher(goalAsString.substring(1));
        int counter = 0;
        String matchFound;
        String[] matchFoundAsArray;
        String nameFound;
        String predicateFound = null;
        String goalValueFound = null;
        while (matcher.find()) {
            matchFound = matcher.group(1);
            matchFoundAsArray = getSpaceSplit(matchFound);
            if (counter == 1)
                matchFound = getFirstGoal(matchFound);
            if (goalHasPredicateAndValue(matchFoundAsArray)) {
                nameFound = getGoalName(matchFoundAsArray);
                predicateFound = getGoalPredicate(matchFoundAsArray);
                goalValueFound = getGoalValue(matchFoundAsArray);
            } else
                nameFound = matchFound;

            goalElements.add(new Sense(nameFound, goalValueFound, predicateFound));
            predicateFound = null;
            goalValueFound = null;
            counter++;
        }
        return goalElements;
    }

    private String getGoalName(String[] matchFoundAsArray) {
        return matchFoundAsArray[0].replaceAll("\\(", "");
    }

    private String getCompetenceName(String elementAsString) {
        // We get position 1, as 0 is (C
        return getSpaceSplit(elementAsString)[1];
    }

    private String getFirstGoal(String elementAsString) {
        return getSpaceSplit(elementAsString)[1].replaceAll("\\(", "").replaceAll("\\)", "");
    }

    private String getGoalValue(String[] elementAsStringArray) {
        return elementAsStringArray[1];
    }

    private String getGoalPredicate(String[] elementAsStringArray) {
        return elementAsStringArray[2];
    }

    private boolean goalHasPredicateAndValue(String[] elementAsStringArray) {
        return (elementAsStringArray.length >= 3);
    }

    private String[] getSpaceSplit(String elementAsString) {
        return elementAsString.split(" ");
    }
}