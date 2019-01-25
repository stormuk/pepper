package com.storm.posh.plan.planelements.action;

import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.ElementWithTrigger;
import com.storm.posh.plan.planelements.TimeUnits;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: Andreas
 * Date: 18/01/2016.
 */
public class ActionPatternElement extends ElementWithTrigger {

    public ActionPatternElement(String nameOfElement) {
        super(nameOfElement);
    }

    public ActionPatternElement(String name, double timeValue, TimeUnits timeUnits, List<ActionEvent> actions) {
        super(name);
    }
}