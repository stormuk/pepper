package com.storm.posh.plan.writer;

import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.action.ActionPattern;
import com.storm.posh.plan.planelements.competence.Competence;
import com.storm.posh.plan.planelements.competence.CompetenceElement;
import com.storm.posh.plan.planelements.drives.DriveCollection;
import com.storm.posh.plan.planelements.drives.DriveElement;
import com.storm.posh.util.IWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *
 * @author :   Andreas Theodorou - www.recklesscoding.com
 * @version :   %G%
 */
public class LapPlanWriter implements IWriter {

    private final static String EMPTY_SPACE = " ";

    private final static String NEW_LINE = "\n";

    private final static String TAB_SPACE = "\t";

    private final static String OPENING_BRACKET = "(";

    private final static String CLOSING_BRACKET = ")";

    private final static String CLOSING_AND_NEW_LINE = ")\n";

    /* Tabs */
    private final static String DOUBLE_TAB_SPACE = TAB_SPACE + TAB_SPACE;

    private final static String TRIPLE_TAB_SPACE = DOUBLE_TAB_SPACE + TAB_SPACE;

    private final static String QUADRO_TAB_SPACE = DOUBLE_TAB_SPACE + DOUBLE_TAB_SPACE;

    private final static String TRIPLE_TAB_OPENING_BRACKET_NEW_LINE = TRIPLE_TAB_SPACE+ OPENING_BRACKET + NEW_LINE;

    private final static String TRIPLE_TAB_CLOSING_BRACKET_NEW_LINE = TRIPLE_TAB_SPACE + CLOSING_AND_NEW_LINE;

    /* Element-specific */
    private final static String DOCUMENTATION_OPENING = "(documentation ";

    private final static String AP_OPENING = "(AP ";

    private final static String C_OPENING = "(C ";

    private final static String RDC_OPENING = "(RDC ";

    private final static String NRDC_OPENING = "(NRDC ";

    private final static String GOAL_OPENING = "(goal (";

    private final static String TRIGGER_OPENING = "(trigger (";

    private final static String CE_OPENING_LINE = DOUBLE_TAB_SPACE + "(elements " + NEW_LINE;

    private final static String DRIVES_OPENING_LINE = DOUBLE_TAB_SPACE + "(drives " + NEW_LINE;


    @Override
    public void writeFile(String filePath) {
        savePlan(filePath);
    }

    private void savePlan(String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);

            writer.append(OPENING_BRACKET);
            writer.append(NEW_LINE);

            writer.append(TAB_SPACE + DOCUMENTATION_OPENING + CLOSING_BRACKET);
            writer.append(NEW_LINE);

            for (ActionPattern actionPattern : Plan.getInstance().getActionPatterns()) {
                writer.append(TAB_SPACE).append(constructAPLine(actionPattern));
            }

            for (Competence competence : Plan.getInstance().getCompetences()) {
                String competenceLine = constructCompetenceLine(competence);
                writer.append(TAB_SPACE + competenceLine);
                writer.append(CE_OPENING_LINE);
                for (CompetenceElement competenceElement : competence.getCompetenceElements()) {
                    writer.append(TRIPLE_TAB_OPENING_BRACKET_NEW_LINE);
                    writer.append(QUADRO_TAB_SPACE).append(constructCELine(competenceElement));
                    writer.append(TRIPLE_TAB_CLOSING_BRACKET_NEW_LINE);
                }
                writer.append(DOUBLE_TAB_SPACE + CLOSING_AND_NEW_LINE);
                writer.append(TAB_SPACE + CLOSING_AND_NEW_LINE);
            }

            for (DriveCollection driveCollection : Plan.getInstance().getDriveCollections()) {
                writer.append(TAB_SPACE + constructDriveLine(driveCollection));
                writer.append(DRIVES_OPENING_LINE);
                for (DriveElement driveElement : driveCollection.getDriveElements()) {
                    writer.append(TRIPLE_TAB_OPENING_BRACKET_NEW_LINE);
                    writer.append(QUADRO_TAB_SPACE).append(constructDELine(driveElement));
                    writer.append(TRIPLE_TAB_CLOSING_BRACKET_NEW_LINE);
                }
                writer.append(DOUBLE_TAB_SPACE + CLOSING_AND_NEW_LINE);
                writer.append(TAB_SPACE + CLOSING_AND_NEW_LINE);
            }

