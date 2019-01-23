package com.storm.posh.planner.planelements;

// file did not exist previously

public class PlanComparable extends PlanElement {
    public Double value;
    public String comparator;

    @Override
    public String toString() {
        return String.format("%s { name='%s' value=%d comparator='%s' }", this.getClass().getSimpleName(), name, value, comparator);
    }

    public PlanComparable(String name, String value, String comparator) {
        super(name);
        this.value = Double.valueOf(value);
        this.comparator = comparator;
    }
}

/*
using System.Collections.Generic;

public class Sense
{
    private string name = "";
    internal string Name
    {
        get
        {
            return name;
        }
    }

    private double value;
    internal double Value
    {
        get
        {
            return value;
        }
    }

    private string comperator;
    internal string Comperator
    {
        get
        {
            return comperator;
        }
    }

    public Sense(string name, double value, string comperator)
    {
        this.name = name;
        this.value = value;
        this.comperator = comperator;
    }
}
 */