package com.storm.pepper;

import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.drives.DriveCollection;

public interface PepperLog {
    void appendLog(String tag, String text, boolean server);
    void appendLog(String tag, String text);
    void appendLog(String text);
    void clearLog();

    void addCurrentElement(PlanElement element);
    void clearCurrentElements();

    void checkedBooleanSense(String tag, Sense sense, boolean value);
    void checkedDoubleSense(String tag, Sense sense, double value);
    void clearCheckedSenses();

    void notifyABOD3(String name, String type);
}
