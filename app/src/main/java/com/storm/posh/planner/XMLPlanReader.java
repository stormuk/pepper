package com.storm.posh.planner;

import android.content.res.XmlResourceParser;
import android.util.Xml;

import com.storm.posh.planner.planelements.Action;
import com.storm.posh.planner.planelements.ActionPattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLPlanReader {
    // We don't use namespaces
    private static final String ns = null;

    public List readFile(XmlResourceParser plan) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser(plan);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
//            parser.setInput(in, null);
            parser.nextTag();
            return readPlan(parser);
        }
    }

    private List readPlan(XmlPullParser parser) throws XmlPullParserException, IOException {
        List actionPatterns = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "Plan");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("ActionPatterns")) {
                actionPatterns = readActionPatterns(parser);
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private List readActionPatterns(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "ActionPatterns");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            if (name.equals('ActionPattern')) {
                entries.add(readActionPattern(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private ActionPattern readActionPattern(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "ActionPattern");

        String name = parser.getAttributeValue(null, 'name');
        List actions = new ArrayList();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.getName().equals("Action")) {
                actions.add(readAction(parser));
            } else {
                skip(parser);
            }
        }

        return new ActionPattern(name, actions);
    }

    private Action readAction(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, 'Action');

        String name = parser.getAttributeValue(null, 'name');
        Double timeToComplete = parser.getAttributeValue(null, 'timeToComplete');

        return new Action(name, timeToComplete);
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
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

    private static void LinkCompetenceElements(ref List<ActionPattern> actionPatterns, ref List<CompetenceElement> competenceElements, ref List<Competence> competences)
    {
        foreach (CompetenceElement competenceElement in competenceElements)
        {
            bool elemFound = false;

            foreach (ActionPattern actionPattern in actionPatterns)
            {
                if (competenceElement.TriggerableElement.Name.Equals(actionPattern.Name))
                {
                    competenceElement.TriggerableElement = actionPattern;
                    elemFound = true; ;
                }
                if (elemFound)
                    break;
            }
            if (!elemFound)
            {
                foreach (Competence competence in competences)
                {
                    if (competenceElement.TriggerableElement.Name.Equals(competence.Name))
                    {
                        competenceElement.TriggerableElement = competence;
                        elemFound = true; ;
                    }
                    if (elemFound)
                        break;
                }
            }
        }
    }

    private static void LinkDriveElements(ref List<ActionPattern> actionPatterns, ref List<Competence> competences, ref List<DriveElement> driveElements)
    {
        foreach (DriveElement driveElement in driveElements)
        {
            bool elemFound = false;

            foreach (ActionPattern actionPattern in actionPatterns)
            {
                if (driveElement.TriggerableElement.Name.Equals(actionPattern.Name))
                {
                    driveElement.TriggerableElement = actionPattern;
                    elemFound = true; ;
                }
                if (elemFound)
                    break;
            }
            if (!elemFound)
            {
                foreach (Competence competence in competences)
                {
                    if (driveElement.TriggerableElement.Name.Equals(competence.Name))
                    {
                        driveElement.TriggerableElement = competence;
                        elemFound = true; ;
                    }
                    if (elemFound)
                        break;
                }
            }
        }
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