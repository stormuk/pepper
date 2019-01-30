//package com.storm.abod3;
//
//import android.content.Context;
//import android.graphics.drawable.GradientDrawable;
//import android.view.View;
//import android.widget.TextView;
//
//import com.storm.experiment1.R;
//
//class ARPlanElement {
//
//    private final String name;
//    private TextView element;
//    private GradientDrawable drawableBackground;
//    private String uIName;
//
//    public ARPlanElement(Context applicationContext, int priority, String name, int borderColor){
//
//        this.name = name;
//
//        if(priority != 0){
//            this.uIName = Integer.toString(priority)+": "+name;
//        }else{
//            this.uIName = name;
//        }
//
//        this.element = (TextView) View.inflate(applicationContext, R.layout.plan_element, null);
//
//        this.element.setText(uIName);
//
//        this.drawableBackground = (GradientDrawable) element.getBackground();
//        this.drawableBackground.setStroke(2, borderColor);
//
//    }
//
//    public View getView() {
//        return element;
//    }
//
//    public void setBackgroundColor(int color){
//        drawableBackground.setColor(color);
//    }
//
//    public String getName(){
//        return name;
//    }
//
//}
