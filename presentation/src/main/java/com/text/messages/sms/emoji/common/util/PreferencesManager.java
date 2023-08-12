package com.text.messages.sms.emoji.common.util;

import static com.text.messages.sms.emoji.common.util.RateMeNowDialog.PLAY_STORE_LINK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class PreferencesManager {

    public final static String SHARED_PREFS = "smart_messages";
    public final static String SHARED_PREFS_RATE_US = "shared_pref_rate_us";

    public static void setRateUs(Context context, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHARED_PREFS_RATE_US, value);
        editor.commit();
    }

    public static boolean getRate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getBoolean(SHARED_PREFS_RATE_US, false);
    }

    public static void RateUs(Context context) {
//        try {
//            Uri playstoreuri1 = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
//            Intent playstoreIntent1 = new Intent(Intent.ACTION_VIEW, playstoreuri1);
//            context.startActivity(playstoreIntent1);
//        } catch (Exception exp) {
            Uri playstoreuri2 = Uri.parse(PLAY_STORE_LINK + context.getPackageName());
            Intent playstoreIntent2 = new Intent(Intent.ACTION_VIEW, playstoreuri2);
            context.startActivity(playstoreIntent2);
//        }
    }

}
