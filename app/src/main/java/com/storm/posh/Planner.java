package com.storm.posh;

import com.storm.pepper.PepperLog;
import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.drives.DriveCollection;
import com.storm.posh.plan.planelements.drives.DriveElement;
import com.storm.posh.plan.planelements.action.ActionPattern;
import com.storm.posh.plan.planelements.competence.Competence;
import com.storm.posh.plan.planelements.competence.CompetenceElement;

import java.util.Calendar;
import java.util.List;

public class Planner {
    private PepperLog pepperLog;
    private static final String TAG = Planner.class.getSimpleName();
    private volatile Plan plan;
    private int iteration;

    public BaseBehaviourLibrary behaviourLibrary;

    public Planner(PepperLog pepperLog) {
        this.pepperLog = pepperLog;
    }

    public void start() {
        pepperLog.appendLog(TAG,"Starting Planner");
        plan = Plan.getInstance();
        behaviourLibrary = BaseBehaviourLibrary.getInstance();

        pepperLog.appendLog(TAG, "Got plan:");
        pepperLog.appendLog(TAG, plan.toString());
    }

    public boolean update(int iteration) {
        this.iteration = iteration;
        pepperLog.clearCheckedSenses();
        drivesHandler();
        return true;
    }

    public void reset() {
        behaviourLibrary.reset();
        plan.reset();
    }

    public List<DriveCollection> driveCollections() {
        return plan.getDriveCollections();
    }


    private boolean drivesHandler() {
        int validDrivesCount = plan.getDriveCollections().size();
        pepperLog.appendLog(TAG, String.format("Starting drives: %d", validDrivesCount));
        int currentPriority = -1;


        for (DriveCollection drive : plan.getDriveCollections()) {
            boolean runDrive = false;
            pepperLog.appendLog(TAG, String.format("Considering: %s", drive));

            // TODO: Should this just be replaced by a forced break out of loop? Otherwise an arbitrary selection of drives may run

            // Avoid extra loops for lower priority items.
//            if (currentPriority != -1) {
//                if (currentPriority < drive.getPriority()) {
//                    pepperLog.appendLog(TAG, "Priority too low");
//                    continue;
//                }
//            }

            if (currentPriority == -1 || currentPriority == drive.getPriority()) {
                if (drive.getGoals().size() != 0) {
                    int numGoalsMet = 0;

                    for (Sense goal : drive.getGoals()) {
                        numGoalsMet = checkSense(numGoalsMet, goal);
                    }

                    if (numGoalsMet == drive.getGoals().size()) {
                        pepperLog.appendLog(TAG, String.format("All goals met, skipping."));
                        validDrivesCount -= 1;
                    } else {
                        pepperLog.appendLog(TAG, "Goals unmet, running drive elements");
                        runDrive = true;
                    }

                } else {
                    pepperLog.appendLog(TAG, "No goals to meet, running drive elements");
                    runDrive = true;
                }

                if (runDrive) {
                    if (driveHandler(drive)) {
//                currentPriority = drive.getPriority();
                        return true;
                    }
                }
            }

        }

        pepperLog.appendLog(TAG, "No valid drive this time");

        plan.setCurrentDrive(null);

        return false;
    }

