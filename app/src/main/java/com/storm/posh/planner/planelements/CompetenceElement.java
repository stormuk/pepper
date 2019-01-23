package com.storm.posh.planner.planelements;

import java.util.List;
import java.util.Objects;

public class CompetenceElement extends PlanElement {
    public List senses;
    public PlanElement triggerableElement;
    public String triggerableName;

    private static final String TAG = CompetenceElement.class.getSimpleName();

    @Override
    public String toString() {
        return String.format(
           "CompetenceElement { name='%s' senses='%s' triggerableName='%s' triggerableElement='%s' }",
           name, Objects.toString(senses), triggerableName, Objects.toString(triggerableElement)
        );
    }

    public CompetenceElement(String name, List<Sense> senses, String triggerableName) {
        super(name);
        this.senses = senses;
        this.triggerableName = triggerableName;
    }


    public CompetenceElement(String name, List<Sense> senses, PlanElement triggerableElement) {
        super(name);
        this.senses = senses;
        this.triggerableElement = triggerableElement;
        this.triggerableName = triggerableElement.name;
    }
}

/*
using UnityEngine;
using UnityEditor;
using System.Collections.Generic;

public class CompetenceElement
{
    private string name = "";
    internal string Name
    {
        get
        {
            return name;
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


    public CompetenceElement(string name, List<Sense> senses, PlanElement triggerableElement)
    {
        this.name = name;

        if (senses != null)
            this.senses = senses;
        else
            this.senses = new List<Sense>();

        this.triggerableElement = triggerableElement;
    }
}
 */