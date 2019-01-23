package com.storm.posh.planner.planelements;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Plan {
    public String name;
    public List<ActionPattern> actionPatterns;
    public List<CompetenceElement> competenceElements;
    public List<Competence> competences;
    public List<DriveElement> driveElements;
    public List<DriveCollection> driveCollections;

    private static final String TAG = Plan.class.getSimpleName();

    @Override
    public String toString() {
        return "Plan {" +
                " name='" + name + '\'' +
                " actionPatterns=" + Objects.toString(actionPatterns)+
                " competenceElements=" + Objects.toString(competenceElements)+
                " competences=" + Objects.toString(competences)+
                " driveElements=" + Objects.toString(driveElements)+
                " driveCollections=" + Objects.toString(driveCollections)+
                " }";
    }

    public Plan() {
        this.name = "Broken Plan!";
    }

    public Plan(List actionPatterns, List competenceElements, List competences, List driveElements, List driveCollections) {
        this.name = "The Plan";
        this.actionPatterns = actionPatterns;
        this.competenceElements = competenceElements;
        this.competences = competences;
        this.driveElements = driveElements;
        this.driveCollections = driveCollections;
    }

    public void linkCompetenceElements() {
        Log.d(TAG, "Linking competence elements");
        elementLoop: for (CompetenceElement competenceElement : competenceElements) {
            Log.d(TAG, competenceElement.name);
            if (competenceElement.triggerableName == null) {
                Log.d(TAG, "... has no trigger");
                continue;
            } else {
                Log.d(TAG, "triggers: "+competenceElement.triggerableName);
            }

            Log.d(TAG, "action patterns");
            for (ActionPattern actionPattern : actionPatterns) {
                if (competenceElement.triggerableName.equals(actionPattern.name)) {
                    competenceElement.triggerableElement = actionPattern;
                    Log.d(TAG, String.format("matched! %s", actionPattern.toString()));
                    continue elementLoop;
                }
            }
            Log.d(TAG, "no action pattern match");

            Log.d(TAG, "competences");
            for (Competence competence : competences) {
                if (competenceElement.triggerableName.equals(competence.name)) {
                    competenceElement.triggerableElement = competence;
                    Log.d(TAG, String.format("matched! %s", competence.toString()));
                    continue elementLoop;
                }
            }
            Log.d(TAG, "no competences match");
        }
    }

    public void linkCompetences() {
        Log.d(TAG, "Linking competences");
        for (Competence competence : competences) {
            Log.d(TAG, competence.name);

            List<CompetenceElement> actualCompetenceElements = new ArrayList();

            if (competence.competenceElements.size() == 0) {
                Log.d(TAG, "... has no elements!");
                continue;
            } else {
                Log.d(TAG, String.format("... has %d elements!", competence.competenceElements.size()));
            }

            competenceLoop: for (CompetenceElement tempCompetenceElement : competence.competenceElements) {
                for (CompetenceElement competenceElement : competenceElements) {
                    if (competenceElement.name.equals(tempCompetenceElement.name)) {
                        actualCompetenceElements.add(competenceElement);
                        Log.d(TAG, "Matched "+competenceElement.name);
                        continue competenceLoop;
                    }
                }

                Log.d(TAG, "Did not match "+tempCompetenceElement.name);
            }

            competence.competenceElements = actualCompetenceElements;

        }
    }

    public void linkDriveElements() {
        Log.d(TAG, "Linking drive elements");
        elementLoop: for (DriveElement driveElement : driveElements) {
            Log.d(TAG, driveElement.name);
            if (driveElement.triggerableName == null) {
                Log.d(TAG, "... has no trigger");
                continue;
            } else {
                Log.d(TAG, "triggers: "+driveElement.triggerableName);
            }

            Log.d(TAG, "action patterns");
            for (ActionPattern actionPattern : actionPatterns) {
                if (driveElement.triggerableName.equals(actionPattern.name)) {
                    driveElement.triggerableElement = actionPattern;
                    Log.d(TAG, "matched!");
                    continue elementLoop;
                }
            }
            Log.d(TAG, "no action pattern match");

            Log.d(TAG, "competences");
            for (Competence competence : competences) {
                if (driveElement.triggerableName.equals(competence.name)) {
                    driveElement.triggerableElement = competence;
                    Log.d(TAG, "matched!");
                    continue elementLoop;
                }
            }
            Log.d(TAG, "no competences match");
        }
    }

    public void linkDriveCollections() {
        Log.d(TAG, "Linking drive collections");
        for (DriveCollection driveCollection : driveCollections) {
            Log.d(TAG, driveCollection.name);

            List<DriveElement> actualDriveElements = new ArrayList();

            Log.d(TAG, "drive elements");
            driveLoop: for (DriveElement tempDriveElement : driveCollection.driveElements) {
                for (DriveElement driveElement : driveElements) {
                    if (driveElement.name.equals(tempDriveElement.name)) {
                        actualDriveElements.add(driveElement);
                        Log.d(TAG, "Matched "+driveElement.name);
                        continue driveLoop;
                    }
                }

                Log.d(TAG, "Did not match "+tempDriveElement.name);
            }

            driveCollection.driveElements = actualDriveElements;
        }
    }

    public void prioritiseDrives() {
        class SortByPriority implements Comparator<DriveCollection> {
            public int compare(DriveCollection a, DriveCollection b) {
                return a.priority - b.priority;
            }
        }

        Collections.sort(driveCollections, new SortByPriority());
    }
}

/*
using System.Collections.Generic;

public class PlanElement
{
    private string name = "";
    internal string Name
    {
        get
        {
            return name;
        }
    }

    public PlanElement(string name)
    {
        this.name = name;
    }
}
 */