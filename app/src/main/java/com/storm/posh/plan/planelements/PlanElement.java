package com.storm.posh.plan.planelements;

//import com.storm.posh.plan.nodes.PlanElementNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 28/12/2015.
 */
public class PlanElement {

    private static int ID = 0;

    private String nameOfElement;

    private boolean enabled = true;

    private int usageCounter = 0;

    private boolean isSetToUpdate = false;

//    private List<PlanElementNode> planElementNodes = new ArrayList<>();

    public PlanElement(String nameOfElement) {
        this.nameOfElement = nameOfElement;
        ID++;
    }

//    public synchronized void setToUpdate() {
//        isSetToUpdate = true;
//        for (PlanElementNode planElementNode: planElementNodes) {
//            planElementNode.increaseGlow();
//        }
//        increaseUsageCounter();
//    }

    public void setFinishUpdate() {
        isSetToUpdate = false;
    }

//    public void encloseToNode(PlanElementNode planElementNode) {
//        this.planElementNodes.add(planElementNode);
//    }


    public int getID() {
        return ID;
    }

    public String getNameOfElement() {
        return nameOfElement;
    }

    public void setNameOfElement(String nameOfElement) {
        this.nameOfElement = nameOfElement;
    }

    @Override
    public String toString() {
        return getNameOfElement();
    }

    public int getUsageCounter() {
        return usageCounter;
    }

    private void increaseUsageCounter() {
        this.usageCounter++;
    }

    public boolean isSetToUpdate() {
        return isSetToUpdate;
    }
}