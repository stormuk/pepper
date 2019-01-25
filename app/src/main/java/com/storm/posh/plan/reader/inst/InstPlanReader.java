package com.storm.posh.plan.reader.inst;

import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.ElementWithTrigger;
import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.action.ActionPattern;
import com.storm.posh.plan.planelements.competence.Competence;
import com.storm.posh.plan.planelements.competence.CompetenceElement;
import com.storm.posh.plan.planelements.drives.DriveCollection;
import com.storm.posh.plan.reader.PlanReader;
import com.storm.posh.plan.reader.inst.builders.InstPlanReaderHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Author: @Andreas.
 * Date : @29/12/2015
 */
public class InstPlanReader extends PlanReader {

    @Override
    public void readFile(String fileName) {
        readInstFile(fileName);
    }

    private void readInstFile(String fileName) {
        //All of them are local parameter, we don't need them in the memory after their job is done.
        InstPlanReaderHelper helper = new InstPlanReaderHelper();

        int runCounter = 0;
        String currentLine;

        // Read the file as one string.
        BufferedReader file;
        while (runCounter < 5) {
            try {
                file = new BufferedReader(new FileReader(fileName));

                while ((currentLine = file.readLine()) != null) {
                    currentLine = removeSpacesTabs(currentLine);

                    if (isStartingWithPrefix(currentLine, "// Drive: ") && (runCounter == 0)) {
                        createNewDriveCollection(currentLine, file, helper);
                    }

                    if (isStartingWithPrefix(currentLine, "// Competence: ") && (runCounter == 1)) {
                        createNewCompetence(currentLine, file, helper);
                    }

                    if (isStartingWithPrefix(currentLine, "// ActionPattern: ") && (runCounter == 2)) {
                        createNewActionPattern(currentLine, file, helper);
                    }

                    if (isStartingWithPrefix(currentLine, "// CompetenceElement: ") && (runCounter == 3)) {
                        createNewCompetenceElement(currentLine, file, helper);
                    }
                    if (isStartingWithPrefix(currentLine, "// ActionPatternElement: ") && (runCounter == 4)) {
                        createActionsForAP(file, helper);
                    }
                }
            } catch (IOException e) {
                // TODO: Exception handling
                e.printStackTrace();
            }
            runCounter++;
        }
    }

    private void createActionsForAP(BufferedReader file, InstPlanReaderHelper helper) throws IOException {
        boolean isDefinition = true;
        ActionPattern parrentElement = null;
        PlanElement childElement = null;
        String currentLine;
        while ((currentLine = file.readLine()) != null) {
            currentLine = removeSpacesTabs(currentLine);
            if (isParentLine(currentLine)) {
                parrentElement = (ActionPattern) Plan.getInstance().findActionPattern(helper.getParentName(currentLine, 0));
            }
            if (isChildrenLine(currentLine)) {
                childElement = Plan.getInstance().findCompetence(currentLine);
                if (childElement == null) {
                    childElement = Plan.getInstance().findActionPattern(helper.getChildName(currentLine));
                }
                if (childElement == null) {
                    childElement = Plan.getInstance().createAction(helper.getChildName(currentLine));
                }

            }
            if (isPELEMLine(currentLine)) {
                isDefinition = false;
            }
            if (isEndOfBlock(currentLine)) {
                break;
            }
        }
        if (isDefinition) {
            if (!(childElement instanceof ActionEvent))
                parrentElement.setTriggeredElement(childElement);
            else
                parrentElement.addAction((ActionEvent) childElement);
        }
    }

    private void createNewActionPattern(String currentLine, BufferedReader file, InstPlanReaderHelper helper) throws IOException {
        ActionPattern actionPattern = helper.buildActionPattern(currentLine);

        boolean isDefinition = true;
        ElementWithTrigger parrent;
        while ((currentLine = file.readLine()) != null) {
            currentLine = removeSpacesTabs(currentLine);
            if (isParentLine(currentLine)) {
                for (int i = 0; i < helper.getNumberOfParents(currentLine); i++) {
                    parrent = Plan.getInstance().findDriveCollection(helper.getParentName(currentLine, i));
                    if (parrent == null)
                        parrent = Plan.getInstance().findCompetenceElement(helper.getParentName(currentLine, i));
                    if (parrent == null)
                        parrent = (ElementWithTrigger) Plan.getInstance().findActionPattern(helper.getParentName(currentLine, i));
                    if (parrent != null) {
                        parrent.setTriggeredElement(actionPattern);
                    }
                }
            }
            if (isPELEMLine(currentLine))
                isDefinition = false;
            if (isEndOfBlock(currentLine))
                break;
        }
        if (isDefinition) {
            Plan.getInstance().addActionPattern(actionPattern);
        }
    }

