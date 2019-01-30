package com.storm.posh.plan.planelements;

//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;

/**
 * <p>
 *
 * @author :   Andreas Theodorou - www.recklesscoding.com
 * @version :   %G%
 */
public enum TimeUnits {
    MILISECONDS("ms"), SECONDS("seconds"), MINUTES("minutes");

    private String value;

    TimeUnits(String value) {

        this.value = value;
    }

    public static TimeUnits getTimeUnits(String time) {
        String timeLower = time.toLowerCase();
        switch (timeLower) {
            case "ms":
                return MILISECONDS;
            case "seconds":
                return SECONDS;
            case "minutes":
                return MINUTES;
        }
        return SECONDS;
    }

//    public static ObservableList<String> getAll() {
//        ObservableList<String> timeUnits = FXCollections.observableArrayList();
//        timeUnits.addAll(MILISECONDS.toString(), SECONDS.toString(), MINUTES.toString());
//
//        return timeUnits;
//    }
}