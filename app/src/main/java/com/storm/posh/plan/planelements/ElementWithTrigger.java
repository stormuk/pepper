package com.storm.posh.plan.planelements;

/**
 * Athor: Andreas
 * Date: 04/02/2016.
 */
public class ElementWithTrigger extends PlanElement{

    private PlanElement triggeredElement;

    public ElementWithTrigger(String nameOfElement) {
        super(nameOfElement);
    }

    public ElementWithTrigger(String nameOfElement, PlanElement triggerableElement) {
        super(nameOfElement);

        this.triggeredElement = triggerableElement;
    }

    public PlanElement getTriggeredElement() {
        return triggeredElement;
    }

    public void removeTriggeredElement(){
        triggeredElement = null;
    }

    public void setTriggeredElement(PlanElement triggeredElement) {
        this.triggeredElement = triggeredElement;
    }
}