package com.storm.posh.planner.planelements;

import java.util.List;
import java.util.Objects;

public class Competence extends PlanElement {
    public List<Sense> goals;
    public List<CompetenceElement> competenceElements;

    @Override
    public String toString() {
        return "DriveElement {" +
                " name='" + name + '\'' +
                " goals=" + Objects.toString(goals)+
                " competenceElements=" + Objects.toString(competenceElements)+
                " }";
    }

    public Competence(String name, List<Sense> goals, List<CompetenceElement> competenceElements) {
        super(name);
        this.goals = goals;
        this.competenceElements = competenceElements;
    }
}

/*
using UnityEngine;
using UnityEditor;
using System.Collections.Generic;

public class Competence : PlanElement
{
    private List<Sense> goals;
    internal List<Sense> Goals
    {
        get
        {
            return goals;
        }
    }

    private List<CompetenceElement> elements;
    internal List<CompetenceElement> Elements
    {
        get
        {
            return elements;
        }
    }

    public Competence(string name, List<Sense> goals, List<CompetenceElement> elements) : base(name)
    {
        if (goals != null)
            this.goals = goals;
        else
            this.goals = new List<Sense>();

        if (elements != null)
            this.elements = elements;
        else
            this.elements = new List<CompetenceElement>();
    }
}
 */