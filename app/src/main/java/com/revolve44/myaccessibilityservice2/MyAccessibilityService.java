package com.revolve44.myaccessibilityservice2;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.revolve44.myaccessibilityservice2.MainActivity.TAG;
/*
This class needs for get Instagram login and save in xml

 */




public class MyAccessibilityService extends AccessibilityService {

    Map<String, Integer> clickQueueMap = new HashMap<>();

    private static final int CLICK_TYPE_GESTURE = 1;

    MainActivity ma = new MainActivity();

    int logic = 0;
    int control = 0;

    HashSet<String> HashSetLogger = new HashSet<String>();

    List<CharSequence> rawLogs = new ArrayList<>();
    ArrayList<CharSequence> instaLogs = new ArrayList<CharSequence>();




    boolean ok = false;
    boolean enough = false;
    int tst = 0;


    // This method main listener of events

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        Log.d("fetch", fetchAccessibilityDetails(accessibilityEvent));

        validatePendingGestureEvent(accessibilityEvent);

        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source == null) {
            return;
        }
        Log.d("raw", String.valueOf(accessibilityEvent.getText().size()));

        if (accessibilityEvent.getText().size()>0){
            rawLogs = accessibilityEvent.getText();
            Log.d("rawlog0", ""+rawLogs.get(0));

            if (ok & HashSetLogger.size()<3 & !enough & rawLogs.get(0).length()<10){
                if (!String.valueOf(rawLogs).equals("Clock")){
                    HashSetLogger.add(String.valueOf(rawLogs));
                    Log.d("rawlog2", ""+ HashSetLogger.toString());
                }


            }

            if (String.valueOf(rawLogs.get(0)).equals("Instagram")){
                ok = true;
                Log.d("rawlog1", ""+ok);
            }

            if (HashSetLogger.size()==3){
                enough = true;


                Log.d("rawlog3",""+HashSetLogger.toString());

                Toast.makeText(getApplicationContext(),""+HashSetLogger.toString(),Toast.LENGTH_LONG).show();
                findLogin();
            }



        }
        Log.d("rawlog2", ""+ HashSetLogger.toString());
        Log.i(TAG, "onAccessibilityEvent: READ START " + source.getClass().getSimpleName());

        source = getTopParent(source);
        readViewTreeOfEventSource(source);

    }
    // data flow filter from screen of instagram
    private void findLogin() {

        try {
            ArrayList<String> arrayList = new ArrayList<>(HashSetLogger);
//        arrayList.remove("[Clock]");
//        arrayList.remove("[Stopwatch]");
            arrayList.remove("[Grid View]");
            arrayList.remove("Grid View");
            arrayList.remove("[Create New]");
            arrayList.remove("Create New");
            arrayList.remove("[Options]");
            arrayList.remove("Options");
            arrayList.remove("[refresh]");
            arrayList.remove("refresh");

            Log.d("rawlog5",""+arrayList.toString());

            for (int i = 0; i < arrayList.size(); i++){
                if (arrayList.get(i).length()>15){
                    arrayList.remove(i);
                }
            }
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("login",arrayList.get(0)+"");
            editor.apply();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "ERROR in metod findLogin()", Toast.LENGTH_LONG).show();
        }
    }

    List<AccessibilityNodeInfo> findNodeById(AccessibilityNodeInfo source, String id){
        Log.i(MainActivity.TAG, String.format("findNodeById: %s", id));
        return source.findAccessibilityNodeInfosByViewId(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void readViewTreeOfEventSource(AccessibilityNodeInfo source){
        try {
            Log.i(TAG, String.format("getChild: %s", source.getViewIdResourceName() + " dataXXX: "));
            for (int i = 0; i < source.getChildCount(); i++) {
                AccessibilityNodeInfo child = source.getChild(i);
                Log.i(TAG, String.format("getChild: %s", child.getViewIdResourceName() + " data: "));
                readViewTreeOfEventSource(child);
                child.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    AccessibilityNodeInfo getTopParent(AccessibilityNodeInfo accessibilityNodeInfo){
        Log.d(TAG, "getTopParent: " + accessibilityNodeInfo.getViewIdResourceName());

        if(accessibilityNodeInfo.getViewIdResourceName() != null) {
            if (accessibilityNodeInfo.getViewIdResourceName().equals("android:id/content")) {
                Log.d(TAG, "root found");
                return accessibilityNodeInfo;
            }
        }

        if(accessibilityNodeInfo.getParent() != null){
            return getTopParent(accessibilityNodeInfo.getParent());
        } else {
            return accessibilityNodeInfo;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void performManualClick(AccessibilityNodeInfo target){
        if(target.getViewIdResourceName() == null) {
            Log.e(TAG, "performClick: cannot perform click to node without resourceID");
            return;
        }

        Rect rect = new Rect();
        target.getBoundsInScreen(rect);
        Log.i(TAG, String.format("target bounds: %d %d %d %d", rect.left, rect.top, rect.right, rect.bottom));
        float target_width = (float)(rect.right-rect.left);
        float target_height = (float)(rect.bottom-rect.top);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.i(TAG, String.format("display metrics: %d %d ", displayMetrics.heightPixels, displayMetrics.widthPixels));

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        float from_x = rect.left + target_width/10;
        float from_y = rect.top + target_height/10;
        float to_x = from_x + target_width/10;
        float to_y = from_y + target_height/10;
        path.moveTo(from_x, from_y);
        path.moveTo(to_x, to_y);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 100));

        if(checkIfGestureAlreadyExistsInQueue(target, CLICK_TYPE_GESTURE)){
            Log.i(TAG, "performClick: gesture already exists in queue");
            return;
        }

        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.i(TAG, "GestureResultCallback onCompleted: ");
                addGestureInDetectionQueue(target, CLICK_TYPE_GESTURE);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.i(TAG, "GestureResultCallback onCancelled: ");
            }
        }, null);
    }

    boolean checkIfGestureAlreadyExistsInQueue(AccessibilityNodeInfo accessibilityNodeInfo, int gestureType){
        for (Map.Entry<String, Integer> map : clickQueueMap.entrySet()) {
            if (accessibilityNodeInfo.getViewIdResourceName().equals(map.getKey())
                    && map.getValue() == gestureType) {
                return true;
            }
        }
        return false;
    }

    private void validatePendingGestureEvent(AccessibilityEvent accessibilityEvent) {
        if(accessibilityEvent.getSource() == null
                || accessibilityEvent.getSource().getViewIdResourceName() == null
                || clickQueueMap.size() < 1){
            return;
        }
        switch (accessibilityEvent.getEventType()){
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if(accessibilityEvent.getSource().getViewIdResourceName() != null) {
                    for (Map.Entry<String, Integer> map : clickQueueMap.entrySet()) {
                        if (accessibilityEvent.getSource().getViewIdResourceName().equals(map.getKey())
                                && map.getValue() == CLICK_TYPE_GESTURE) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                clickQueueMap.remove(map.getKey(), map.getValue());
                            }
                            Log.i(TAG, "validatePendingGestureEvent: ");
                            return;
                        }
                    }
                }
            default:
                return;
        }
    }

    void addGestureInDetectionQueue(AccessibilityNodeInfo target, int gestureType){
        clickQueueMap.put(target.getViewIdResourceName(), gestureType);
    }


    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt: ");
    }

    static String fetchAccessibilityDetails(AccessibilityEvent accessibilityEvent){

        return String.format(
                "AccessibilityEvent:\t %s"+
                        "\naccessibilityEvent.getSource():\t%s" +
                        "\naccessibilityEvent.getAction():\t%s" +
                        "\naccessibilityEvent.describeContents():\t%s" +
                        "\naccessibilityEvent.getContentChangeTypes():\t%s" +
                        "\naccessibilityEvent.getEventTime():\t%s" +
                        "\naccessibilityEvent.getEventType():\t%s" +
                        "\naccessibilityEvent.getMovementGranularity():\t%s" +
                        "\naccessibilityEvent.getPackageName():\t%s"+
                        "\naccessibilityEvent.getWindowChanges():\t%s"
                /*"\naccessibilityEvent.getRecordCount():\t%s"*/,
                accessibilityEvent.toString(),
                accessibilityEvent.getSource(),
                accessibilityEvent.getAction(),
                accessibilityEvent.describeContents(),
                accessibilityEvent.getContentChangeTypes(),
                accessibilityEvent.getEventTime(),
                accessibilityEvent.getEventType(),
                accessibilityEvent.getMovementGranularity(),
                accessibilityEvent.getPackageName(),
               // accessibilityEvent.getWindowChanges(),
                accessibilityEvent.getRecordCount()
        );
    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = getServiceInfo();
//        info.packageNames = new String[]
//                {
//                        "com.flipkart.android", "com.myntra.android", "com.whatsapp", "com.facebook.orca",
//                        "com.msf.kbank.mobile", "bpr10.git.voodosample", "com.facebook.katana", "net.one97" +
//                        ".paytm", "com.ubercab", "com.nianticlabs.pokemongo", "com.phonepe.app",
//                        "in.co.zuka.myapplication"
//                };
        info.packageNames = null;
//        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED |
//                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;

//        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;

        info.notificationTimeout = 0;

        this.setServiceInfo(info);
    }

}
