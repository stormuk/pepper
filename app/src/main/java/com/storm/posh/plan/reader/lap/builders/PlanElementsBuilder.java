package com.storm.posh.plan.reader.lap.builders;

import com.storm.posh.plan.planelements.PlanElement;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Author: @Andreas.
 * Date : @29/12/2015
 */
public abstract class PlanElementsBuilder {

    protected Pattern PATTERN = Pattern.compile("\\((.+?)\\)");

    public Queue<PlanElement> getPlanElements(Queue<String> planElementsLines) {
        Queue<PlanElement> planElements = new ArrayDeque<>();
        int size = planElementsLines.size();
        for (int i = 0; i < size; i++) {
            String planElement = planElementsLines.poll();
//            planElements.add(planElementBuilder(planElement));
        }
        System.out.println(planElementsLines);
        return planElements;
    }
}