    private void createNewCompetence(String currentLine, BufferedReader file, InstPlanReaderHelper helper) throws IOException {
        Competence competence = helper.buildCompetence(currentLine);

        boolean isDefinition = true;
        ElementWithTrigger parrent;
        while ((currentLine = file.readLine()) != null) {
            currentLine = removeSpacesTabs(currentLine);
            if (isParentLine(currentLine)) {
                for (int i = 0; i < helper.getNumberOfParents(currentLine); i++) {
                    parrent = Plan.getInstance().findDriveCollection(helper.getParentName(currentLine, i));
                    if (parrent == null) {
                        parrent = Plan.getInstance().findCompetenceElement(helper.getParentName(currentLine, i));
                    }
                    if (parrent == null) {
                        parrent = (ElementWithTrigger) Plan.getInstance().findActionPattern(helper.getParentName(currentLine, i));
                    }
                    if (parrent != null) {
                        parrent.setTriggeredElement(competence);
                    }
                    if (parrent == null) {
                        ActionEvent action = Plan.getInstance().findAction(competence.getNameOfElement());
                        if (action != null) {
                            List<ActionPattern> actionPatternsWithAction = Plan.getInstance().findActionPatternsWithAction(competence.getNameOfElement());
                            for (ActionPattern actionPattern : actionPatternsWithAction) {
                                actionPattern.removeActionEvent(action);
                                actionPattern.setTriggeredElement(competence);
                            }
                        }
                    }
                }
            }
            if (isPELEMLine(currentLine)) {
                isDefinition = false;
            }
            if (isEndOfBlock(currentLine)) {
                break;
            }
        }
        if (isDefinition) {
            Plan.getInstance().addCompetence(competence);
        }
    }

    private void createNewDriveCollection(String currentLine, BufferedReader file, InstPlanReaderHelper helper) throws IOException {
        DriveCollection driveCollection = helper.buildDriveCollector(currentLine);

        if (isDefinition(file))
            Plan.getInstance().addDriveCollection(driveCollection);
    }

    private void createNewCompetenceElement(String currentLine, BufferedReader file, InstPlanReaderHelper helper) throws IOException {
        CompetenceElement competenceElement = helper.buildCompetenceElement(currentLine);
        Competence competence = null;
        boolean isDefinition = true;
        while ((currentLine = file.readLine()) != null) {
            currentLine = removeSpacesTabs(currentLine);
            if (isParentLine(currentLine))
                competence = Plan.getInstance().findCompetence(helper.getParentName(currentLine, 0));
            if (isChildrenLine(currentLine)) {
                PlanElement triggeredElement = Plan.getInstance().findActionPatternOrCompetence(helper.getChildName(currentLine));
                if (triggeredElement != null) {
                    competenceElement.setTriggeredElement(triggeredElement);
                } else {
                    competenceElement.setTriggeredElement(Plan.getInstance().createAction(helper.getChildName(currentLine)));
                }
            }
            if (isSense(currentLine)) {
                //TODO: Sense
            }
            if (isPELEMLine(currentLine))
                isDefinition = false;
            if (isEndOfBlock(currentLine))
                break;
        }
        if (isDefinition)
            competence.addCompetenceElement(competenceElement);
    }

    private boolean isSense(String currentLine) {
        return isStartingWithPrefix(currentLine, "// \tSense: ");
    }

    private boolean isDefinition(BufferedReader file) throws IOException {
        String currentLine;
        while ((currentLine = file.readLine()) != null) {
            currentLine = removeSpacesTabs(currentLine);
            if (isPELEMLine(currentLine))
                return false;
            if (isEndOfBlock(currentLine))
                break;
        }
        return true;
    }

    private boolean isEndOfBlock(String currentLine) {
        return currentLine.equals("") || currentLine.equals("\n");
    }

    private boolean isParentLine(String currentLine) {
        return isStartingWithPrefix(currentLine, "// \tParents: ");
    }

    private boolean isChildrenLine(String currentLine) {
        return isStartingWithPrefix(currentLine, "// \tChildren: ");
    }

    private boolean isPELEMLine(String currentLine) {
        return isStartingWithPrefix(currentLine, "PELEM");
    }

    private boolean isStartingWithPrefix(String currentLine, String pelem) {
        return currentLine.startsWith(pelem);
    }

    private String removeSpacesTabs(String line) {
        return line.trim();
    }
}