    private boolean driveHandler(DriveCollection drive) {
        pepperLog.notifyABOD3(drive.getNameOfElement(), "D");
        long time = Calendar.getInstance().getTimeInMillis();
        plan.setCurrentDrive(drive);

        for (DriveElement driveElement : drive.getDriveElements()) {
            pepperLog.appendLog(TAG, String.format("Running: %s", driveElement));

            if (time >= driveElement.getNextCheck()) {
                driveElement.updateNextCheck(time);

                int numTriggersMet = 0;
                int numTriggersNeeded = driveElement.getSenses().size();

                for (Sense sense : driveElement.getSenses()) {
                    numTriggersMet = checkSense(numTriggersMet, sense);
                }

                if (numTriggersMet == numTriggersNeeded) {
                    setCurrentElement(driveElement);
                    pepperLog.notifyABOD3(driveElement.getNameOfElement(), "DE");
                    PlanElement elementToBeTriggered = driveElement.getTriggeredElement();

                    if (elementToBeTriggered != null) {
                        pepperLog.appendLog(TAG, String.format("Triggering %s...", elementToBeTriggered.getNameOfElement()));
                    }

                    if (elementToBeTriggered instanceof Competence) {
                        competenceHandler((Competence) elementToBeTriggered);

                    } else if (elementToBeTriggered instanceof ActionPattern) {
                        actionPatternHandler((ActionPattern) elementToBeTriggered);

                    } else if (elementToBeTriggered instanceof ActionEvent) {
                        triggerAction((ActionEvent) elementToBeTriggered);

                    } else if (elementToBeTriggered != null) {
                        pepperLog.appendLog(TAG, String.format("Failed to trigger unknown type: %s", elementToBeTriggered.getClass().getSimpleName()));
                    }

                    // only trigger one drive element per update
                    // end handler
                    return true;

                } else {
                    pepperLog.appendLog(TAG, String.format("Triggers mismatch: %d v %d", numTriggersMet, numTriggersNeeded));
                }
            } else {
                pepperLog.appendLog(TAG, "Not due to run yet");
            }
        }


        return false;
    }

    private void setCurrentElement(PlanElement element) {
        plan.getCurrentDrive().setCurrentElement(element);
        pepperLog.addCurrentElement(element);
    }

    private void competenceHandler(Competence competence) {
        pepperLog.appendLog(TAG, String.format("Running competence: %s", competence));

        int numGoalsMet = 0;
        int numGoals = competence.getGoals().size();

        for (Sense goal : competence.getGoals()) {
            numGoalsMet = checkSense(numGoalsMet, goal);
        }

        if (numGoals == 0 || numGoalsMet < numGoals) {
            setCurrentElement(competence);
            pepperLog.notifyABOD3(competence.getNameOfElement(), "C");

            for (CompetenceElement competenceElement : competence.getCompetenceElements()) {
                if (competenceElementHandler(competenceElement)) {
                    pepperLog.appendLog(TAG, String.format("Successfully ran competenceElement %s for competence %s", competenceElement.getNameOfElement(), competence.getNameOfElement()));
                    // only execute one competence element per update
                    // end handler
                    return;
                }
            }
        } else {
            pepperLog.appendLog(TAG, String.format("Not running CEs, all goals met(?): %d v %d", numGoalsMet, competence.getGoals().size()));

        }
    }

    private boolean competenceElementHandler(CompetenceElement competenceElement) {
        pepperLog.appendLog(TAG, String.format("Running competence element: %s", competenceElement));
        int numSensesMatched = 0;
        int numSensesNeeded = competenceElement.getSenses().size();

        for (Sense sense : competenceElement.getSenses()) {
            pepperLog.appendLog(TAG, sense.getNameOfElement());
            numSensesMatched = checkSense(numSensesMatched, sense);
        }

        pepperLog.appendLog(TAG, String.format("Checked senses: %d vs %d", numSensesMatched, numSensesNeeded));

        if (numSensesMatched == numSensesNeeded) {
            setCurrentElement(competenceElement);
            pepperLog.notifyABOD3(competenceElement.getNameOfElement(), "CE");
            PlanElement elementToBeTriggered = competenceElement.getTriggeredElement();

            if (elementToBeTriggered != null) {
                pepperLog.appendLog(TAG, String.format("Triggering %s...", elementToBeTriggered.getNameOfElement()));
            }

            if (elementToBeTriggered instanceof Competence) {
                competenceHandler((Competence) elementToBeTriggered);

            } else if (elementToBeTriggered instanceof ActionPattern) {
                actionPatternHandler((ActionPattern) elementToBeTriggered);

            } else if (elementToBeTriggered instanceof ActionEvent) {
                triggerAction((ActionEvent) elementToBeTriggered);

            } else if (elementToBeTriggered != null) {
                pepperLog.appendLog(TAG, String.format("Unknown type: %s", elementToBeTriggered.getClass().getSimpleName()));

            } else {
                pepperLog.appendLog(TAG, "Nothing to trigger!");
            }
            return true;
        } else {
            // sense mismatch
            pepperLog.appendLog(TAG, String.format("Only matched %d of %d senses", numSensesMatched, numSensesNeeded));
            return false;
        }
    }


