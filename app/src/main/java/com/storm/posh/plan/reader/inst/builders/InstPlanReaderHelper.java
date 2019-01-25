package com.storm.posh.plan.reader.inst.builders;

import com.storm.posh.plan.planelements.action.ActionPattern;
import com.storm.posh.plan.planelements.competence.Competence;
import com.storm.posh.plan.planelements.competence.CompetenceElement;
import com.storm.posh.plan.planelements.drives.DriveCollection;

/**
 * Author: @Andreas.
 * Date : @29/12/2015
 */
public class InstPlanReaderHelper {

    public ActionPattern buildActionPattern(String nameLine) {
        return new ActionPattern(getElementName(nameLine));
    }

    public Competence buildCompetence(String nameLine) {
        return new Competence(getElementName(nameLine));
    }

    public CompetenceElement buildCompetenceElement(String nameLine) {
        return new CompetenceElement(getElementName(nameLine));
    }

    public DriveCollection buildDriveCollector(String nameLine) {
        return new DriveCollection(getElementName(nameLine));
    }

    public String getChildName(String currentLine)
    {
        return getSpaceSplit(currentLine)[2];
    }

    public String getParentName(String currentLine, int number)
    {
        return getSpaceSplit(currentLine)[number+2];
    }

    public int getNumberOfParents(String currentLine)
    {
        return getSpaceSplit(currentLine).length - 2;
    }
    
    private String getElementName(String nameLine) {
        return getSpaceSplit(nameLine)[2];
    }

    private String[] getSpaceSplit(String elementAsString) {
        return elementAsString.split(" ");
    }

}