package org.horaapps.leafpic.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;


 final class SharedPrefs {

    private static final String PREFERENCES_NAME = "org.horaapps.leafpic.SHARED_PREFS";
    private static final int PREFERENCES_MODE = Context.MODE_PRIVATE;

    private final SharedPreferences sharedPrefs;

     SharedPrefs(@NonNull Context context) {
        sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, PREFERENCES_MODE);
    }

    @NonNull
    private SharedPreferences.Editor getEditor() {
        return sharedPrefs.edit();
    }

    int get(@NonNull String key, int defaultValue) {
        return sharedPrefs.getInt(key, defaultValue);
    }

    void put(@NonNull String key, int value) {
        getEditor().putInt(key, value).commit();
    }

    boolean get(@NonNull String key, boolean defaultValue) {
        return sharedPrefs.getBoolean(key, defaultValue);
    }

    void put(@NonNull String key, boolean value) {
        getEditor().putBoolean(key, value).commit();
    }
}
