package com.storm.posh.plan;

import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.action.ActionPattern;
import com.storm.posh.plan.planelements.competence.Competence;
import com.storm.posh.plan.planelements.competence.CompetenceElement;
import com.storm.posh.plan.planelements.drives.DriveCollection;
import com.storm.posh.plan.planelements.drives.DriveElement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: @Andreas.
 * Date : @07/01/2016
 */
public class Plan {

    private static Plan instance = null;

    private volatile List<Sense> senses = new LinkedList<>();

    private volatile List<ActionEvent> actionEvents = new LinkedList<>();

    private volatile List<ActionPattern> actionPatterns = new LinkedList<>();

    private volatile List<Competence> competences = new LinkedList<>();

    private volatile List<CompetenceElement> competenceElements = new LinkedList<>();

    private volatile List<DriveElement> driveElements = new LinkedList<>();

    private volatile List<DriveCollection> driveCollections = new LinkedList<>();

    private volatile DriveCollection currentDrive;

    private volatile PlanElement currentElement;

    private Plan() {
    }

    public static Plan getInstance() {
        if (instance == null) {
            instance = new Plan();
        }
        return instance;
    }

    public void reset() {
        this.currentDrive = null;
        this.currentElement = null;
    }

    public void cleanAllLists() {
        actionEvents.clear();
        actionPatterns.clear();
        competenceElements.clear();
        competences.clear();
        driveElements.clear();
        driveCollections.clear();
    }

    public ActionEvent createAction(String name) {
        ActionEvent action = findAction(name);
        if (action != null) {
            return action;
        }
        action = new ActionEvent(name);
        actionEvents.add(action);

        return action;
    }

    public Sense createSense(String name) {
        Sense sense = findSense(name);
        if (sense != null) {
            return sense;
        }
        sense = new Sense(name);
        senses.add(sense);

        return sense;
    }

    public Sense createSense(String name, String comparator, String value) {
        Sense sense = findSense(name);
        if (sense != null) {
            return sense;
        }
        sense = new Sense(name, comparator, value);
        senses.add(sense);

        return sense;
    }

    public void addActionPattern(ActionPattern action) {
        actionPatterns.add(action);
    }

    public void addCompetence(Competence competence) {
        competences.add(competence);
    }

    public void addCompetenceElement(CompetenceElement competenceElement) {
        competenceElements.add(competenceElement);
    }

    public void addDriveElement(DriveElement driveElement) {
        driveElements.add(driveElement);
    }

    public void addDriveCollection(DriveCollection driveCollection) {
        this.driveCollections.add(driveCollection);
    }

    public List<DriveCollection> getDriveCollections() {
        return driveCollections;
    }

    public List<DriveElement> getDriveElements() {
        return driveElements;
    }

    public List<CompetenceElement> getCompetenceElements() {
        return competenceElements;
    }

    public List<Competence> getCompetences() {
        return competences;
    }

    public List<ActionPattern> getActionPatterns() {
        return actionPatterns;
    }

    public List<ActionEvent> getActionEvents() {
        return actionEvents;
    }

    public void removeDriveCollection(DriveCollection driveCollection) {
        driveCollections.remove(driveCollection);
    }

    public Sense findSense(String name) {
//        for (Sense sense : senses) {
//            if (sense.getNameOfElement().equals(name)) {
//                return sense;
//            }
//        }
        return null;
    }

    public ActionEvent findAction(String name) {
        for (ActionEvent actionEvent : actionEvents) {
            if (actionEvent.getNameOfElement().equals(name)) {
                return actionEvent;
            }
        }
        return null;
    }

    public PlanElement findActionPattern(String name) {
        for (ActionPattern actionsPattern : actionPatterns) {
            if (actionsPattern.getNameOfElement().equals(name)) {
                return actionsPattern;
            }
        }
        return null;
    }


    public Competence findCompetence(String name) {
        for (Competence competence : competences) {
            if (competence.getNameOfElement().equals(name)) {
                return competence;
            }
        }
        return null;
    }

    public CompetenceElement findCompetenceElement(String name) {
        for (Competence competence : competences) {
            for (CompetenceElement competenceElement : competence.getCompetenceElements()) {
                if (competenceElement.getNameOfElement().equals(name))
                    return competenceElement;
            }
        }
        return null;
    }

    public CompetenceElement findCompetenceElementXPOSH(String name) {
        for (CompetenceElement competenceElement : competenceElements) {
            if (competenceElement.getNameOfElement().equals(name))
                return competenceElement;
        }
        return null;
    }

    public DriveElement findDriveElementXPOSH(String name) {
        for (DriveElement driveElement : driveElements) {
            if (driveElement.getNameOfElement().equals(name))
                return driveElement;
        }
        return null;
    }

    public DriveCollection findDriveCollection(String name) {
        for (DriveCollection driveCollection : driveCollections) {
            if (driveCollection.getNameOfElement().equals(name)) {
                return driveCollection;
            }
        }
        return null;
    }

    public PlanElement findActionPatternOrCompetence(String name) {
        for (Competence competence : competences) {
            if (competence.getNameOfElement().equals(name)) {
                return competence;
            }
        }
        return findActionPattern(name);
    }

    public List<ActionPattern> findActionPatternsWithAction(String name) {
        List<ActionPattern> actionPatterns = new ArrayList<>();

        for (ActionPattern actionsPattern : actionPatterns) {
            for (ActionEvent actionEvent : actionsPattern.getActionEvents()) {
                if (actionEvent.getNameOfElement().equals(name)) {
                    actionPatterns.add(actionsPattern);
                }
            }
        }

        return actionPatterns;
    }

    public DriveCollection getCurrentDrive() {
        return currentDrive;
    }

    public void setCurrentDrive(DriveCollection currentDrive) {
        this.currentDrive = currentDrive;
    }

    public PlanElement getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(PlanElement currentElement) {
        this.currentElement = currentElement;
    }
}