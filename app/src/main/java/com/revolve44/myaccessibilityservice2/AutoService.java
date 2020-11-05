package com.revolve44.myaccessibilityservice2;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/*
This class needs for make auto click

 */

public class AutoService extends AccessibilityService {
    private Handler mHandler;
    private int mX;
    private int mY;
    int width = 0;
    int height = 0;

    int wcoeff = 0;
    int hcoeff = 0;


    @Override
    public void onCreate() {
        super.onCreate();


        // get sizes of screen
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode

        width = pref.getInt("w",0);
        height = pref.getInt("h",0);

        wcoeff = pref.getInt("ws",0);
        hcoeff = pref.getInt("hs",0);




        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

    }

    @Override
    protected void onServiceConnected() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","SERVICE STARTED");
        if(intent!=null){
            String action = intent.getStringExtra("action");
           // if (action.equals("play")) {
//                mX = intent.getIntExtra("x", 60);
//                //Log.d("x_value",Integer.toString(mX));
//                mY = intent.getIntExtra("y", 60);
//                mX = 60;
//                mY = 60;
                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }
                //playTap(mX,mY);
                //mHandler.postDelayed(mRunnable, 1000);
                mHandler.post(mRunnable);
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
            //}
//            else if(action.equals("stop")){
//                mHandler.removeCallbacksAndMessages(null);
//            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //generate Click
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void playTap(int x, int y) {
        //Log.d("TAPPED","STARTED TAPpING");
        Toast.makeText(getApplicationContext(),"playTap x = "+x+" "+y, Toast.LENGTH_LONG).show();
        Path swipePath = new Path();
        Path swipePath2 = new Path();
        swipePath.moveTo(x, y);
        //swipePath.moveTo(400, 80);
        swipePath.lineTo(x, y);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 2000));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d("Gesture Completed","Gesture Completed");
                    super.onCompleted(gestureDescription);
                    //mHandler.postDelayed(mRunnable, 1);
                    mHandler.post(mRunnable);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    //Log.d("Gesture Cancelled","Gesture Cancelled");
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }
        //Log.d("hi","hi?");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }


    @Override
    public void onInterrupt() {
    }


    private IntervalRunnable mRunnable;
    int a = 0;

    // Start generating of click
    private class IntervalRunnable implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            //Log.d("clicked","click");
            Log.d("in RUNN  w and h", "" +width+" "+height);
            if (width !=0){

                a++;
                if (a < 6){
                    playTap((int) (width*0.8), (int) (height*0.7));

                }

                if (a>=6 &a< 22){
                    playTap(((width*2)/7), 200);
                    a++;
                }
                if (a>21){
                    a=0;
                }

            }

        }
    }
}

