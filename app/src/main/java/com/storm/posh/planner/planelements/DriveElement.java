package com.storm.posh.planner.planelements;

import java.util.List;
import java.util.Objects;

public class DriveElement extends PlanElement {
    public List<Sense> senses;
    public String checkTime;
    public String triggerableName;
    public PlanElement triggerableElement;

    @Override
    public String toString() {
        return "DriveElement {" +
                " name='" + name + '\'' +
                " triggerableName='" + name + '\'' +
                " triggerableElement='" + Objects.toString(triggerableElement)+ '\'' +
                " checkTime='" + checkTime + '\'' +
                " senses=" + Objects.toString(senses)+
                " }";
    }

    public DriveElement(String name, List senses, String checkTime, String triggerableName) {
        super(name);
        this.senses = senses;
        this.checkTime = checkTime;
        this.triggerableName = triggerableName;
    }

    public DriveElement(String name, List senses, String checkTime, PlanElement triggerableElement) {
        super(name);
        this.senses = senses;
        this.checkTime = checkTime;
        this.triggerableElement = triggerableElement;
        this.triggerableName = triggerableElement.name;
    }
}

/*
using UnityEngine;
using UnityEditor;
using System.Collections.Generic;

public class DriveElement : PlanElement
{
    private List<Sense> senses;
    internal List<Sense> Senses
    {
        get
        {
            return senses;
        }
    }

    private float checkTime = 0;

    private float nextCheck = 0;
    internal float NextCheck
    {
        get
        {
            return nextCheck;
        }
    }
    internal void UpdateNextCheck()
    {
        nextCheck = Time.time + checkTime;
    }

    private PlanElement triggerableElement;
    internal PlanElement TriggerableElement
    {
        get
        {
            return triggerableElement;
        }
        set
        {
            triggerableElement = value;
        }
    }

    public DriveElement(string name, List<Sense> senses, PlanElement triggerableElement, float checkTime) : base(name)
    {
        if (senses != null)
            this.senses = senses;
        else
            this.senses = new List<Sense>();

        this.triggerableElement = triggerableElement;

        this.checkTime = checkTime;
        this.nextCheck = this.checkTime;
    }
}
 */