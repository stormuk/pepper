package com.storm.posh.planner.planelements;

public class Sense {
    public String name;
    public Double value;
    public String comparator;

    public String toString() {
        return String.format("%s { name='%s' value=%f comparator='%s' }", this.getClass().getSimpleName(), name, value, comparator);
    }

    public Sense(String name, String value, String comparator) {
        this.name = name;
        this.value = Double.valueOf(value);
        this.comparator = comparator;
    }
}