            writer.append(CLOSING_BRACKET);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String constructDELine(DriveElement driveElement) {
        return OPENING_BRACKET+ driveElement.getNameOfElement() + EMPTY_SPACE + constructTriggers(driveElement.getSenses())
                + EMPTY_SPACE + driveElement.getTriggeredElement() +OPENING_BRACKET +
                contructTime(driveElement.getFrequencyUnit().toLowerCase(), String.valueOf(driveElement.getFrequencyValue()))
                + CLOSING_AND_NEW_LINE;
    }

    private String constructDriveLine(DriveCollection driveCollection) {
        return constructRT(driveCollection.isRealTime()) + driveCollection.getNameOfElement() + EMPTY_SPACE + constructGoalsForDrive(driveCollection);
    }

    private String constructRT(boolean isRealTime) {
        if (isRealTime){
            return RDC_OPENING;
        }
        else
        {
            return NRDC_OPENING;
        }
    }

    private String constructGoalsForDrive(DriveCollection driveCollection) {
        List<Sense> goals = driveCollection.getGoals();
        String goalsLine = GOAL_OPENING;
        goalsLine = constructSensesLine(goals, goalsLine);

        return goalsLine;
    }

    private String constructCELine(CompetenceElement competenceElement) {
        return OPENING_BRACKET + competenceElement.getNameOfElement() + EMPTY_SPACE +
                constructTriggers(competenceElement.getSenses()) + EMPTY_SPACE +
                competenceElement.getTriggeredElement() + EMPTY_SPACE
                + competenceElement.getRetries() + CLOSING_AND_NEW_LINE;
    }

    private String constructTriggers(List<Sense> senses) {
        String triggersLine = TRIGGER_OPENING;
        triggersLine = constructSensesLine(senses, triggersLine);

        return triggersLine;
    }

    private String constructCompetenceLine(Competence competence) {
        return C_OPENING + competence.getNameOfElement() + EMPTY_SPACE +
                contructTime(competence.getTimeUnits().toString().toLowerCase(), String.valueOf(competence.getTimeout())) + EMPTY_SPACE
                + constructGoalsForC(competence) + NEW_LINE;
    }

    private String constructGoalsForC(Competence competence) {
        List<Sense> goals = competence.getGoals();
        String goalsLine = GOAL_OPENING;
        goalsLine = constructSensesLine(goals, goalsLine);

        return goalsLine;
    }

    private String constructSensesLine(List<Sense> senses, String line) {
        if (senses.size() > 0) {
            for (Sense sense : senses) {
                line = line + OPENING_BRACKET + sense.getNameOfElement() + EMPTY_SPACE +
                        constructValuePredicate(sense) + CLOSING_BRACKET;
            }
            line = line + CLOSING_BRACKET + CLOSING_BRACKET;
        } else {
            line = line + CLOSING_BRACKET;
        }
        return line;
    }

    private String constructValuePredicate(Sense sense) {
        if (sense.getValue() != null && !sense.getValue().isEmpty() )
            return sense.getValue() + EMPTY_SPACE + sense.getComparator();
        else
            return "";
    }

    private String constructAPLine(ActionPattern actionPattern) {
        return AP_OPENING + actionPattern.getNameOfElement() + EMPTY_SPACE +
                contructTime(actionPattern.getTimeUnits().toString().toLowerCase(), String.valueOf(actionPattern.getTimeValue())) + EMPTY_SPACE
                + constructActionsStringForAP(actionPattern) + CLOSING_AND_NEW_LINE;
    }

    private String constructActionsStringForAP(ActionPattern actionPattern) {
        String actionsLine = OPENING_BRACKET;
        List<ActionEvent> actionEvents = actionPattern.getActionEvents();
        for (int i = 0; i < actionEvents.size(); i++) {
            actionsLine = actionsLine + actionEvents.get(i).getNameOfElement();
            if (i < actionEvents.size() - 1) {
                actionsLine = actionsLine + EMPTY_SPACE;
                System.out.println(actionsLine);
            }
        }
        actionsLine = actionsLine + CLOSING_BRACKET;

        return actionsLine;
    }

    private String contructTime(String timeUnits, String timeValue) {
        return OPENING_BRACKET + timeUnits + EMPTY_SPACE
                + timeValue + CLOSING_BRACKET;
    }
}