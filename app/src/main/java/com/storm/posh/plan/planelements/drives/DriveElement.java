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
public class DriveElement extends ElementWithTrigger {

    private long nextCheck;

    private double frequencyValue;

    private String frequencyUnit;

    private List<Sense> driveElementSenses = new LinkedList<>();

    public DriveElement(String nameOfElement) {
        super(nameOfElement);
    }

    /**
     * For XPOSH
     *
     * @param nameOfElement
     * @param driveElementSenses
     */
    public DriveElement(String nameOfElement, List<Sense> driveElementSenses) {
        super(nameOfElement);

        if (driveElementSenses != null) {
            this.driveElementSenses = driveElementSenses;
        }
        this.frequencyUnit = "sec";
        this.frequencyValue = 0;
    }

    public DriveElement(String nameOfElement,  PlanElement acTriggered, List<Sense> driveElementSenses) {
        super(nameOfElement, acTriggered);

        if (driveElementSenses != null) {
            this.driveElementSenses = driveElementSenses;
        }
        this.frequencyUnit = "sec";
        this.frequencyValue = 0;
    }

    /**
     * For XPOSH
     *
     * @param nameOfElement
     * @param driveElementSenses
     * @param frequencyValue
     */
    public DriveElement(String nameOfElement, List<Sense> driveElementSenses, double frequencyValue) {
        super(nameOfElement);

        if (driveElementSenses != null) {
            this.driveElementSenses = driveElementSenses;
        }
        this.frequencyUnit = "seconds";
        this.frequencyValue = frequencyValue;
    }

    /**
     * For XPOSH
     *
     * @param nameOfElement
     * @param driveElementSenses
     * @param frequencyValue
     */
    public DriveElement(String nameOfElement,  PlanElement acTriggered, List<Sense> driveElementSenses, double frequencyValue) {
        super(nameOfElement, acTriggered);

        if (driveElementSenses != null) {
            this.driveElementSenses = driveElementSenses;
        }
        this.frequencyUnit = "seconds";
        this.frequencyValue = frequencyValue;
    }


    public DriveElement(String nameOfElement, List<Sense> driveElementSenses, PlanElement acTriggered, String frequencyUnit, double frequencyValue) {
        super(nameOfElement, acTriggered);

        if (driveElementSenses != null) {
            this.driveElementSenses = driveElementSenses;
        }
        this.frequencyUnit = frequencyUnit;
        this.frequencyValue = frequencyValue;
    }

    public double getFrequencyValue() {
        return frequencyValue;
    }

    public void setFrequencyValue(double frequencyValue) {
        this.frequencyValue = frequencyValue;
    }

    public String getFrequencyUnit() {
        return frequencyUnit;
    }

    public void setFrequencyUnit(String frequencyUnit) {
        this.frequencyUnit = frequencyUnit;
    }

    public List<Sense> getSenses() {
        return driveElementSenses;
    }

    public void setDriveElementSenses(List<Sense> driveElementSenses) {
        this.driveElementSenses = driveElementSenses;
    }

    public long getNextCheck() {
        return nextCheck;
    }

    public void setNextCheck(long nextCheck) {
        this.nextCheck = nextCheck;
    }

    public void updateNextCheck(long time) {
        setNextCheck(time + getInterval().longValue());
    }

    private Double getInterval() {
        Double milliseconds;

        switch(frequencyUnit) {
            case "second":
            case "seconds":
                milliseconds = 1000 * frequencyValue;
                break;

            case "minute":
            case "minutes":
                milliseconds = 60 * 1000 * frequencyValue;
                break;

            case "hour":
            case "hours":
                milliseconds = 60 * 60 * 1000 * frequencyValue;
                break;

            default:
                milliseconds = 0.0;
                break;
        }

        return milliseconds;
    }
}
