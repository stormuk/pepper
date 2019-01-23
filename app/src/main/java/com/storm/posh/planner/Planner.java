package com.storm.posh.planner;

import android.content.res.XmlResourceParser;
import android.os.Debug;
import android.util.Log;

import com.storm.posh.planner.planelements.DriveCollection;
import com.storm.posh.planner.planelements.Plan;
import com.storm.posh.planner.planelements.Sense;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Planner {
    private static final String TAG = Planner.class.getSimpleName();
    private volatile Plan plan;

    public BehaviourLibrary behaviourLibrary;

    public void start(XmlResourceParser planFile) {
        Log.d(TAG, "Starting Planner");
        plan = new XMLPlanReader().readFile(planFile);
        Log.d(TAG, "Got plan:");
        Log.d(TAG, plan.toString());

        plan.linkCompetenceElements();
        plan.linkDriveElements();
        plan.linkDriveCollections();
        plan.prioritiseDrives();
    }

    public void update() {
        drivesHandler();
    }

    private void drivesHandler() {
        int currentPriority = -1;

        for (DriveCollection drive : plan.driveCollections) {
            // Avoid extra loops for lower priority items.
            if (currentPriority != -1) {
                if (currentPriority < drive.priority) {
                    continue;
                }
            }

            if (currentPriority == -1 || currentPriority == drive.priority) {
                if (drive.senses.size() != 0) {
                    int numSensesNeeded = 0;

                    for (Sense sense : drive.senses) {
                        numSensesNeeded = checkSense(numSensesNeeded, sense);
                    }

                    if (numSensesNeeded == drive.senses.size()) {
//                        ABOD3_Bridge.getInstance().alertForElement(drive.name, "D");
                        driveElementsHandler(drive.driveElements);
                        currentPriority = drive.priority;
                    }

                } else {
//                        ABOD3_Bridge.getInstance().alertForElement(drive.name, "D");
                    driveElementsHandler(drive.driveElements);
                    currentPriority = drive.priority;
                }
            }
        }
    }

    private void driveElementsHandler(List driveElements) {

    }

    private int checkSense(int numTriggersTrue, Sense sense) {
        switch (sense.comparator) {
            case "bool":
                if (SenseIsBool(sense)) {
                    numTriggersTrue = numTriggersTrue + 1;
                }
                break;

            case "=":
                if (SenseIsEqual(sense)) {
                    numTriggersTrue = numTriggersTrue + 1;
                }
                break;

            case "<":
                if (SenseIsLessThan(sense)) {
                    numTriggersTrue = numTriggersTrue + 1;
                }
                break;

            case "<=":
                if (SenseIsLessThanOrEqual(sense)) {
                    numTriggersTrue = numTriggersTrue + 1;
                }
                break;

            case ">=":
                if (SenseIsGreaterThanOrEqual(sense)) {
                    numTriggersTrue = numTriggersTrue + 1;
                }
                break;

            case ">":
                if (SenseIsGreaterThan(sense)) {
                    numTriggersTrue = numTriggersTrue + 1;
                }
                break;

            default:
                break;
        }

        return numTriggersTrue;
    }

    private boolean SenseIsBool(Sense sense) {
        if (sense.value == 1) {
            if (behaviourLibrary.checkBooleanSense(sense)) {
                return true;
            }
        } else if (sense.value == 0) {
            if (!behaviourLibrary.checkBooleanSense(sense)) {
                return true;
            }
        } else {
            Log.d(TAG, "Sense: "+sense.toString()+" expected output should be boolean 0 or 1");
        }

        return false;
    }

    private boolean SenseIsEqual(Sense sense) {
        return (behaviourLibrary.checkDoubleSense(sense) == sense.value);
    }

    private boolean SenseIsLessThan(Sense sense) {
        return (behaviourLibrary.checkDoubleSense(sense) < sense.value);
    }

    private boolean SenseIsLessThanOrEqual(Sense sense) {
        return (behaviourLibrary.checkDoubleSense(sense) <= sense.value);
    }

    private boolean SenseIsGreaterThan(Sense sense) {
        return (behaviourLibrary.checkDoubleSense(sense) > sense.value);
    }
    private boolean SenseIsGreaterThanOrEqual(Sense sense) {
        return (behaviourLibrary.checkDoubleSense(sense) >= sense.value);
    }

}

