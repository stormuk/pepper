package com.storm.posh.planner.planelements;

import android.util.Log;

import com.storm.posh.planner.XMLPlanReader;

import java.util.Objects;

public class Action extends PlanElement {
    public double timeToComplete;

    private static final String TAG = Action.class.getSimpleName();

    @Override
    public String toString() {
        return "Action{" +
                "timeToComplete=" + Objects.toString(timeToComplete) +
                ", name='" + name + '\'' +
                '}';
    }

    public Action(String name, Double timeToComplete) {
        super(name);
        this.timeToComplete = Double.valueOf(timeToComplete);
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