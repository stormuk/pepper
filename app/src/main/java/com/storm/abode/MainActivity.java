//package com.alexbath.abod3ar;
//
//import android.content.pm.ActivityInfo;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.support.constraint.ConstraintLayout;
//import android.support.v7.app.AppCompatActivity;
//import android.text.method.ScrollingMovementMethod;
//import android.util.Log;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.recklesscoding.abode.core.plan.Plan;
//import com.recklesscoding.abode.core.plan.planelements.PlanElement;
//import com.recklesscoding.abode.core.plan.planelements.action.ActionEvent;
//import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;
//
//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.CameraBridgeViewBase;
//import org.opencv.android.JavaCameraView;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;
//import org.opencv.android.Utils;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.Point;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
//
//    private static final String OPENCVTAG = "OpenCVCamera";
//    private static final String SERVERTAG = "SERVER";
//    private CameraBridgeViewBase cameraBridgeViewBase;
//    private Mat frame,frameHSV,thresh, eroded, dilated,blurred,marker, markerResized,output;
//    private BaseLoaderCallback baseLoaderCallback;
//    private TextView statusTextView;
//    private TextView serverTextView;
//    private Scalar lower;
//    private Scalar upper;
//    private Mat circles;
//    private int iCannyUpperThreshold;
//    private int iMinRadius;
//    private int iMaxRadius;
//    private int iAccumulator;
//    private boolean drawCirclesDetection;
//    private int robotIdx = 0;
//    private Handler generalHandler;
//    private Thread uiFlasherThread;
//    private Button connectToServerbutton;
//    private Button loadPlanButton;
//    private Button testButton;
//    private static final int START_SERVER_POLLING = 0;
//    private static final int SERVER_RESPONSE = 1;
//    private static final int START_FLASHING = 2;
//    private static final int ARELEMENT_BACKGROUND_COLOR_CHANGE = 3;
//    private static final int DEFINE_SERVER_REQUEST = 4;
//    private static final int HIDE_ARPLANELEMENTS = 6;
//    private static final int SHOW_ARPLANELEMENTS = 7;
//    private Point center;
//    Mat element = null;
//    private ARPlanElement driveRoot = null;
//    private ArrayList<ARPlanElement> drivesList = null;
//    private boolean showElements = false;
//    private int nodeRadialOffset = 260;
//    private NetworkThread networkThread;
//
//    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//        System.loadLibrary("opencv_java3");
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//
//        robotIdx = 1;
//        statusTextView = (TextView) findViewById(R.id.status_text);
//        serverTextView = (TextView) findViewById(R.id.server_response);
//        serverTextView.setMovementMethod(new ScrollingMovementMethod());
//        connectToServerbutton = findViewById(R.id.connect_server_button);
//        loadPlanButton  = findViewById(R.id.load_plan_button);
//        testButton = findViewById(R.id.test_button);
//
//        testButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                networkThread.stop();
//            }
//        });
//
//        connectToServerbutton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if(drivesList != null) {
//                    generalHandler.sendEmptyMessage(DEFINE_SERVER_REQUEST);
//                    generalHandler.sendEmptyMessage(START_SERVER_POLLING);
//                }else {
//                    statusTextView.append("\n Load a Plan first!");
//                }
//            }
//        });
//
//        loadPlanButton.setOnClickListener(v -> {
//            String fileName = "plans/Plan6.inst";
//            List<DriveCollection> driveCollections = PlanLoader.loadPlanFile(fileName, getApplicationContext());
//
//            ConstraintLayout cl = findViewById(R.id.coordinatorLayout);
//            drivesList = new ArrayList<>();
//
//            driveRoot = new ARPlanElement(getApplicationContext(), "Drives", Color.YELLOW);
//
//            for (DriveCollection driveCollection : driveCollections){
//
//                ARPlanElement arPlanElement = new ARPlanElement(getApplicationContext(), driveCollection.getNameOfElement(), Color.RED);
//                arPlanElement.setUIName(driveCollection.getNameOfElement());
//
//                arPlanElement.createFlasherThread(generalHandler);
//
//                drivesList.add(arPlanElement);
//                cl.addView(arPlanElement.getView());
//
//                arPlanElement.getView().setOnClickListener(new View.OnClickListener() {
//                    ARPlanElement arPlanElementListener = arPlanElement;
//                    public void onClick(View v) {
//                        statusTextView.append("\n "+arPlanElementListener.getUIName());
//                    }
//                });
//            }
//
//            cl.addView(driveRoot.getView());
//            showElements = true;
//
////            ObjectAnimator animator = ObjectAnimator.ofInt(drivesList.get(0).getView(),"backgroundColor",
////                    Color.parseColor("#0000ff"), Color.parseColor("#2f4f4f"),Color.parseColor("#0000ff"));
////            animator.setDuration(100);
////            animator.setEvaluator(new ArgbEvaluator());
////            //animator.setRepeatMode(Animation.REVERSE);
////            animator.setRepeatCount(Animation.INFINITE);
////            animator.start();
////            animator.setDuration(1000);
//
//            generalHandler.sendEmptyMessage(START_FLASHING);
//        });
//
//        // TODO: Run any OPENCV code after this part!!!
//        // also read this: https://docs.opencv.org/2.4/platforms/android/service/doc/JavaHelper.html#boolean-initdebug
//        if(OpenCVLoader.initDebug()){
//            statusTextView.setText("OpenCV Loaded!");
//            Log.d(OPENCVTAG, "OpenCV Loaded!");
//        }else{
//            statusTextView.setText("OpenCV Error!");
//            Log.d(OPENCVTAG, "OpenCV Failed!");
//        }
//
//        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.openCVCameraView);
//        //cameraBridgeViewBase.setMaxFrameSize(1600,900); // this will improve performance!!
//        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
//        cameraBridgeViewBase.setCvCameraViewListener(this);
//
//        baseLoaderCallback = new BaseLoaderCallback(this) {
//            @Override
//            public void onManagerConnected(int status) {
//                switch (status){
//                    case BaseLoaderCallback.SUCCESS:
//                        cameraBridgeViewBase.enableView();
//                        break;
//                    default:
//                        super.onManagerConnected(status);
//                        break;
//                }
//                super.onManagerConnected(status);
//            }
//        };
//
//        element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * 7 + 1, 2 * 7 + 1),
//                new Point(7, 7));
//
//        //marker = Imgcodecs.imread("markers/marker1.png");
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.marker1);
//        marker = new Mat();
//        markerResized = new Mat();
//        Utils.bitmapToMat(bitmap, marker);
//        Size sz = new Size(150,150);
//        Imgproc.resize( marker, markerResized, sz );
//        Imgproc.cvtColor(markerResized, marker,Imgproc.COLOR_RGB2GRAY);
//    }
//
//
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        frame = new Mat(width,height, CvType.CV_8UC4);
//        frameHSV = new Mat(width,height, CvType.CV_8UC4);
//        thresh = new Mat(width,height, CvType.CV_8UC4);
//        eroded = new Mat(width,height, CvType.CV_8UC4);
//        dilated = new Mat(width,height, CvType.CV_8UC4);
//        blurred = new Mat(width,height, CvType.CV_8UC4);
//        output = new Mat(width,height, CvType.CV_8UC4);
//        lower = new Scalar(29, 86, 6);
//        upper = new Scalar(64, 255, 255);
//
//        iCannyUpperThreshold = 100;
//        iMinRadius = 10;
//        iMaxRadius = 120;
//        iAccumulator = 60;
//        circles = new Mat();
//
//        drawCirclesDetection = false;
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//        frame.release();
//        frameHSV.release();
//        thresh.release();
//        eroded.release();
//        dilated.release();
//        blurred.release();
//        marker.release();
//        markerResized.release();
//        output.release();
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//
//        frame = inputFrame.rgba();
//
//        Imgproc.cvtColor(frame,frameHSV,Imgproc.COLOR_BGR2HSV);
//        Core.inRange(frameHSV,lower,upper,thresh);
//
//        Imgproc.erode(thresh, eroded,element);
//        Imgproc.dilate(eroded, dilated,element);
//
//        //returns single channel image!
//        gaussianBlur(dilated.getNativeObjAddr(),blurred.getNativeObjAddr());
//        //Imgproc.GaussianBlur(dilated, blurred, new Size(7, 7), 3, 3 );
//
//        Imgproc.HoughCircles(blurred, circles, Imgproc.CV_HOUGH_GRADIENT,2.0,
//                blurred.rows() / 8, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);
//
//        if(circles.cols() == 1 ){
//
//            int i = 0;
//            //circlesDetails[0]=x, 1=y, 2=radius
//            double circlesDetails[] = circles.get(0, 0);
//
//            double circleX = Math.round(circlesDetails[0]);
//            double circleY = Math.round(circlesDetails[1]);
//            int radius = (int) Math.round(circlesDetails[2]);
//
//            center = new Point(circleX,circleY);
//
////            if(circleX == 0.0 && circleY == 0.0){
////                generalHandler.sendEmptyMessage(HIDE_ARPLANELEMENTS);
////                return frame;
////            }else{
////                generalHandler.sendEmptyMessage(SHOW_ARPLANELEMENTS);
////            }
//
//            if(drawCirclesDetection){
//                Imgproc.circle(frame, center,2, new Scalar(0,0,255), -1, 8, 0 );
//                Imgproc.circle( frame, center, radius, new Scalar(0,0,255), 3, 8, 0 );
//            }
//
//            if(showElements){
//
//                driveRoot.getView().setX((float) (circleX - driveRoot.getView().getWidth()/2));
//                driveRoot.getView().setY((float) (circleY - driveRoot.getView().getHeight()/2));
//
//                for(int k = 0; k<drivesList.size(); k++){
//
//                    //TODO: 4 should be drivesList.size()!
//                    float xV = (float) (circleX + nodeRadialOffset * Math.cos(Math.PI / drivesList.size() * (2*k + 1)));
//                    float yV = (float) (circleY + nodeRadialOffset * Math.sin(Math.PI / drivesList.size() * (2*k + 1)));
//
//                    drivesList.get(k).getView().setX(Math.round(xV));
//                    drivesList.get(k).getView().setY(Math.round(yV));
//
//                    Imgproc.line(frame, center,
//                                 new Point(xV, // + drivesList.get(k).getView().getWidth()/2,
//                                            yV), // + drivesList.get(k).getView().getHeight()/2),
//                                                new Scalar(255,255,255),3);
//                }
//            }
//        }
//
//        return frame;
//
////        TODO: MARKER detection part
////        frame = inputFrame.gray();
////
////        detector(markerResized.getNativeObjAddr(), frame.getNativeObjAddr());
////
////        return frame;
//    }
//
//    private native void detector(long source, long target);
//    private native void gaussianBlur(long source, long target);
//
//    @Override
//    protected void onPause() {
//
//        if(cameraBridgeViewBase != null){
//            cameraBridgeViewBase.disableView();
//        }
//        super.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//
//        generalHandler = new Handler(Looper.getMainLooper()){
//            @Override
//            public void handleMessage(Message msg){
//
//                switch (msg.what){
//                    case DEFINE_SERVER_REQUEST:
//
//                        //networkThread.setRequest(drivesList);
//
//                        break;
//                    case START_SERVER_POLLING:
//
//                        networkThread.start();
//
//                        break;
//                    case SERVER_RESPONSE:
//
//                        serverTextView.append("\n"+msg.obj);
//
//                        String[] splittedLine = ((String) msg.obj).split(" ");
//                        PlanElement planElement = null;
//                        String typeOfPlanElement;
//                        String planElementName = splittedLine[3];
//
//                        if (isValidLine(splittedLine)) {
//                            typeOfPlanElement = splittedLine[2];
//                            if (!isActionPatternElement(typeOfPlanElement)) { //We ignore ActionPatternELements as they are instinct only
//                                planElement = getPlanElement(typeOfPlanElement, planElement, planElementName);
//                                if (planElement != null) {
//                                    if(typeOfPlanElement.equals("D")){
//                                        for (ARPlanElement drive : drivesList){
//                                            if(drive.getUIName().equals(planElementName)){
//                                                //increase flash/blink freq
//                                                drive.increaseFlashFrequency();
//                                            }else{
//                                                //decrease flash/blink freq
//                                                drive.decreaseFlashFrequency();
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        break;
//
//                    case START_FLASHING:
//
//                        for (ARPlanElement arPlanElement : drivesList){
//                            arPlanElement.startFlasherThread();
//                        }
//                        break;
//
//                    case ARELEMENT_BACKGROUND_COLOR_CHANGE:
//
//                        String[] flashInfo = msg.obj.toString().split(":");
//                        String arElementName = flashInfo[0];
//                        int arElementColor = Color.parseColor(flashInfo[1]);
//
//                        for (ARPlanElement arPlanElement : drivesList){
//                            if(arPlanElement.getUIName().equals(arElementName)){
//                                arPlanElement.setBackgroundColor(arElementColor);
//                            }
//                        }
//
//                        break;
//
//                    case HIDE_ARPLANELEMENTS:
//
//                        if(driveRoot != null && drivesList != null) {
//
//                            driveRoot.getView().setVisibility(View.INVISIBLE);
//
//                            for (ARPlanElement arPlanElement : drivesList) {
//                                arPlanElement.getView().setVisibility(View.INVISIBLE);
//                            }
//                        }
//                        break;
//
//                    case SHOW_ARPLANELEMENTS:
//
//                        if(driveRoot != null && drivesList != null) {
//
//                            driveRoot.getView().setVisibility(View.VISIBLE);
//
//                            for (ARPlanElement arPlanElement : drivesList) {
//                                arPlanElement.getView().setVisibility(View.VISIBLE);
//                            }
//                        }
//                        break;
//
//                    default:
//                        super.handleMessage(msg);
//                }
//            }
//        };
//
//        networkThread = new NetworkThread(50,generalHandler,"192.168.0.100", 3001);
//
//        if(!OpenCVLoader.initDebug()){
//            Log.d(OPENCVTAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
//            Toast.makeText(getApplicationContext(),"OpenCV problem!",Toast.LENGTH_LONG).show();
//        }else{
//            Log.d(OPENCVTAG, "OpenCV library found inside package. Using it!");
//            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        }
//
//        super.onResume();
//    }
//
//    @Override
//    protected void onStop() {
//
//        networkThread.stop();
//        generalHandler.removeCallbacksAndMessages(null);
//
//        super.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        if(cameraBridgeViewBase != null){
//            cameraBridgeViewBase.disableView();
//        }
//
//        super.onDestroy();
//    }
//
//    private boolean isValidLine(String[] splittedLine) {
//        return !splittedLine[0].startsWith("*") && splittedLine.length >= 4;
//    }
//
//    private boolean isActionPatternElement(String typeOfPlanElement) {
//        return typeOfPlanElement.startsWith("APE");
//    }
//
//    private boolean isActionPattern(String typeOfPlanElement) {
//        return typeOfPlanElement.equals("AP");
//    }
//
//    private boolean isAction(String typeOfPlanElement) {
//        return typeOfPlanElement.equals("A");
//    }
//
//    private boolean isCompetence(String typeOfPlanElement) {
//        return typeOfPlanElement.equals("C");
//    }
//
//    private boolean isCompetenceElement(String typeOfPlanElement) {
//        return typeOfPlanElement.equals("CE");
//    }
//
//    private boolean isDrive(String typeOfPlanElement) {
//        return typeOfPlanElement.equals("D");
//    }
//
//    private PlanElement getPlanElement(String typeOfPlanElement, PlanElement planElement, String planElementName) {
//        if (isAction(typeOfPlanElement)) {
//            planElement = Plan.getInstance().findAction(planElementName);
//            if (planElement == null) {
//                planElement = new ActionEvent(planElementName);
//            }
//        } else if (isActionPattern(typeOfPlanElement)) {
//            planElement = Plan.getInstance().findActionPattern(planElementName);
//            if (planElement == null) {
//                planElement = new ActionEvent(planElementName);
//            }
//        } else if (isCompetence(typeOfPlanElement)) {
//            planElement = Plan.getInstance().findCompetence(planElementName);
//        } else if (isCompetenceElement(typeOfPlanElement)) {
//            planElement = Plan.getInstance().findCompetenceElement(planElementName);
//        } else if (isDrive(typeOfPlanElement)) {
//            planElement = Plan.getInstance().findDriveCollection(planElementName);
//        }
//        return planElement;
//    }
//
//}
