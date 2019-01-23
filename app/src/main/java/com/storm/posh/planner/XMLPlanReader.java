package com.storm.posh.planner;

import android.util.Log;

import com.storm.posh.planner.planelements.Action;
import com.storm.posh.planner.planelements.ActionPattern;
import com.storm.posh.planner.planelements.Competence;
import com.storm.posh.planner.planelements.CompetenceElement;
import com.storm.posh.planner.planelements.DriveCollection;
import com.storm.posh.planner.planelements.DriveElement;
import com.storm.posh.planner.planelements.Goal;
import com.storm.posh.planner.planelements.Plan;
import com.storm.posh.planner.planelements.Sense;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLPlanReader {
    private static final String TAG = XMLPlanReader.class.getSimpleName();

    // We don't use namespaces
    private static final String ns = null;

    public Plan readFile(XmlPullParser parser) {
        try {
            parser.next();
            parser.next();
            return readPlan(parser);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return new Plan();
    }

    private Plan readPlan(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading plan");
        List actionPatterns = new ArrayList();
        List competenceElements = new ArrayList();
        List competences = new ArrayList();
        List driveElements = new ArrayList();
        List driveCollections = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "Plan");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("Plan"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readPlan: found %s", tag));

                if (tag.equals("ActionPatterns")) {
                    actionPatterns = readActionPatterns(parser);
                } else if (tag.equals("CompetenceElements")) {
                    competenceElements = readCompetenceElements(parser);
                } else if (tag.equals("Competences")) {
                    competences = readCompetences(parser);
                } else if (tag.equals("DriveElements")) {
                    driveElements = readDriveElements(parser);
                } else if (tag.equals("Drives")) {
                    driveCollections = readDriveCollections(parser);
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }


        Plan plan = new Plan(actionPatterns, competenceElements, competences, driveElements, driveCollections);

        return plan;
    }


    private List readActionPatterns(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading action patterns");
        List actionPatterns = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "ActionPatterns");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("ActionPatterns"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readActionPatterns: found %s", tag));

                if (tag.equals("ActionPattern")) {
                    actionPatterns.add(readActionPattern(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return actionPatterns;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private ActionPattern readActionPattern(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading action pattern");
        parser.require(XmlPullParser.START_TAG, ns, "ActionPattern");
        String actionPatternName = parser.getAttributeValue(null, "name");
        List actions = new ArrayList();

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("ActionPattern"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readActionPattern: found %s", tag));

                if (tag.equals("Action")) {
                    actions.add(readAction(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return new ActionPattern(actionPatternName, actions);
    }

    private Action readAction(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading action");
        parser.require(XmlPullParser.START_TAG, ns, "Action");
        String name = parser.getAttributeValue(null, "name");

        Double timeToComplete = 0.0;
        String actionTimeToComplete = parser.getAttributeValue(null, "timeToComplete");

        if (actionTimeToComplete != null) {
            timeToComplete = Double.valueOf(actionTimeToComplete);
        }

        return new Action(name, timeToComplete);
    }

    private List readCompetenceElements(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading competence elements");
        List competenceElements = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "CompetenceElements");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("CompetenceElements"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readCompetenceElements: found %s", tag));

                if (tag.equals("CompetenceElement")) {
                    competenceElements.add(readCompetenceElement(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return competenceElements;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private CompetenceElement readCompetenceElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading competence element");
        parser.require(XmlPullParser.START_TAG, ns, "CompetenceElement");

        String competenceElementName = parser.getAttributeValue(null, "name");
        String triggerableName = parser.getAttributeValue(null, "triggers");
        List senses = new ArrayList();

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("CompetenceElement"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readCompetenceElement: found %s", tag));

                if (tag.equals("Sense")) {
                    senses.add(readSense(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return new CompetenceElement(competenceElementName, senses, triggerableName);
    }

    private List readSenses(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading senses");
        List senses = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "Senses");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("Senses"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readSenses: found %s", tag));

                if (tag.equals("Sense")) {
                    senses.add(readSense(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return senses;
    }

    private Sense readSense(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading sense");
        parser.require(XmlPullParser.START_TAG, ns, "Sense");

        String name = parser.getAttributeValue(null, "name");
        String value = parser.getAttributeValue(null, "value");
        String comparator = parser.getAttributeValue(null, "comparator");

        return new Sense(name, value, comparator);
    }

    private List readCompetences(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading competences");
        List competences = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "Competences");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("Competences"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readCompetences: found %s", tag));

                if (tag.equals("Competence")) {
                    competences.add(readCompetence(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return competences;
    }

    private Competence readCompetence(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading competence");
        parser.require(XmlPullParser.START_TAG, ns, "Competence");

        String competenceName = parser.getAttributeValue(null, "name");
        List<Goal> goals = new ArrayList<>();
        List<CompetenceElement> competenceElements = new ArrayList<>();

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("Competence"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readCompetence: found %s", tag));

                if (tag.equals("Goals")) {
                    goals = readGoals(parser);
                } else if (tag.equals("CompetenceElements")) {
                    competenceElements = readCompetenceElements(parser);
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return new Competence(competenceName, goals, competenceElements);
    }

    private List readGoals(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading goals");
        List goals = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "Goals");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("Goals"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readGoals: found %s", tag));

                if (tag.equals("Goal")) {
                    goals.add(readGoal(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return goals;
    }

    private Goal readGoal(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading goal");
        parser.require(XmlPullParser.START_TAG, ns, "Goal");

        String name = parser.getAttributeValue(null, "name");
        String value = parser.getAttributeValue(null, "value");
        String comparator = parser.getAttributeValue(null, "comparator");

        return new Goal(name, value, comparator);
    }

    private List readDriveElements(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading drive elements");
        List driveElements = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "DriveElements");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("DriveElements"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readDriveElements: found %s", tag));

                if (tag.equals("DriveElement")) {
                    driveElements.add(readDriveElement(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return driveElements;
    }

    private DriveElement readDriveElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading drive element");
        parser.require(XmlPullParser.START_TAG, ns, "DriveElement");

        String driveElementName = parser.getAttributeValue(null, "name");
        String checkTime = parser.getAttributeValue(null, "checkTime");
        String triggerableName = parser.getAttributeValue(null, "triggers");
        List<Sense> senses = new ArrayList();

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("DriveElement"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readDriveElement: found %s", tag));

                if (tag.equals("Senses")) {
                    senses = readSenses(parser);
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return new DriveElement(driveElementName, senses, checkTime, triggerableName);
    }

    private List readDriveCollections(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading drives");
        List driveCollections = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "Drives");

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("Drives"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readDrives: found %s", tag));

                if (tag.equals("Drive")) {
                    driveCollections.add(readDriveCollection(parser));
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return driveCollections;
    }

    private DriveCollection readDriveCollection(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(TAG, "Reading drive");
        parser.require(XmlPullParser.START_TAG, ns, "Drive");

        String driveCollectionName = parser.getAttributeValue(null, "name");
        String drivePriority = parser.getAttributeValue(null, "priority");
        List<Sense> senses = new ArrayList();
        List<DriveElement> driveElements = new ArrayList();

        Integer priority = 0;
        if (drivePriority != null) {
            priority = Integer.valueOf(drivePriority);
        }

        String tag = parser.getName();
        int eventType = parser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && tag.equals("Drive"))) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d(TAG, String.format("readDriveCollection: found %s", tag));

                if (tag.equals("Senses")) {
                    senses = readSenses(parser);
                } else if (tag.equals("DriveElements")) {
                    driveElements = readDriveElements(parser);
                }
            }

            eventType = parser.next();
            tag = parser.getName();
        }

        return new DriveCollection(driveCollectionName, senses, driveElements, priority);
    }
}

/*
using System.Collections;
using System.Collections.Generic;
using System.Xml;
using UnityEngine;

public class XMLPlanReader
{
    internal List<DriveCollection> ReadFile(TextAsset plan)
    {
        XmlDocument xmlDoc = new XmlDocument();
        xmlDoc.LoadXml(plan.text);
        XmlNode planRoot = xmlDoc.FirstChild;

        List<ActionPattern> actionPatterns = ExtractActionPatterns(xmlDoc);
        List<CompetenceElement> competenceElements = ExtractCompetenceElements(xmlDoc);
        List<Competence> competences = ExtractCompetences(xmlDoc, competenceElements);
        List<DriveElement> driveElements = ExtractDriveElements(xmlDoc);
        List<DriveCollection> drives = ExtractDrives(xmlDoc, driveElements);

        LinkCompetenceElements(ref actionPatterns, ref competenceElements, ref competences);
        LinkDriveElements(ref actionPatterns, ref competences, ref driveElements);

        return drives;
    }


    private List<ActionPattern> ExtractActionPatterns(XmlDocument xmlDoc)
    {
        List<ActionPattern> actionPatterns = new List<ActionPattern>();

        XmlNode actionPatternsNode = xmlDoc.GetElementsByTagName("ActionPatterns").Item(0);
        foreach (XmlNode actionPatternNode in actionPatternsNode.ChildNodes)
        {
            actionPatterns.Add(new ActionPattern(actionPatternNode.Attributes["name"].Value,
                ExtractActions(actionPatternNode)));
        }

        return actionPatterns;
    }

    private List<Action> ExtractActions(XmlNode actionPatternNode)
    {
        List<Action> actions = new List<Action>();

        double timeToComplete = 0;

        foreach (XmlNode actionNode in actionPatternNode.ChildNodes)
        {
            if ((actionNode.Attributes["timeToComplete"]) != null)
            {
                timeToComplete = System.Double.Parse(actionNode.Attributes["timeToComplete"].Value);
            } else
            {
                timeToComplete = 0;
            }
            actions.Add(new Action(actionNode.Attributes["name"].Value,
               timeToComplete));
        }

        return actions;
    }

    private List<Competence> ExtractCompetences(XmlDocument xmlDoc, List<CompetenceElement> allCompetenceElements)
    {
        List<Competence> competences = new List<Competence>();

        XmlNode competencesNode = xmlDoc.GetElementsByTagName("Competences").Item(0);
        foreach (XmlNode competenceNode in competencesNode.ChildNodes)
        {
            List<CompetenceElement> competenceElements = new List<CompetenceElement>();

            List<string> competenceElementsNames = ExtractCompetenceElementsNames(competenceNode);
            foreach (CompetenceElement competenceElement in allCompetenceElements)
            {
                foreach (string nameCE in competenceElementsNames)
                {
                    if (competenceElement.Name.Equals(nameCE))
                    {

                        competenceElements.Add(competenceElement);
                    }
                }
            }

            competences.Add(new Competence(competenceNode.Attributes["name"].Value,
                ExtractSenses(competenceNode), competenceElements
                ));
        }

        return competences;
    }

    private List<string> ExtractCompetenceElementsNames(XmlNode competenceNode)
    {
        List<string> competenceElements = new List<string>();

        XmlNode competenceElementsNode = competenceNode.ChildNodes.Item(1);
        foreach (XmlNode competenceElementNode in competenceElementsNode.ChildNodes)
        {
            competenceElements.Add(competenceElementNode.Attributes["name"].Value);
        }

        return competenceElements;
    }

    private List<CompetenceElement> ExtractCompetenceElements(XmlDocument xmlDoc)
    {
        List<CompetenceElement> competenceElements = new List<CompetenceElement>();

        XmlNode competenceElementsNode = xmlDoc.GetElementsByTagName("CompetenceElements").Item(0);
        foreach (XmlNode competenceElementNode in competenceElementsNode.ChildNodes)
        {
            competenceElements.Add(new CompetenceElement(competenceElementNode.Attributes["name"].Value,
                ExtractSenses(competenceElementNode),
                new Action(competenceElementNode.Attributes["triggers"].Value)));
        }

        return competenceElements;
    }

    private List<Sense> ExtractSenses(XmlNode competenceElementNode)
    {
        List<Sense> Senses = new List<Sense>();

        foreach (XmlNode Sense in competenceElementNode.ChildNodes.Item(0).ChildNodes)
        {
            Senses.Add(new Sense(Sense.Attributes["name"].Value.ToString(),
                System.Double.Parse(Sense.Attributes["value"].Value),
                Sense.Attributes["comperator"].Value.ToString()));
        }

        return Senses;
    }

    private List<DriveElement> ExtractDriveElements(XmlDocument xmlDoc)
    {
        List<DriveElement> driveElements = new List<DriveElement>();

        XmlNode driveElementsNode = xmlDoc.GetElementsByTagName("DriveElements").Item(0);
        foreach (XmlNode driveElementNode in driveElementsNode.ChildNodes)
        {
            driveElements.Add(new DriveElement(driveElementNode.Attributes["name"].Value,
                ExtractSenses(driveElementNode),
                new Action(driveElementNode.Attributes["triggers"].Value),
               float.Parse(driveElementNode.Attributes["checkTime"].Value)));
        }

        return driveElements;
    }

    private List<DriveCollection> ExtractDrives(XmlDocument xmlDoc, List<DriveElement> allDriveElements)
    {
        List<DriveCollection> driveCollections = new List<DriveCollection>();

        XmlNode drivesNode = xmlDoc.GetElementsByTagName("Drives").Item(0);

        foreach (XmlNode driveNode in drivesNode.ChildNodes)
        {
            List<string> driveElementNames = ExtractDriveElementsNames(driveNode);
            List<DriveElement> driveElements = new List<DriveElement>();

            foreach (DriveElement driveElement in allDriveElements)
            {
                foreach (string nameDE in driveElementNames)
                {
                    if (driveElement.Name.Equals(nameDE))
                    {
                        driveElements.Add(driveElement);
                    }
                }
            }
            int priority = int.Parse(driveNode.Attributes["priority"].Value);
            driveCollections.Add(new DriveCollection(driveNode.Attributes["name"].Value,
                ExtractSenses(driveNode), driveElements, priority));
        }

        return driveCollections;
    }

    private List<string> ExtractDriveElementsNames(XmlNode driveNode)
    {
        List<string> driveElements = new List<string>();
        XmlNode driveElementsNode = driveNode.ChildNodes.Item(1);
        foreach (XmlNode competenceElementNode in driveElementsNode.ChildNodes)
        {
            driveElements.Add(competenceElementNode.Attributes["name"].Value);
        }

        return driveElements;
    }

}
 */