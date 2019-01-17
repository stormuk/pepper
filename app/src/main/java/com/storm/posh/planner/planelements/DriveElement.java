package com.storm.posh.planner.planelements;

public class DriveElement extends PlanElement {
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