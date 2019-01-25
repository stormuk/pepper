package com.storm.posh.plan.reader.lap.builders;

import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.drives.DriveCollection;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: @Andreas.
 * Date : @07/01/2016
 */
public class DriveCollectionBuilder {

    private Pattern PATTERN = Pattern.compile("\\((.+?)\\)");

    /**
     * Creates a DriveCollection object.
     *
     * @param driveCollectionAsString
     * @return
     */
    public DriveCollection createDriveCollectionFromLAPFile(String driveCollectionAsString) {
        String[] driveCollectionAsStringArray = getSpaceSplit(driveCollectionAsString);

        return new DriveCollection(getDriveCollectionName(driveCollectionAsStringArray),
                isRealTimeDrive(driveCollectionAsStringArray), getGoalElements(driveCollectionAsString));
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
            if (counter == 0)
                matchFound = getFirstGoal(matchFound);
            if (hasPredicateAndValue(matchFoundAsArray)) {
                nameFound = getGoalName(matchFoundAsArray);
                predicateFound = getGoalPredicate(matchFoundAsArray);
                goalValueFound = getGoalValue(matchFoundAsArray);
            } else
                nameFound = matchFound;

            goalElements.add(new Sense(nameFound, predicateFound, goalValueFound));
            predicateFound = null;
            goalValueFound = null;
            counter++;
        }
        return goalElements;
    }

    private String getGoalName(String[] matchFoundAsArray) {
        return matchFoundAsArray[0].replaceAll("\\(", "");
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

    private String[] getSpaceSplit(String elementAsString) {
        return elementAsString.split(" ");
    }

    private boolean isRealTimeDrive(String[] driveCollectionAsStringArray) {
        return driveCollectionAsStringArray[0].equals("(RDC");
    }

    private boolean hasPredicateAndValue(String[] elementAsStringArray) {
        return (elementAsStringArray.length >= 3);
    }

    private String getDriveCollectionName(String[] driveCollectionAsStringArray) {
        return driveCollectionAsStringArray[1];
    }
}
