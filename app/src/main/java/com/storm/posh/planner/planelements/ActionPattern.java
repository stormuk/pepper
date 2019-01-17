package com.storm.posh.planner.planelements;

import java.util.List;

public class ActionPattern extends PlanElement {
    public List<Action> actions;

    public ActionPattern(String name, List actions) {
        super(name);
        this.actions = actions;
    }
}

/*


public class PlanElement {
    public String name;

    public PlanElement(String name) {
        this.name = name;
    }
}


using System.Collections.Generic;

public class ActionPattern : PlanElement
{
    private List<Action> actions;
    internal List<Action> Actions
    {
        get
        {
            return actions;
        }
    }

    public ActionPattern(string name, List<Action> actions) : base(name)
    {
        this.actions = actions;
    }
}
 */