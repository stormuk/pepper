package com.storm.posh.plan.planelements.competence;

import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.TimeUnits;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: @Andreas.
 * Date : @31/12/2015
 */
public class Competence extends PlanElement {

    private double timeout;

    private TimeUnits timeUnits = TimeUnits.SECONDS;

    private List<Sense> goals = new LinkedList<>();

    private List<CompetenceElement> competenceElements = new LinkedList<>();

    public Competence(String nameOfElement) {
        super(nameOfElement);
    }

    public Competence(String nameOfElement, List<Sense> goals, List<CompetenceElement> comptenceElements) {
        super(nameOfElement);
        this.goals = goals;

        if (goals != null) {
            this.goals = goals;
        } else {
            this.goals = new LinkedList<>();
        }
        if (comptenceElements != null) {
            this.competenceElements = comptenceElements;
        } else {
            this.competenceElements = new LinkedList<>();
        }
    }

    public Competence(String nameOfElement, double timeout, TimeUnits timeUnits, List<Sense> goals, List<CompetenceElement> comptenceElements) {
        super(nameOfElement);
        this.timeout = timeout;
        this.timeUnits = timeUnits;
        this.goals = goals;
        this.competenceElements = comptenceElements;
    }

    public void setCompetenceElements(List<CompetenceElement> competenceElements) {
        this.competenceElements = competenceElements;
    }

    public List<CompetenceElement> getCompetenceElements() {
        return competenceElements;
    }

    public void removeCompetenceElement(CompetenceElement competenceElement) {
        competenceElements.remove(competenceElement);
    }

    public void addCompetenceElement(CompetenceElement competenceElement) {
        competenceElements.add(competenceElement);
    }

    public double getTimeout() {
        return timeout;
    }

    public void setTimeout(double timeout) {
        this.timeout = timeout;
    }

    public List<Sense> getGoals() {
        return goals;
    }

    public void setGoals(List<Sense> goals) {
        this.goals = goals;
    }

    public void setTimeUnits(TimeUnits timeUnits) {
        this.timeUnits = timeUnits;
    }

    public TimeUnits getTimeUnits() {
        return timeUnits;
    }
}