package com.storm.experiment1;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.drives.DriveCollection;
import com.storm.posh.plan.reader.xposh.XPOSHPlanReader;
import com.storm.posh.BaseBehaviourLibrary;
import com.storm.posh.Planner;
import com.storm.posh.plan.Plan;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends RobotActivity implements PepperLog {

    private int mode = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final SimpleDateFormat logTimeFormat = new SimpleDateFormat("HH:mm:ss.SSSS");

    private int maxIterations = 0;
    private boolean stopRunningPlan;

    private Planner planner;
//    private UIPlanTree uiPlanTree = null;
//    private ExecutorService backgroundColorExecutor = null;
//    private ScheduledExecutorService backgroundPingerScheduler;
//    private ConstraintLayout rootLayout = null;
//    private Handler generalHandler = null;
    private TextView plannerLog;
    private TextView checkedSenses;
    private TextView currentDriveName;
    private TextView currentElementName;

    private PepperServer pepperServer;
    private BaseBehaviourLibrary behaviourLibrary;

    public Button startButton;
    public Button stopButton;

    private ArrayList currentElements = new ArrayList();

    ListView drivesList;
    ListView elementsList;

    DrivesListAdapter drivesAdapter;
    ElementsListAdapter elementsAdapter;
    NoElementsListAdapter noElementsAdapter;

    private int planResourceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);

//        plannerLog = findViewById(R.id.textPlannerLog);
//        currentDriveName = findViewById(R.id.currentDrive);
//        currentElementName = findViewById(R.id.currentElement);
//        checkedSenses = findViewById(R.id.checkedSenses);

//        rootLayout = findViewById(R.id.root_layout);

        planner = new Planner(this);

        // configure for chosen plan
        planResourceId = R.raw.plan_complex;
        behaviourLibrary = new AnnoyBehaviourLibrary();
        // end configure for chosen plan

        behaviourLibrary.setPepperLog(this);
        behaviourLibrary.setActivity(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Pepper server
        pepperServer = new PepperServer(this);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, BaseBehaviourLibrary.getInstance());


        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);


        startButton.setOnClickListener(ignore -> {
            runPlan();
//            if (behaviourLibrary.hasQiContext()) {
//                appendLog(TAG, "STARTING");
//
//                FutureUtils.wait(3, TimeUnit.SECONDS).andThenConsume(ignore_too -> behaviourLibrary.doMapping());
//            } else {
//                appendLog(TAG, "CANNOT START YET");
//            }

        });

        stopButton.setOnClickListener(ignore -> {
            appendLog(TAG, "STOPPING");
            stopRunningPlan = true;

            behaviourLibrary.stopMoving();
        });

        drivesList = (ListView) findViewById(R.id.drives_list);
        elementsList = (ListView) findViewById(R.id.elements_list);

        readPlan();
    }

    @Override
    public void appendLog(final String tag, final String message, boolean server) {
        Log.d(tag, message);
        final Date currentTime = Calendar.getInstance().getTime();
        final String formattedMessage = String.format("%s [%s]: %s", logTimeFormat.format(currentTime), tag, message);

        if (server) {
            pepperServer.sendMessage(formattedMessage);
        }

        runOnUiThread(new Runnable(){
            @Override
            public void run(){
//                plannerLog.append("\n" + formattedMessage);
            }
        });
    }

    @Override
    public void appendLog(String tag, String message) {
        this.appendLog(tag, message, true);
    }

    @Override
    public void appendLog(String message) {
        this.appendLog(TAG, message, true);
    }

    @Override
    public void clearLog() {
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
//                plannerLog.setText("");
            }
        });
    }

    @Override
    public void checkedBooleanSense(String tag, Sense sense, boolean value) {
        String formattedMessage = String.format("%s: %b", sense, value);

        appendLog(tag, "Checked sense - "+formattedMessage, false);
        notifyABOD3(sense.getNameOfElement(), "S");

//        runOnUiThread(new Runnable(){
//            @Override
//            public void run(){
//                checkedSenses.append("\n" + formattedMessage);
//            }
//        });
    }

    @Override
    public void checkedDoubleSense(String tag, Sense sense, double value) {
        String formattedMessage = String.format("%s: %f", sense, value);

        appendLog(tag, "Checked sense - "+formattedMessage, false);
        notifyABOD3(sense.getNameOfElement(), "S");

//        runOnUiThread(new Runnable(){
//            @Override
//            public void run(){
//                checkedSenses.append("\n" + formattedMessage);
//            }
//        });
    }

    public void displaySenses() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                checkedSenses.setText("");
