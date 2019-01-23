package com.storm.posh.planner.planelements;

import java.util.List;
import java.util.Objects;

public class DriveElement extends PlanElement {
    public List<Sense> triggers;
    public Double checkTime = 0.0;
    public String triggerableName;
    public PlanElement triggerableElement;
    public long nextCheck = 0;

    @Override
    public String toString() {
        return "DriveElement {" +
                " name='" + name + '\'' +
                " triggerableName='" + name + '\'' +
                " triggerableElement='" + Objects.toString(triggerableElement)+ '\'' +
                " checkTime='" + checkTime + '\'' +
                " triggers=" + Objects.toString(triggers)+
                " }";
    }

    public DriveElement(String name, List<Sense> triggers, String checkTime, String triggerableName) {
        super(name);
        this.triggers = triggers;
        this.triggerableName = triggerableName;

        if (checkTime != null) {
            this.checkTime = Double.valueOf(checkTime);
        }
    }

    public DriveElement(String name, List<Sense> triggers, String checkTime, PlanElement triggerableElement) {
        this(name, triggers, checkTime, triggerableElement.name);
        this.triggerableElement = triggerableElement;
    }

    public void updateNextCheck(long time) {
        this.nextCheck = time + checkTime.longValue();
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