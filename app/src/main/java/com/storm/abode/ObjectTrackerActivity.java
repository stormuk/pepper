//package com.storm.abode;
//
//import android.Manifest;
//import android.content.pm.ActivityInfo;
//import android.content.pm.PackageManager;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.support.constraint.ConstraintLayout;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.text.method.ScrollingMovementMethod;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.recklesscoding.abode.core.plan.Plan;
//import com.recklesscoding.abode.core.plan.planelements.PlanElement;
//import com.recklesscoding.abode.core.plan.planelements.action.ActionEvent;
//import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;
//import com.storm.experiment1.R;
//
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//import boofcv.abst.tracker.ConfigComaniciu2003;
//import boofcv.abst.tracker.ConfigTld;
//import boofcv.abst.tracker.MeanShiftLikelihoodType;
//import boofcv.abst.tracker.TrackerObjectQuad;
//import boofcv.alg.tracker.sfot.SfotConfig;
//import boofcv.factory.tracker.FactoryTrackerObjectQuad;
//import boofcv.struct.image.GrayU8;
//import boofcv.struct.image.ImageBase;
//import boofcv.struct.image.ImageType;
//import georegression.struct.point.Point2D_F64;
//import georegression.struct.point.Point2D_I32;
//import georegression.struct.shapes.Quadrilateral_F64;
//import mehdi.sakout.fancybuttons.FancyButton;
//
//import static com.storm.abode.ObjectTrackerActivity.TrackerType.CIRCULANT;
//
//public class ObjectTrackerActivity extends Camera2Activity implements View.OnTouchListener {
//
//    private int mode = 0;
//    // size of the minimum square which the user can select
//    private final static int MINIMUM_MOTION = 20;
//
//    private Point2D_I32 click0 = new Point2D_I32();
//    private Point2D_I32 click1 = new Point2D_I32();
//    private FrameLayout surfaceLayout = null;
//    private ConstraintLayout overlayLayout = null;
//    private FancyButton connectToServerbutton = null;
//    private FancyButton loadPlanButton = null;
//    private TextView serverTextView = null;
//    private FancyButton reset_button = null;
//    private FancyButton showServerDataButton = null;
//    private ConstraintLayout rootLayout = null;
//    private boolean showServerData = false;
//    private static final int SERVER_RESPONSE = 1;
//    private static final int HIDE_ARPLANELEMENTS = 6;
//    private static final int SHOW_ARPLANELEMENTS = 7;
//    private Handler generalHandler = null;
//    private String planName = null;
//    private String serverIPAddress = null;
//    private int serverPort;
//    private ExecutorService networkExecutor = null;
//    private ExecutorService backgroundColorExecutor = null;
//    private NetworkTask networkTask = null;
//    private ScheduledExecutorService serverPingerScheduler;
//    private ScheduledExecutorService backgroundPingerScheduler;
//    //private UIPlanTree.Node<ARPlanElement> root = null;
//    private UIPlanTree uiPlanTree = null;
//
//    public enum TrackerType { // TODO: add the others later
//        CIRCULANT,MEAN_SHIFT_LIKELIHOOD,MEAN_SHIFT,TLD,MEAN_SHIFT_SCALE,SPARSE_FLOW
//    }
//
//    public ObjectTrackerActivity() {
//        super(Resolution.R640x480);
//        //super.stretchToFill = true;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0);
//        }
//
//        setContentView(R.layout.activity_camera);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//
//        planName = "plans/Plan6.inst";
//        serverIPAddress = "192.168.0.100";
//        serverPort = 3001;
//
//        createGeneralHandler();
//
//        rootLayout = findViewById(R.id.root_layout);
//        surfaceLayout = findViewById(R.id.camera_frame_layout);
//        overlayLayout = findViewById(R.id.overlay_layout);
//        serverTextView = (TextView) findViewById(R.id.server_response);
//        serverTextView.setVisibility(View.INVISIBLE);
//        serverTextView.setMovementMethod(new ScrollingMovementMethod());
//        connectToServerbutton = findViewById(R.id.connect_server_button);
//        loadPlanButton = findViewById(R.id.load_plan_button);
//        showServerDataButton = findViewById(R.id.show_server_data);
//        reset_button = findViewById(R.id.reset_button);
//
//        startCamera(surfaceLayout,null);
//        displayView.setOnTouchListener(this);
//
//        reset_button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                reset();
//            }
//        });
//
//        showServerDataButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if(showServerData) {
//                    showServerData = false;
//                    serverTextView.setVisibility(View.INVISIBLE);
//                }else{
//                    showServerData = true;
//                    serverTextView.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//
//        connectToServerbutton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//
//                if(uiPlanTree != null) {
//
//                    if(networkExecutor == null) {
//
//                        //create executor and start that will get the server requests
//                        networkExecutor = Executors.newSingleThreadExecutor();
//                        serverPingerScheduler = Executors.newSingleThreadScheduledExecutor();
//                        networkTask = new NetworkTask(serverIPAddress, serverPort, generalHandler, serverPingerScheduler);
//                        networkExecutor.execute(networkTask);
//
//                    }else{
//                        System.out.println("networkExecutor already exists!");
//                    }
//
//                }
//            }
//        });
//
//        loadPlanButton.setOnClickListener(v -> {
//
//            if( uiPlanTree == null) {
//
//                List<DriveCollection> driveCollections = PlanLoader.loadPlanFile(planName, getApplicationContext());
//
//                //createTree
//                uiPlanTree = new UIPlanTree(driveCollections,getApplicationContext(),overlayLayout);
//                //root = uiPlanTree.getRoot();
//                uiPlanTree.initState();
//
//                backgroundColorExecutor = Executors.newSingleThreadExecutor();
//                backgroundPingerScheduler = Executors.newSingleThreadScheduledExecutor();
//
//                backgroundColorExecutor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        final Runnable backgroundPinger = new Runnable() {
//
//                            @Override
//                            public void run() {
//                                uiPlanTree.setDefaultBackgroundColorNodes();
//                            }
//                        };
//
//                        backgroundPingerScheduler.scheduleAtFixedRate(backgroundPinger, 30, 400, TimeUnit.MILLISECONDS);
//                    }
//                });
//
//            }
//        });
//
//    }
//
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
//
//    private void updateARElementsVisuals(Message msg) {
//
//        if(msg.obj == null){
//            serverTextView.setText("No Incoming String! \n");
//            System.out.println("No Incoming String!");
//            return;
//        }
//
//        if(msg.obj.equals("Robot Running ...")){
//            serverTextView.setText("Invalid String: " + msg.obj + "\n" );
//            System.out.println("Invalid String: " + msg.obj + "\n" );
//            return;
//        }
//
//        String[] splittedLine = ((String) msg.obj).split(" ");
//
//        if(splittedLine.length < 4){
//            serverTextView.setText("Invalid String: " + msg.obj + "\n" );
//            System.out.println("Invalid String: " + msg.obj + "\n" );
//            return;
//        }
//
//        PlanElement planElement = null;
//        String typeOfPlanElement;
//        String planElementName = splittedLine[3];
//
//        if (isValidLine(splittedLine)) {
//            typeOfPlanElement = splittedLine[2];
//            if (!isActionPatternElement(typeOfPlanElement)) { //We ignore ActionPatternELements as they are instinct only
//                planElement = getPlanElement(typeOfPlanElement, planElement, planElementName);
//                if (planElement != null) {
//                    if(uiPlanTree != null) {
//                        uiPlanTree.updateNodesVisuals(planElementName);
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        reset();
//    }
//
//    public void reset() {
//        stopExecutorService(networkExecutor);
//        stopExecutorService(backgroundColorExecutor);
//        stopExecutorService(serverPingerScheduler);
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
//        networkExecutor = null;
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
//
//    @Override
//    public void createNewProcessor() {
//        startObjectTracking(setTrackerType(CIRCULANT));
//    }
//
//    private void startObjectTracking(int pos) {
//        TrackerObjectQuad tracker;
//
//        switch (pos) {
//            case 0:
//                tracker = FactoryTrackerObjectQuad.circulant(null,GrayU8.class);
//                break;
//
//            case 1: {
//                ImageType imageType = ImageType.pl(3, GrayU8.class);
//                tracker = FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(false), imageType);
//            }break;
//
//            case 2: {
//                ImageType imageType = ImageType.pl(3, GrayU8.class);
//                tracker = FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(true), imageType);
//            }break;
//
//            case 3: {
//                ImageType imageType = ImageType.pl(3, GrayU8.class);
//                tracker = FactoryTrackerObjectQuad.meanShiftLikelihood(30, 5, 256, MeanShiftLikelihoodType.HISTOGRAM, imageType);
//            }break;
//
//            case 4:{
//                SfotConfig config = new SfotConfig();
//                config.numberOfSamples = 10;
//                config.robustMaxError = 30;
//                tracker = FactoryTrackerObjectQuad.sparseFlow(config,GrayU8.class,null);
//            }break;
//
//            case 5:
//                tracker = FactoryTrackerObjectQuad.tld(new ConfigTld(false),GrayU8.class);
//                break;
//
//            default:
//                throw new RuntimeException("Unknown tracker: "+pos);
//        }
//        setProcessing(new TrackingProcessing(tracker) );
//    }
//
//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//        if( mode == 0 ) {
//            if(MotionEvent.ACTION_DOWN == motionEvent.getActionMasked()) {
//                click0.set((int) motionEvent.getX(), (int) motionEvent.getY());
//                click1.set((int) motionEvent.getX(), (int) motionEvent.getY());
//                mode = 1;
//            }
//        } else if( mode == 1 ) {
//            if(MotionEvent.ACTION_MOVE == motionEvent.getActionMasked()) {
//                click1.set((int)motionEvent.getX(),(int)motionEvent.getY());
//            } else if(MotionEvent.ACTION_UP == motionEvent.getActionMasked()) {
//                click1.set((int)motionEvent.getX(),(int)motionEvent.getY());
//                mode = 2;
//            }
//        }
//        return true;
//    }
//
//    private int setTrackerType(TrackerType type) {
//        switch (type) {
//            case CIRCULANT:
//                return 0;
//            case MEAN_SHIFT:
//                return 1;
//            case MEAN_SHIFT_SCALE:
//                return 2;
//            case MEAN_SHIFT_LIKELIHOOD:
//                return 3;
//            case SPARSE_FLOW:
//                return 4;
//            case TLD:
//                return 5;
//            default:
//                throw new IllegalArgumentException("Unknown");
//        }
//    }
//
//    protected class TrackingProcessing extends DemoProcessingAbstract {
//
//        TrackerObjectQuad tracker;
//        boolean visible;
//
//        Quadrilateral_F64 location = new Quadrilateral_F64();
//
//        Paint paintSelected = new Paint();
//        Paint bluePaint = new Paint();
//        Paint greenPaint = new Paint();
//        Paint yellowPaint = new Paint();
//        Paint whitePaint = new Paint();
//        Paint redPaint = new Paint();
//        private Paint textPaint = new Paint();
//
//        int width,height;
//
//        public TrackingProcessing(TrackerObjectQuad tracker ) {
//            super(tracker.getImageType());
//            mode = 0;
//            this.tracker = tracker;
//
//            paintSelected.setARGB(0xFF/3,0xFF,0xFF,0);
//            paintSelected.setStyle(Paint.Style.FILL_AND_STROKE);
//
//            bluePaint.setColor(Color.BLUE);
//            greenPaint.setColor(Color.GREEN);
//            yellowPaint.setColor(Color.YELLOW);
//            whitePaint.setColor(Color.WHITE);
//            redPaint.setColor(Color.RED);
//
//            // Create out paint to use for drawing
//            textPaint.setARGB(255, 200, 0, 0);
//        }
//
//        private void drawLine( Canvas canvas , Point2D_F64 a , Point2D_F64 b , Paint color ) {
//            canvas.drawLine((float)a.x,(float)a.y,(float)b.x,(float)b.y,color);
//        }
//
//        private void makeInBounds( Point2D_F64 p ) {
//            if( p.x < 0 ) p.x = 0;
//            else if( p.x >= width )
//                p.x = width - 1;
//
//            if( p.y < 0 ) p.y = 0;
//            else if( p.y >= height )
//                p.y = height - 1;
//        }
//
//        private boolean movedSignificantly( Point2D_F64 a , Point2D_F64 b ) {
//            if( Math.abs(a.x-b.x) < MINIMUM_MOTION )
//                return false;
//            if( Math.abs(a.y-b.y) < MINIMUM_MOTION )
//                return false;
//
//            return true;
//        }
//
//        @Override
//        public void initialize(int imageWidth, int imageHeight, int sensorOrientation) {
//            this.width = imageWidth;
//            this.height = imageHeight;
//
//            float density = cameraToDisplayDensity;
//            paintSelected.setStrokeWidth(5f*density);
//            bluePaint.setStrokeWidth(5f*density);
//            greenPaint.setStrokeWidth(5f*density);
//            yellowPaint.setStrokeWidth(5f*density);
//            whitePaint.setStrokeWidth(2.5f*density);
//            redPaint.setStrokeWidth(2.5f*density);
//            textPaint.setTextSize(60*density);
//        }
//
//        @Override
//        public void onDraw(Canvas canvas, Matrix imageToView) {
//
//            canvas.concat(imageToView);
//
//            if( mode == 1 ) {
//                Point2D_F64 a = new Point2D_F64();
//                Point2D_F64 b = new Point2D_F64();
//
//                applyToPoint(viewToImage, click0.x, click0.y, a);
//                applyToPoint(viewToImage, click1.x, click1.y, b);
//
//                double x0 = Math.min(a.x,b.x);
//                double x1 = Math.max(a.x,b.x);
//                double y0 = Math.min(a.y,b.y);
//                double y1 = Math.max(a.y,b.y);
//
//                canvas.drawRect((int) x0, (int) y0, (int) x1, (int) y1, paintSelected);
//            } else if( mode == 2 ) {
//                if (!imageToView.invert(viewToImage)) {
//                    return;
//                }
//                applyToPoint(viewToImage,click0.x, click0.y, location.a);
//                applyToPoint(viewToImage,click1.x, click1.y, location.c);
//
//                // make sure the user selected a valid region
//                makeInBounds(location.a);
//                makeInBounds(location.c);
//
//                if( movedSignificantly(location.a,location.c) ) {
//                    // use the selected region and start the tracker
//                    location.b.set(location.c.x, location.a.y);
//                    location.d.set( location.a.x, location.c.y );
//
//                    visible = true;
//                    mode = 3;
//                } else {
//                    // the user screw up. Let them know what they did wrong
//                    runOnUiThread(() -> Toast.makeText(ObjectTrackerActivity.this,
//                            "Drag a larger region", Toast.LENGTH_SHORT).show());
//                    mode = 0;
//                }
//            }
//
//            if( mode >= 2 ) {
//                if( visible ) {
//
////                    drawLine(canvas,location.a,location.b, bluePaint);
////                    drawLine(canvas,location.b,location.c, greenPaint);
////                    drawLine(canvas,location.c,location.d, yellowPaint);
////                    drawLine(canvas,location.d,location.a, whitePaint);
//
//                    if(uiPlanTree != null){
//
//                        Point2D_F64 imageCenter = getImageCenter(location);
//                        //view = canvas
//                        Point2D_F64 viewCenter = getViewCenter(location, imageToView);
//
//                        int startingXPoint =  (- uiPlanTree.getFocusedNode().getData().getView().getWidth() / 2) + 80 ;
//                        int startingYPoint =  (- uiPlanTree.getFocusedNode().getData().getView().getHeight() / 2) + 80 ;
//
//                        canvas.drawCircle((float) imageCenter.x, (float) imageCenter.y, 8, yellowPaint);
//                        canvas.drawCircle((float) imageCenter.x, (float) imageCenter.y, 5, redPaint);
//
//                        uiPlanTree.setUpTree(startingXPoint,startingYPoint,viewCenter);
//
//                        if(uiPlanTree.getFocusedNode().getParent() != null){
//                            drawTreeUIElementsConnectors(uiPlanTree.getFocusedNode().getParent(),canvas,viewToImage,imageCenter);
//                        }else{
//                            drawTreeUIElementsConnectorsInitState(uiPlanTree.getRoot(),canvas,viewToImage,imageCenter);
//                        }
//
//                    }
//
//                } else {
//                    canvas.drawText("x",width/2,height/2, textPaint);
//                }
//            }
//        }
//
//        private void drawTreeUIElementsConnectorsInitState(UIPlanTree.Node<ARPlanElement> node, Canvas canvas, Matrix viewToImage, Point2D_F64 imageCenter) {
//
//            if(uiPlanTree.isFocusedNode(node)) {
//
//                Point2D_F64 rootAnchor = new Point2D_F64();
//                applyToPoint(viewToImage, node.getData().getView().getX() + node.getData().getView().getWidth()/2,node.getData().getView().getY() + node.getData().getView().getHeight()/2, rootAnchor);
//
//                drawLine(canvas, imageCenter, rootAnchor, redPaint);
//
//                for (UIPlanTree.Node<ARPlanElement> child : node.getChildren()){
//                    Point2D_F64 childAnchor = new Point2D_F64();
//                    applyToPoint(viewToImage, child.getData().getView().getX(),child.getData().getView().getY() + child.getData().getView().getHeight()/2, childAnchor);
//
//                    if(child.getData().getView().getVisibility() == View.VISIBLE){
//                        drawLine(canvas, rootAnchor, childAnchor, redPaint);
//                    }
//
//                    drawTreeUIElementsConnectorsInitState(child,canvas,viewToImage,imageCenter);
//                }
//
//            }else{
//                Point2D_F64 parentAnchor = new Point2D_F64();
//                applyToPoint(viewToImage, node.getData().getView().getX() + node.getData().getView().getWidth(),node.getData().getView().getY() + node.getData().getView().getHeight()/2, parentAnchor);
//
//                for (UIPlanTree.Node<ARPlanElement> child : node.getChildren()){
//                    Point2D_F64 childAnchor = new Point2D_F64();
//                    applyToPoint(viewToImage, child.getData().getView().getX(),child.getData().getView().getY() + child.getData().getView().getHeight()/2, childAnchor);
//
//                    if(child.getData().getView().getVisibility() == View.VISIBLE){
//                        drawLine(canvas, parentAnchor, childAnchor, redPaint);
//                    }
//                }
//
//                for (int i = 0; i < node.getChildren().size(); i++){
//                    drawTreeUIElementsConnectorsInitState(node.getChildren().get(i),canvas,viewToImage,imageCenter);
//                }
//            }
//        }
//
//        private void drawTreeUIElementsConnectors(UIPlanTree.Node<ARPlanElement> node, Canvas canvas, Matrix viewToImage, Point2D_F64 imageCenter) {
//
//            Point2D_F64 parentAnchor = new Point2D_F64();
//            applyToPoint(viewToImage, node.getData().getView().getX() + node.getData().getView().getWidth(),node.getData().getView().getY() + node.getData().getView().getHeight()/2, parentAnchor);
//
//            if( uiPlanTree.isNodeFocusedNodeParent(node)){
//                drawLine(canvas, parentAnchor, imageCenter, redPaint);
//
//                Point2D_F64 focusedNode = new Point2D_F64();
//
//                applyToPoint(viewToImage, uiPlanTree.getFocusedNode().getData().getView().getX() + uiPlanTree.getFocusedNode().getData().getView().getWidth()/2,uiPlanTree.getFocusedNode().getData().getView().getY() + uiPlanTree.getFocusedNode().getData().getView().getHeight()/2, focusedNode);
//
//                drawLine(canvas, imageCenter, focusedNode, redPaint);
//
//            }else {
//
//                for (UIPlanTree.Node<ARPlanElement> child : node.getChildren()) {
//                    Point2D_F64 childAnchor = new Point2D_F64();
//                    applyToPoint(viewToImage, child.getData().getView().getX(), child.getData().getView().getY() + child.getData().getView().getHeight() / 2, childAnchor);
//
//                    if (child.getData().getView().getVisibility() == View.VISIBLE) {
//                        drawLine(canvas, parentAnchor, childAnchor, redPaint);
//                    }
//                }
//            }
//
//            for (int i = 0; i < node.getChildren().size(); i++){
//                drawTreeUIElementsConnectors(node.getChildren().get(i),canvas,viewToImage,imageCenter);
//            }
//        }
//
//        private Point2D_F64 getImageCenter(Quadrilateral_F64 location){
//            double x = (location.c.x + location.a.x)/2;
//            double y = (location.c.y + location.a.y)/2;
//
//            Point2D_F64 point = new Point2D_F64();
//            point.x = x;
//            point.y = y;
//
//            return point;
//        }
//
//        private Point2D_F64 getViewCenter(Quadrilateral_F64 location, Matrix imageToView) {
//            double x = (location.c.x + location.a.x)/2;
//            double y = (location.c.y + location.a.y)/2;
//
//            Point2D_F64 center = new Point2D_F64();
//            applyToPoint(imageToView, x, y, center);
//
//            return center;
//        }
//
//        @Override
//        public void process(ImageBase input) {
//            if( mode == 3 ) {
//                tracker.initialize(input, location);
//                visible = true;
//                mode = 4;
//            } else if( mode == 4 ) {
//                //surfaceLayout.setVisibility(View.INVISIBLE);
//                visible = tracker.process(input,location);
//            }
//        }
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