//                checkedSenses.append(String.format("Idle Time: %f\n\n", behaviourLibrary.getIdleTime()));
//
//                checkedSenses.append(String.format("Human Present: %b\n", behaviourLibrary.isHumanPresent()));
//                checkedSenses.append(String.format("Human Engaged: %b\n\n", behaviourLibrary.isHumanEngaged()));
//
//                checkedSenses.append(String.format("Mapping Complete: %b\n", behaviourLibrary.isMappingComplete()));
//                checkedSenses.append(String.format("Mapping In Progress: %b\n\n", behaviourLibrary.isMappingInProgress()));
//
//                checkedSenses.append(String.format("Battery Low: %b\n", behaviourLibrary.isBatteryLow()));
//                checkedSenses.append(String.format("Battery Charging: %b\n", behaviourLibrary.isBatteryCharging()));
            }
        });
    }

    @Override
    public void clearCheckedSenses() {
//        runOnUiThread(new Runnable(){
//            @Override
//            public void run(){
//                checkedSenses.setText("");
//            }
//        });
    }

    @Override
    public void setCurrentDrive(final DriveCollection drive) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (drive != null) {
//                    currentDriveName.setText(drive.getNameOfElement());
                } else {
//                    currentDriveName.setText("Waiting...");
                }
            }
        });

        setCurrentElement(null);
    }

    @Override
    public void setCurrentElement(final PlanElement element) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (element != null) {
//                    currentElementName.setText(element.getNameOfElement());
                } else {
//                    currentElementName.setText("");
                }
            }
        });
    }

    @Override
    public void addCurrentElement(final PlanElement element) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (element != null) {
                    currentElements.add(element);
                }
            }
        });
    }

    @Override
    public void clearCurrentElements() {
        currentElements.clear();
    }

    @Override
    public void notifyABOD3(String name, String type) {
        String message = String.format("ABOD3,%s,%s", name, type);
//        this.appendLog(TAG, message, false);
        pepperServer.sendMessage(message);
    }

    public void readPlan(View view) {
        readPlan();
    }

    private void readPlan() {
        Log.d(TAG, "READING PLAN");

        Plan.getInstance().cleanAllLists();
        XPOSHPlanReader planReader = new XPOSHPlanReader();

        InputStream planFile = getResources().openRawResource(planResourceId);

        planReader.readFile(planFile);

        planner.start();

        displayPlan();
    }

    private void displayPlan() {
        Plan plan = Plan.getInstance();

        if (drivesAdapter == null || drivesAdapter.isStale(plan.getCurrentDrive())) {
            drivesAdapter = new DrivesListAdapter(this, plan.getDriveCollections(), plan.getCurrentDrive());
            drivesList.setAdapter(drivesAdapter);
        }

        if (currentElements.isEmpty()) {
            if (noElementsAdapter == null) {
                ArrayList noElement = new ArrayList();
                noElement.add("Performing no action...");
                noElementsAdapter = new NoElementsListAdapter(this, noElement);
            }
            elementsList.setAdapter(noElementsAdapter);
        } else {
//            Collections.reverse(currentElements);
            elementsAdapter = new ElementsListAdapter(this, currentElements);
            elementsList.setAdapter(elementsAdapter);
        }
    }

