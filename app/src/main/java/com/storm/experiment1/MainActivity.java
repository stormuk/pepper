package com.storm.experiment1;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.storm.posh.plan.reader.xposh.XPOSHPlanReader;
import com.storm.posh.BehaviourLibrary;
import com.storm.posh.Planner;
import com.storm.posh.plan.Plan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, PepperLog {

    private int mode = 0;
    private String planFile;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final SimpleDateFormat logTimeFormat = new SimpleDateFormat("HH:mm:ss.SSSS");
    private ConstraintLayout overlayLayout = null;
    private Planner planner;
//    private UIPlanTree uiPlanTree = null;
//    private ExecutorService backgroundColorExecutor = null;
//    private ScheduledExecutorService backgroundPingerScheduler;
    private ConstraintLayout rootLayout = null;
    private Handler generalHandler = null;
    private TextView plannerLog = findViewById(R.id.textPlannerLog);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        planFile = "plans/plan.xml";

        rootLayout = findViewById(R.id.root_layout);
        overlayLayout = findViewById(R.id.overlay_layout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    public void appendLog(final String tag, final String message) {
        Log.d(tag, message);
        final Date currentTime = Calendar.getInstance().getTime();

        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                plannerLog.append(String.format("\n %s [%s]: %s", logTimeFormat.format(currentTime), tag, message));
            }
        });
    }

    @Override
    public void appendLog(final String message) {
        Log.d(TAG, message);
        final Date currentTime = Calendar.getInstance().getTime();

        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                plannerLog.append(String.format("\n %s [%s]: %s", logTimeFormat.format(currentTime), TAG, message));
            }
        });
    }

    @Override
    public void clearLog() {
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                plannerLog.setText("");
            }
        });
    }

    public void readPlan(View view) {
        Log.d(TAG, "READING PLAN");

        planner = new Planner(this);

        BehaviourLibrary behaviourLibrary = new BehaviourLibrary();
        planner.behaviourLibrary = behaviourLibrary;

        Plan.getInstance().cleanAllLists();
        XPOSHPlanReader planReader = new XPOSHPlanReader();

        planReader.readFile(planFile);

        planner.start();

        displayPlan();
    }

    private void displayPlan() {
//        Output text summary of plan, or simple grid structure?
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

    public void runPlan(View view) {
        clearLog();
        appendLog("RESETTING PLAN");
        planner.reset();

        appendLog("RUNNING PLAN");


        final Handler handler = new Handler();
        Runnable planRunner = new Runnable() {
            int iteration = 1;
            boolean completed = false;

            @Override
            public void run() {
                appendLog(String.format(".... starting update #%d....", iteration));
                try {
                    completed = !planner.update();

                } catch (Exception e) {
                    // TODO: handle exception
                }
                finally {
                    if (completed) {
                        appendLog("REACHED END OF PLAN");
                    } else if (iteration > 50) {
                        appendLog("REACHED ITERATION LIMIT");
                    } else {
                        iteration += 1;
                        appendLog(".... waiting 5000ms ....");
                        handler.postDelayed(this, 5000);
                    }
                }
            }
        };

        //runnable must be execute once
        appendLog(".... delaying 5000ms ....");
        handler.postDelayed(planRunner, 5000);
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
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // The robot focus is gained.
//
//        // Create a new say action.
//        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
//                .withText("Hello human!") // Set the text to say.
//                .build(); // Build the say action.
//
//        // Execute the action.
//        say.run();
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }
}
