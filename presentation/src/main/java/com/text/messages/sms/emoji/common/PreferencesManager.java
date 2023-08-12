package com.text.messages.sms.emoji.common;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    public static String PREF_NAME = "textmessaging";
    public static SharedPreferences AppPreference;
    public static final String SHARED_PREFS_LANGUAGE = "shared_pref_language";
    public static final String SHARED_PREFS_Start = "shared_pref_start";
    public static final String SHARED_PREFS_Rate = "shared_pref_rate";

    public static void setLanguage(Context context, String language) {
        AppPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = AppPreference.edit();
        editor.putString(SHARED_PREFS_LANGUAGE, language).apply();
    }

    public static String getLanguage(Context context) {
        AppPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return AppPreference.getString(SHARED_PREFS_LANGUAGE, "");
    }

    public static void setStart(Context context, boolean language) {
        AppPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = AppPreference.edit();
        editor.putBoolean(SHARED_PREFS_Start, language).apply();
    }

    public static boolean getStart(Context context) {
        AppPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return AppPreference.getBoolean(SHARED_PREFS_Start, false);
    }
}