    private void actionPatternHandler(ActionPattern actionPattern) {
        setCurrentElement(actionPattern);
        pepperLog.appendLog(TAG, String.format("Running action pattern: %s", actionPattern));
        pepperLog.notifyABOD3(actionPattern.getNameOfElement(), "AP");

        // TODO: in Andreas' C# code this is started in a 'coroutine' - investigate this
        executeActionPattern(actionPattern, 0);
    }

    private void executeActionPattern(ActionPattern actionPattern, int currentActionIndex) {
        pepperLog.appendLog(TAG, String.format("executing %s which has these actions:", actionPattern.toString()));

        for (ActionEvent action : actionPattern.getActionEvents()) {
            pepperLog.appendLog(TAG, String.format("Action: %s", action));
        }

        if (actionPattern.getActionEvents().size() <= currentActionIndex) {
            pepperLog.appendLog(TAG, String.format("Aborting execution: %d vs %d", currentActionIndex, actionPattern.getActionEvents().size()));
            return;
        }

        pepperLog.appendLog(TAG, String.format("Executing action %d of action pattern: %s", currentActionIndex, actionPattern));
        if (currentActionIndex < actionPattern.getActionEvents().size()) {
            triggerAction(actionPattern.getActionEvents().get(currentActionIndex));
            pepperLog.appendLog(TAG, "next action...");
            // TODO: delay either timeToComplete seconds, or until we know the action has completed, before going on to the next action
            executeActionPattern(actionPattern, currentActionIndex + 1);

        } else {
            pepperLog.appendLog(TAG, "All done executing AP");
        }
    }

    public void triggerAction(ActionEvent action) {
        pepperLog.appendLog(TAG, String.format("Triggering action: %s", action));
        pepperLog.notifyABOD3(action.getNameOfElement(), "A");
        setCurrentElement(action);
        behaviourLibrary.executeAction(action);
    }

    private int checkSense(int numTriggersTrue, Sense sense) {
        int origNumTriggersTrue = numTriggersTrue;

        switch (sense.getComparator()) {
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
                pepperLog.appendLog(TAG, String.format("Unknown comparator: %s", sense.getComparator()));
                break;
        }

//        if (numTriggersTrue != origNumTriggersTrue) {
//            pepperLog.appendLog(TAG, "Sense true");
//        } else {
//            pepperLog.appendLog(TAG, "Sense false");
//        }

        return numTriggersTrue;
    }

    private boolean SenseIsBoolean(Sense sense) {
//        pepperLog.appendLog(TAG, String.format("bool wants %b", sense.getBooleanValue()));
        if (sense.getBooleanValue()) {
//            pepperLog.appendLog(TAG, "comparator wants true");
            if (behaviourLibrary.getBooleanSense(sense)) {
//                pepperLog.appendLog(TAG, "comparator matches");
                return true;
            }
        } else {
//            pepperLog.appendLog(TAG, "comparator wants false");
            if (!behaviourLibrary.getBooleanSense(sense)) {
//                pepperLog.appendLog(TAG, "comparator matches");
                return true;
            }
        }

//        pepperLog.appendLog(TAG, "comparator does not match");

        return false;
    }

    private boolean SenseIsEqual(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) == sense.getDoubleValue());
    }

    private boolean SenseIsLessThan(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) < sense.getDoubleValue());
    }

    private boolean SenseIsLessThanOrEqual(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) <= sense.getDoubleValue());
    }

    private boolean SenseIsGreaterThan(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) > sense.getDoubleValue());
    }
    private boolean SenseIsGreaterThanOrEqual(Sense sense) {
        return (behaviourLibrary.getDoubleSense(sense) >= sense.getDoubleValue());
    }

    public int getIteration() {
        return iteration;
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