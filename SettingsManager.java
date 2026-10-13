package com.shadowradar.pro;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "shadow_radar_prefs";

    public SettingsManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setBooleanSetting(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void setIntSetting(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public int getIntSetting(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void reset() {
        preferences.edit().clear().apply();
    }
}