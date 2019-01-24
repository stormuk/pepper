package com.storm.experiment1;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.storm.posh.planner.BehaviourLibrary;
import com.storm.posh.planner.Planner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final SimpleDateFormat logTimeFormat = new SimpleDateFormat("HH:mm:ss.SSSS");
    private Planner planner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    public void addToLog(final String message) {
        Log.d(TAG, message);
        final Date currentTime = Calendar.getInstance().getTime();

        runOnUiThread(new Runnable(){
            @Override
            public void run(){
            TextView plannerLog = findViewById(R.id.textPlannerLog);
            plannerLog.append(String.format("\n %s: %s", logTimeFormat.format(currentTime), message));
            }
        });
    }

    public void readPlan(View view) {
        Log.d(TAG, "READING PLAN");

        planner = new Planner();

        BehaviourLibrary behaviourLibrary = new BehaviourLibrary();
        planner.behaviourLibrary = behaviourLibrary;

        XmlResourceParser xmlPlan = getResources().getXml(R.xml.plan);

        planner.start(xmlPlan);
    }

    public void runPlan(View view) {
        addToLog("RESETTING PLAN");
        planner.reset();

        addToLog("RUNNING PLAN");


        final Handler handler = new Handler();
        Runnable planRunner = new Runnable() {
            int iteration = 1;
            boolean completed = false;

            @Override
            public void run() {
                addToLog(String.format(".... starting update #%d....", iteration));
                try {
                    completed = !planner.update();

                } catch (Exception e) {
                    // TODO: handle exception
                }
                finally {
                    if (completed) {
                        addToLog("REACHED END OF PLAN");
                    } else if (iteration > 50) {
                        addToLog("REACHED ITERATION LIMIT");
                    } else {
                        iteration += 1;
                        addToLog(".... waiting 5000ms ....");
                        handler.postDelayed(this, 5000);
                    }
                }
            }
        };

        //runnable must be execute once
        addToLog(".... delaying 5000ms ....");
        handler.postDelayed(planRunner, 5000);
    }

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
