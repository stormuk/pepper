package com.storm.posh.plan.planelements.drives;

import com.storm.posh.plan.planelements.ElementWithTrigger;
import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: @Andreas.
 * Date : @07/01/2016
 */
public class DriveCollection extends ElementWithTrigger {

    private int priority;

    private boolean realTime = true;

    private boolean current = false;

    private volatile List<Sense> goals  = new LinkedList<>();

    private volatile List<DriveElement> driveElements = new LinkedList<>();

    private volatile PlanElement currentElement;

    public DriveCollection(String nameOfElement) {
        super(nameOfElement);
    }

    public DriveCollection(String nameOfElement, boolean realTime, List<Sense> goals) {
        super(nameOfElement);
        this.realTime = realTime;
        if  (!(goals == null))
            this.goals = goals;
        else
            this.goals = new LinkedList<>();
    }

    public DriveCollection(String nameOfElement, List<Sense> goals) {
        super(nameOfElement);
        this.realTime = realTime;
        if  (!(goals == null))
            this.goals = goals;
        else
            this.goals = new LinkedList<>();
    }

    public DriveCollection(String nameOfElement, List<Sense> goals, List<DriveElement> driveElements) {
        super(nameOfElement);
        this.realTime = realTime;
        if  (!(goals == null))
            this.goals = goals;
        else
            this.goals = new LinkedList<>();

        if  (!(driveElements == null))
            this.driveElements = driveElements;
        else
            this.driveElements = new LinkedList<>();
    }

    public DriveElement findDriveElement(String elementsName) {
        for (DriveElement drive : driveElements) {
            if (drive.getNameOfElement().equals(elementsName))
                return drive;
        }

        return null;
    }

    public void addDriveElement(DriveElement element) {
        this.driveElements.add(element);
    }

    public void removeDriveElement(DriveElement element) {
        this.driveElements.remove(element);
    }

    public boolean containsDriveElement(DriveElement element) {
        return driveElements.contains(element);
    }

    public List<DriveElement> getDriveElements() {
        return driveElements;
    }

    public void setDriveElements(List<DriveElement> driveElements) {
        this.driveElements = driveElements;
    }

    public void setGoals(List<Sense> goals)
    {
        this.goals = goals;
    }


    public List<Sense> getGoals() {
        return goals;
    }

    public boolean isRealTime() {
        return realTime;
    }

    public void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public PlanElement getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(PlanElement currentElement) {
        this.currentElement = currentElement;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
