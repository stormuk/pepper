package com.storm.posh.plan.planelements;

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

    public Sense(String nameOfElement, String comperator, String value) {
        super(nameOfElement);
        this.value = value;
        this.comparator = comperator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        System.out.println(value);
        this.value = value;
    }

    public boolean getBooleanValue() {
        return value == "1";
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