package com.storm.posh.plan.planelements.competence;

import com.storm.posh.plan.planelements.ElementWithTrigger;
import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: @Andreas.
 * Date : @01/01/2016
 */
public class CompetenceElement extends ElementWithTrigger {

    private List<Sense> senses;

    private int retries = -1;

    public CompetenceElement(String nameOfElement) {
        super(nameOfElement);

        this.senses = new LinkedList<>();
    }

    public CompetenceElement(String nameOfElement, List<Sense> senses) {
        super(nameOfElement);

        if (senses != null)
            this.senses = senses;
        else
            this.senses = new ArrayList<>();
    }


    public CompetenceElement(String nameOfElement, List<Sense> senses, PlanElement triggeredElement) {
        super(nameOfElement, triggeredElement);

        if (senses != null)
            this.senses = senses;
        else
            this.senses = new ArrayList<>();
    }

    public CompetenceElement(String nameOfElement, List<Sense> senses, PlanElement triggeredElement, Integer retries) {
        super(nameOfElement, triggeredElement);

        this.senses = senses;
        if (retries != null) {
            this.retries = retries;
        }
    }

    public List<Sense> getSenses() {
        return senses;
    }

    public void setSenses(List<Sense> senses) {
        this.senses = senses;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getRetries() {
        return retries;
    }
}
