package com.storm.posh.planner;

import android.content.res.XmlResourceParser;
import android.util.Log;

import com.storm.posh.planner.planelements.Action;
import com.storm.posh.planner.planelements.ActionPattern;
import com.storm.posh.planner.planelements.Competence;
import com.storm.posh.planner.planelements.CompetenceElement;
import com.storm.posh.planner.planelements.DriveCollection;
import com.storm.posh.planner.planelements.DriveElement;
import com.storm.posh.planner.planelements.Plan;
import com.storm.posh.planner.planelements.PlanElement;
import com.storm.posh.planner.planelements.Sense;

import java.util.Calendar;
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
        plan.linkCompetences();
        plan.linkDriveElements();
        plan.linkDriveCollections();
        plan.prioritiseDrives();
    }

    public boolean update() {
        return drivesHandler();
    }

    public void reset() {
        behaviourLibrary.reset();
    }


    private boolean drivesHandler() {
        int validDrivesCount = plan.driveCollections.size();
        Log.d(TAG, String.format("Starting drives: %d", validDrivesCount));
        int currentPriority = -1;

        for (DriveCollection drive : plan.driveCollections) {
            Log.d(TAG, String.format("Considering: %s", drive.name));

            // Avoid extra loops for lower priority items.
            if (currentPriority != -1) {
                if (currentPriority < drive.priority) {
                    Log.d(TAG, "Priority too low");
                    continue;
                }
            }

            if (currentPriority == -1 || currentPriority == drive.priority) {
                if (drive.goals.size() != 0) {
                    Log.d(TAG, "Has goals, checking...");
                    int numGoalsMet = 0;

                    for (Sense goal : drive.goals) {
                        numGoalsMet = checkSense(numGoalsMet, goal);
                    }

                    if (numGoalsMet != drive.goals.size()) {
                        Log.d(TAG, "Goals unmet, running drive elements");
//                        ABOD3_Bridge.getInstance().alertForElement(drive.name, "D");
                        driveElementsHandler(drive.driveElements);
                        currentPriority = drive.priority;
                    } else {
                        Log.d(TAG, String.format("All goals met, skipping."));
                        validDrivesCount -= 1;
                    }

                } else {
                    Log.d(TAG, "No goals to meet, running drive elements");
//                        ABOD3_Bridge.getInstance().alertForElement(drive.name, "D");
                    driveElementsHandler(drive.driveElements);
                    currentPriority = drive.priority;
                }
            }
        }

        Log.d(TAG, String.format("Valid drives: %d", validDrivesCount));

        return validDrivesCount > 0;
    }

    private void driveElementsHandler(List<DriveElement> driveElements) {
        long time = Calendar.getInstance().getTimeInMillis();

        for (DriveElement driveElement : driveElements) {
            Log.d(TAG, String.format("Running: %s", driveElement.name));

            if (time >= driveElement.nextCheck) {
                driveElement.updateNextCheck(time);

                int numTriggersNeeded = 0;

                for (Sense sense : driveElement.triggers) {
                    numTriggersNeeded = checkSense(numTriggersNeeded, sense);
                }

                if (numTriggersNeeded == driveElement.triggers.size()) {
//                    ABOD3_Bridge.getInstance().alertForElement(driveElement.name, "DE");
                    PlanElement elementToBeTriggered = driveElement.triggerableElement;
                    if (elementToBeTriggered instanceof Competence) {
                        competenceHandler((Competence) elementToBeTriggered);

                    } else if (elementToBeTriggered instanceof ActionPattern) {
                        actionPatternHandler((ActionPattern) elementToBeTriggered);

                    } else if (elementToBeTriggered instanceof Action) {
                        triggerAction((Action) elementToBeTriggered);
                    }
                } else {
                    Log.d(TAG, String.format("Triggers mismatch: %d v %d", numTriggersNeeded, driveElement.triggers.size()));
                }
            } else {
                Log.d(TAG, "Not due to run yet");
            }
        }
    }

    private void competenceHandler(Competence competence) {
        Log.d(TAG, String.format("Running competence: %s", competence.name));
        Sense goal = competence.goals.get(0); // TODO: Only check one goal?

        if (checkSense(0, goal) == 0) {
//            ABOD3_Bridge.GetInstance().AlertForElement(competence.name, "C");

            int numCEActivated = 0;

            for (CompetenceElement competenceElement : competence.competenceElements) {
                if (competenceElementHandler(competenceElement)) {
                    numCEActivated += 1;
                }
            }

            Log.d(TAG, String.format("Competence elements run: %d", numCEActivated));
        }
    }

    private boolean competenceElementHandler(CompetenceElement competenceElement) {
        Log.d(TAG, String.format("Running competence element: %s", competenceElement.name));
        int numSensesNeeded = 0;

        Log.d(TAG, String.format("Checking %d senses", competenceElement.senses.size()));

        for (Sense sense : competenceElement.senses) {
            numSensesNeeded = checkSense(numSensesNeeded, sense);
        }

        if (numSensesNeeded == competenceElement.senses.size()) {
//            ABOD3_Bridge.GetInstance().AlertForElement(competenceElement.name, "CE");
            PlanElement elementToBeTriggered = competenceElement.triggerableElement;

            Log.d(TAG, String.format("Triggerable name is %s", competenceElement.triggerableName));
//            Log.d(TAG, String.format("Triggerable is %s", elementToBeTriggered.toString()));

            if (elementToBeTriggered instanceof Competence) {
                competenceHandler((Competence) elementToBeTriggered);

            } else if (elementToBeTriggered instanceof ActionPattern) {
                actionPatternHandler((ActionPattern) elementToBeTriggered);

            } else if (elementToBeTriggered instanceof Action) {
                triggerAction((Action) elementToBeTriggered);
            } else {
                Log.d(TAG, String.format("Unknown type: %s", elementToBeTriggered.getClass().getSimpleName()));
            }

            return true;
        } else {
            return false;
        }
    }


    private void actionPatternHandler(ActionPattern actionPattern) {
        Log.d(TAG, String.format("Running action pattern: %s", actionPattern.name));
//        ABOD3_Bridge.GetInstance().AlertForElement(, actionPattern.name, "AP");

        // TODO: in Andreas' C# code this is started in a 'coroutine' - investigate this
        executeActionPattern(actionPattern, 0);
    }

    private void executeActionPattern(ActionPattern actionPattern, int currentActionIndex) {
        if (actionPattern.actions.size() <= currentActionIndex) {
            return;
        }

        Log.d(TAG, String.format("Executing action %d of action pattern: %s", currentActionIndex, actionPattern.name));
        if (currentActionIndex < actionPattern.actions.size()) {
            triggerAction(actionPattern.actions.get(currentActionIndex));

            // TODO: delay either timeToComplete seconds, or until we know the action has completed, before going on to the next action
            executeActionPattern(actionPattern, currentActionIndex + 1);

        } else {
            // end the execution (was a coroutine `yield` in C#)
        }
    }

    public void triggerAction(Action action) {
        Log.d(TAG, String.format("Triggering action: %s", action.name));
//        ABOD3_Bridge.GetInstance().AlertForElement(action.name, "A");
        behaviourLibrary.executeAction(action);
    }

    private int checkSense(int numTriggersTrue, Sense sense) {
        switch (sense.comparator) {
            case "bool":
                if (SenseIsBoolean(sense)) {
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

    private boolean SenseIsBoolean(Sense sense) {
        if (sense.value == 1) {
            if (behaviourLibrary.getBooleanSense(sense)) {
                return true;
            }
        } else if (sense.value == 0) {
            if (!behaviourLibrary.getBooleanSense(sense)) {
                return true;
            }
        } else {
            Log.d(TAG, "Sense: "+sense.toString()+" expected output should be boolean 0 or 1");
        }

        return false;
    }

    private boolean SenseIsEqual(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) == sense.value);
    }

    private boolean SenseIsLessThan(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) < sense.value);
    }

    private boolean SenseIsLessThanOrEqual(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) <= sense.value);
    }

    private boolean SenseIsGreaterThan(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) > sense.value);
    }
    private boolean SenseIsGreaterThanOrEqual(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) >= sense.value);
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