//    private void displayPlan() {
//        if (uiPlanTree == null) {
//            List<DriveCollection> driveCollections = planner.driveCollections();
//
//            //createTree
//            uiPlanTree = new UIPlanTree(driveCollections, getApplicationContext(), overlayLayout);
//            //root = uiPlanTree.getRoot();
//            uiPlanTree.initState();
//
//            backgroundColorExecutor = Executors.newSingleThreadExecutor();
//            backgroundPingerScheduler = Executors.newSingleThreadScheduledExecutor();
//
//            backgroundColorExecutor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    final Runnable backgroundPinger = new Runnable() {
//
//                        @Override
//                        public void run() {
//                            uiPlanTree.setDefaultBackgroundColorNodes();
//                        }
//                    };
//
//                    backgroundPingerScheduler.scheduleAtFixedRate(backgroundPinger, 30, 400, TimeUnit.MILLISECONDS);
//                }
//            });
//
//        }
//    }

    public void runPlan() {
        clearLog();
        FutureUtils.wait(0, TimeUnit.SECONDS).andThenConsume(ignore -> behaviourLibrary.doHumans());

        stopRunningPlan = false;

        final Handler handler = new Handler();
        Runnable planRunner = new Runnable() {
            int iteration = 1;
            boolean completed = false;

            @Override
            public void run() {
                if (stopRunningPlan == true) {
                    appendLog("PLAN RUN STOPPED");
                    stopRunningPlan = false;
                    return;
                }

                try {
                    clearLog();
                    clearCurrentElements();
                    appendLog(" ");
                    appendLog(String.format("\n\n.... starting update #%d....\n\n", iteration));
                    completed = !planner.update();
                    displayPlan();

                } catch (Exception e) {
                    // TODO: handle exception
                }
                finally {
                    if (completed) {
                        appendLog("REACHED END OF PLAN");
                    } else if (maxIterations > 0 && iteration > maxIterations) {
                        appendLog("REACHED ITERATION LIMIT");
                    } else {
                        iteration += 1;

                        handler.postDelayed(this, 1000);
                    }
                }
            }
        };

        // runnable must be execute once
        FutureUtils
                .wait(3, TimeUnit.SECONDS)
                .andThenConsume(ignore -> {
                    appendLog("RESETTING PLAN");
                    planner.reset();

                    appendLog("RUNNING PLAN");
                    handler.postDelayed(planRunner, 1000);
                });
    }

//    private void createGeneralHandler() {
//        generalHandler = new Handler(Looper.getMainLooper()){
//            @Override
//            public void handleMessage(Message msg){
//
//                switch (msg.what){
//                    case SERVER_RESPONSE:
//
//                        if(serverTextView.getVisibility() == View.VISIBLE) {
//                            serverTextView.append("\n" + msg.obj);
//                        }
//
//                        updateARElementsVisuals(msg);
//
//                        break;
//
//                    default:
//                        super.handleMessage(msg);
//                }
//            }
//        };
//    }

//    public void reset() {
////        stopExecutorService(networkExecutor);
//        stopExecutorService(backgroundColorExecutor);
////        stopExecutorService(serverPingerScheduler);
//        stopExecutorService(backgroundPingerScheduler);
//        generalHandler.removeCallbacksAndMessages(null);
//
//        if(uiPlanTree != null) {
//            uiPlanTree.removeNodesFromUI(rootLayout, uiPlanTree.getRoot());
//        }else{
//            uiPlanTree.removeNodesFromUI(rootLayout, uiPlanTree.getRoot());
//        }
//
//        //root = null;
//        uiPlanTree = null;
//        mode = 0;
////        networkExecutor = null;
//    }
//
//    private boolean stopExecutorService(ExecutorService service) {
//
//        if(service == null){
//            return false;
//        }else {
//            service.shutdown();
//            try {
//                if (!service.awaitTermination(100, TimeUnit.MICROSECONDS)) {
//                    service.shutdownNow();
//                }
//            } catch (InterruptedException e) {
//                System.out.println("stopExecutorService() throwed " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//
//        if(service.isTerminated() && service.isShutdown()){
//            Log.i("NETWORK_TASK", "SUCCESSFULL SHUTDOWN OF GENERIC ExecutorService");
//            return true;
//        }else{
//            return false;
//        }
//    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, BaseBehaviourLibrary.getInstance());
        pepperServer.destroy();
        super.onDestroy();
    }

}
