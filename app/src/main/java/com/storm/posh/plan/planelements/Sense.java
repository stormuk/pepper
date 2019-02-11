package com.storm.posh.plan.planelements;

import android.util.Log;

/**
 * Author: @Andreas.
 * Date : @13/01/2016
 */
public class Sense extends PlanElement {

    private String value;

    private String comparator;

    public Sense(String nameOfElement) {
        super(nameOfElement);
    }

    public Sense(String nameOfElement, String comparator, String value) {
        super(nameOfElement);
        this.value = value;
        this.comparator = comparator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean getBooleanValue() {
        Log.d("D/Sense", String.format("Name: %s. Value: %s", getNameOfElement(), value));
        return value.equals("1");
    }

    public Double getDoubleValue() {
        return Double.valueOf(value);
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }
}