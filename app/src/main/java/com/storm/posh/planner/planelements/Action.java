package com.storm.posh.planner.planelements;

public class Action extends PlanElement {
    public double timeToComplete = 0;

    public Action(String name, Double timeToComplete) {
        super(name);
        this.timeToComplete = timeToComplete;
    }
}


/*
using System.Collections.Generic;

public class Action : PlanElement
        {
private double timeToComplete = 0;
        internal double TimeToComplete
        {
        get
        {
        return timeToComplete;
        }
        }

public Action(string name) : base(name)
        {
        }

public Action(string name, double timeToComplete) : base(name)
        {
        if (timeToComplete != null)
        this.timeToComplete = timeToComplete;
        }
        }
*/