/*
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Planner : MonoBehaviour
{
    private List<DriveCollection> drives = new List<DriveCollection>();

    public int botNumber = 0;
    public static int botCount = 0;

    public BehaviourLibraryLinker behaviourLibrary;

    public TextAsset planFile;

    void Awake()
    {
    }

    void Start()
    {
        behaviourLibrary.Start();

        botCount++;
        botNumber = botCount;

       drives = new XMLPlanReader().ReadFile(planFile);
        drives.Sort((x, y) => x.Priority.CompareTo(y.Priority));
    }

    public Vector3 directionTestVector3;

    void Update()
    {
        DrivesHandler();
    }

    public void ReadPlan(TextAsset planFile)
    {
        this.planFile = planFile;
        drives = new XMLPlanReader().ReadFile(planFile);
        drives.Sort((x,y) => x.Priority.CompareTo(y.Priority));
    }

    public void ChangeBehaviourLibrary(BehaviourLibraryLinker behaviourLibrary)
    {
        System.Type type = behaviourLibrary.GetType();
        Component copy = gameObject.AddComponent(type);
        Destroy(GetComponent(this.behaviourLibrary.GetType()));
        this.behaviourLibrary = (BehaviourLibraryLinker) gameObject.GetComponent(type);
    }

    public void ChangeNav(NavmeshController navmeshController)
    {
        Destroy(GetComponent(GetComponent<BehaviourLibraryLinker>().NavAgent.GetType()));

        System.Type type = navmeshController.GetType();
        Component copy = gameObject.AddComponent(type);

        gameObject.GetComponent<BehaviourLibraryLinker>().navAgent =  (NavmeshController) copy;
    }

    public void MakeSelectedAgent()
    {
        ABOD3_Bridge.GetInstance().ChangeSelectedBot(botNumber);
    }

    private void DrivesHandler()
    {
        int currentPriority = -1;

        foreach (DriveCollection drive in drives)
        {
            if (currentPriority != -1) // Avoid extra loops for lower priority items.
            {
                if (currentPriority < drive.Priority)
                    continue;
            }

            if (currentPriority == -1 || currentPriority == drive.Priority)
            {
                if (drive.Senses.Count != 0)
                {
                    int numSensesNeeded = 0;
                    foreach (Sense goal in drive.Senses)
                    {
                        numSensesNeeded = CheckSense(numSensesNeeded, goal);
                    }
                    if (numSensesNeeded == drive.Senses.Count)
                    {
                        ABOD3_Bridge.GetInstance().AletForElement(botNumber, drive.Name, "D");
                        DriveElementsHandler(drive.DriveElements);
                        currentPriority = drive.Priority;
                    }
                }
                else
                {
                    ABOD3_Bridge.GetInstance().AletForElement(botNumber, drive.Name, "D");
                    DriveElementsHandler(drive.DriveElements);
                    currentPriority = drive.Priority;
                }
            }
        }
    }

    private void DriveElementsHandler(List<DriveElement> driveElements)
    {
        foreach (DriveElement driveElement in driveElements)
        {
            if (Time.time >= driveElement.NextCheck)
            {
                driveElement.UpdateNextCheck();
                int numSensesNeeded = 0;
                foreach (Sense trigger in driveElement.Senses)
                {
                    numSensesNeeded = CheckSense(numSensesNeeded, trigger);
                }
                if (numSensesNeeded == driveElement.Senses.Count)
                {
                    ABOD3_Bridge.GetInstance().AletForElement(botNumber, driveElement.Name, "DE");
                    PlanElement elementToBeTriggered = driveElement.TriggerableElement;
                    if (elementToBeTriggered is Competence)
                    {
                        CompetenceHandler((Competence)elementToBeTriggered);
                    }
                    else if (elementToBeTriggered is ActionPattern)
                    {
                        ActionPatternHandler((ActionPattern)elementToBeTriggered);
                    }
                    else if (elementToBeTriggered is Action)
                    {
                        TriggerAction((Action)elementToBeTriggered);
                    }
                }
            }
        }
    }

    private int CheckSense(int numTriggersTrue, Sense Sense)
    {
        switch (Sense.Comperator)
        {
            case "bool":
                if (SenseIsBool(Sense))
                    numTriggersTrue = numTriggersTrue + 1;
                break;
            case "=":
                if (SenseIsEqual(Sense))
                    numTriggersTrue = numTriggersTrue + 1;
                break;
            case "<":
                if (SenseIsLessThan(Sense))
                    numTriggersTrue = numTriggersTrue + 1;
                break;
            case "<=":
                if (SenseIsLessThanAndEqual(Sense))
                    numTriggersTrue = numTriggersTrue + 1;
                break;
            case ">":
                if (SenseIsGreaterThan(Sense))
                    numTriggersTrue = numTriggersTrue + 1;
                break;
            case ">=":
                if (SenserIsGreaterThanAndEqual(Sense))
                    numTriggersTrue = numTriggersTrue + 1;
                break;
            default:
                break;
        }

        return numTriggersTrue;
    }


    private void CompetenceHandler(Competence competence)
    {
        Sense goal = competence.Goals[0];

        if (CheckSense(0, goal) == 0)
        {
            ABOD3_Bridge.GetInstance().AletForElement(botNumber, competence.Name, "C");

            int numCEActivated = 0;
            foreach (CompetenceElement competenceElement in competence.Elements)
            {
                if (CompetenceElementsHandler(competenceElement))
                {
                    numCEActivated = numCEActivated + 1;
                }
            }
        }
    }
    private bool CompetenceElementsHandler(CompetenceElement competenceElement)
    {
        int numSensesNeeded = 0;
        foreach (Sense sense in competenceElement.Senses)
        {
            numSensesNeeded = CheckSense(numSensesNeeded, sense);
        }
        if (numSensesNeeded == competenceElement.Senses.Count)
        {
            ABOD3_Bridge.GetInstance().AletForElement(botNumber, competenceElement.Name, "CE");

            PlanElement elementToBeTriggered = competenceElement.TriggerableElement;
            if (elementToBeTriggered is Competence)
            {
                CompetenceHandler((Competence)elementToBeTriggered);
            }
            else if (elementToBeTriggered is ActionPattern)
            {
                ActionPatternHandler((ActionPattern)elementToBeTriggered);
            }
            else
            {
                TriggerAction((Action)elementToBeTriggered);
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private void ActionPatternHandler(ActionPattern actionPattern)
    {
        ABOD3_Bridge.GetInstance().AletForElement(botNumber, actionPattern.Name, "AP");
        StartCoroutine(ExecuteActionPattern(actionPattern, 0));
    }

    IEnumerator ExecuteActionPattern(ActionPattern actionPattern, int currentActionindex)
    {
        if (currentActionindex < actionPattern.Actions.Count)
        {
            TriggerAction(actionPattern.Actions[currentActionindex]);
            yield return new WaitForSeconds((float)
                actionPattern.Actions[currentActionindex].TimeToComplete);

            //  DrivesHandler();

            StartCoroutine(ExecuteActionPattern(actionPattern, currentActionindex + 1));
        }
        else
            yield return new WaitForSeconds(0);
    }

    private void TriggerAction(Action action)
    {
        ABOD3_Bridge.GetInstance().AletForElement(botNumber, action.Name, "A");
        try
        {
            behaviourLibrary.ExecuteAction(action);
        }
        catch (System.Exception error)
        {
            Debug.LogError("Actions: " + action.Name + " produced error: " + error);
        }
    }
}
 */