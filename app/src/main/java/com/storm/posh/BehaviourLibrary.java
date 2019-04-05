package com.storm.posh;

import com.aldebaran.qi.sdk.QiContext;
import com.storm.pepper.PepperLog;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.action.ActionEvent;

public interface BehaviourLibrary {
    void setPepperLog(PepperLog pepperLog);
    void setQiContext(QiContext qiContext);
    void reset();
    boolean getBooleanSense(Sense sense);
    double getDoubleSense(Sense sense);
    void executeAction(ActionEvent action);
}
