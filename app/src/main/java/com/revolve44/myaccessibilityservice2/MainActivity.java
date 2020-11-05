package com.revolve44.myaccessibilityservice2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "mCus";
    private TextView root_accessibility_tv;
    private Button btnSettings;
    private TextView output;
    private TextView output2;

    private Button two;
    private Button three;

    int width = 0;
    int height = 0;

    int wcoeff = 0;
    int hcoeff = 0;

    int log = 0;

    String PACKAGENAME = "com.instagram.android";
    //com.revolve44.instagrammm
    //com.instagram.android
    //com.revolve44.mywindturbine

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root_accessibility_tv = findViewById(R.id.root_accessibility_tv);
        btnSettings = findViewById(R.id.button_settings);
        output = findViewById(R.id.output);
        output2 = findViewById(R.id.output2);

        two = findViewById(R.id.button2);
        three = findViewById(R.id.button3);

        if (Build.VERSION.SDK_INT<24){
            Toast.makeText(getApplicationContext(), "You`re android API <24 - Possible errors!",Toast.LENGTH_LONG).show();
        }

        btnSettings.setOnClickListener((view) -> {
            switch (view.getId()) {
                case R.id.button_settings: {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    break;
                }
            }
        });
        two.setOnClickListener((view -> {


            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode

            log = pref.getInt("log",7);

            for (int i = 0; i<1; i++){
                output.append(""+pref.getString("login","xxx")+"\n");
            }
        }));

        three.setOnClickListener((view -> {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("login");

            editor.apply(); // commit changes


        }));
    }


    @Override
    protected void onResume() {
        super.onResume();
//        int log = 0;
//        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
//
//        log = pref.getInt("log",0);
//        for (int i = 0; i<log; i++){
//            // output.append(""+pref.getString("key"+i,"//")+"\n");
//        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (isAccessibilityEnabled()) {
            btnSettings.setVisibility(View.GONE);
            root_accessibility_tv.setText("Accessibility permissions are granted");
        } else {
            btnSettings.setVisibility(View.VISIBLE);
            root_accessibility_tv.setText("Provide accessibility permissions");
        }
    }

    public boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(MainActivity.TAG,
                    "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled != 1) {
            return false;
        }
        String settingValue = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue != null) {
            TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
            splitter.setString(settingValue);
            while (splitter.hasNext()) {
                String accessabilityService = splitter.next();
                if (accessabilityService.equalsIgnoreCase(
                        "in.co.zuka.myaccessibilityservice2/in.co.zuka.myaccessibilityservice2.MyAccessibilityService")) {
                    return true;
                }
            }
        }
        return false;
    }

    // launch Instagram app
    public void insta(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        width = size.x;
        height = size.y;
        Log.i("piz", "" + width+" "+height);

        editor.putInt("wc",wcoeff);
        editor.putInt("hc",hcoeff);

        editor.putInt("w",width);
        editor.putInt("h",height);
        editor.apply();

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(PACKAGENAME);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }

        try {
            startService(new Intent(MainActivity.this, AutoService.class));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "ERROR:"+e.getMessage(),Toast.LENGTH_LONG).show();

        }

    }
}
