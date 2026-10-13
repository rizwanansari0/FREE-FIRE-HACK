package com.shadowradar.pro;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private RadarView radarView;
    private SettingsManager settingsManager;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen, no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize managers
        settingsManager = new SettingsManager(this);
        soundManager = new SoundManager(this);

        // Create main layout
        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        mainLayout.setBackgroundColor(0xFF0A0E27); // Dark blue-black

        // Create radar view
        radarView = new RadarView(this, settingsManager, soundManager);
        mainLayout.addView(radarView);

        setContentView(mainLayout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (radarView != null) {
            radarView.onTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (radarView != null) {
            radarView.pause();
        }
        if (soundManager != null) {
            soundManager.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (radarView != null) {
            radarView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (radarView != null) {
            radarView.stop();
        }
        if (soundManager != null) {
            soundManager.release();
        }
    }
}