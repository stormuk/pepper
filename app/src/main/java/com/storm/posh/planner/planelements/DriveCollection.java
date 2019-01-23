package com.storm.posh.planner.planelements;

import java.util.List;
import java.util.Objects;

public class DriveCollection extends PlanElement {
    public List<Sense> goals;
    public List<DriveElement> driveElements;
    public Integer priority;

    @Override
    public String toString() {
        return "DriveElement {" +
                " name='" + name + '\'' +
                " priority=" + priority.toString() +
                " goals=" + Objects.toString(goals)+
                " driveElements=" + Objects.toString(driveElements)+
                " }";
    }

    public DriveCollection(String name, List<Sense> goals, List<DriveElement> driveElements, Integer priority) {
        super(name);
        this.goals = goals;
        this.driveElements = driveElements;
        this.priority = priority;
    }

}

/*
using UnityEngine;
using UnityEditor;
using System.Collections.Generic;

public class DriveCollection: PlanElement
{
    private int priority = 0;
    public int Priority
    {
        get
        {
            return priority;
        }
    }

    private List<Sense> senses;
    internal List<Sense> Senses
    {
        get
        {
            return senses;
        }
    }

    private List<DriveElement> driveElements;
    internal List<DriveElement> DriveElements
    {
        get
        {
            return driveElements;
        }
    }


    public DriveCollection(string name, List<Sense> senses,
        List<DriveElement> driveElements, int priority) : base(name)
    {
        if (senses != null)
            this.senses = senses;
        else
            this.senses = new List<Sense>();

        if (driveElements != null)
            this.driveElements = driveElements;
        else
            this.driveElements = new List<DriveElement>();

        this.priority = priority;
    }
}
 */