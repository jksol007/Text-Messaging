package com.text.messages.sms.emoji.ads;

import static com.text.messages.sms.emoji.common.util.RateMeNowDialog.MAIL_TO_FEEDBACK;
import static com.text.messages.sms.emoji.common.util.RateMeNowDialog.PRIVACY_POLICY_LINK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.text.messages.sms.emoji.BuildConfig;
import com.text.messages.sms.emoji.R;
import com.text.messages.sms.emoji.common.util.PreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class AppUtils {

    private static ProgressDialog progressDialog;
    private static final String TAG = AppUtils.class.getSimpleName();
    private static SharedPreferences sp;
    public static final String mypreference = BuildConfig.APPLICATION_ID + ".preferences";
    public static final String IS_APP_PURCHASED = "IS_APP_PURCHASED";

    public static boolean isActivityActive(Activity activity) {
        if (null != activity)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                return !activity.isFinishing() && !activity.isDestroyed();
            else return !activity.isFinishing();
        return false;
    }

    public static boolean isContextActive(Context context) {
        if (null != context)
            if (context instanceof Activity) {
                return isActivityActive((Activity) context);
            } else {
                return true;
            }
        return false;
    }

    public static boolean isNetworkNotAvailable(Context context) {
        boolean isAvailable = false;
        try {
            ConnectivityManager manager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                isAvailable = true;
            }
        } catch (Exception ignored) {

        }
        return !isAvailable;
    }

    public static void showProgressDialog(Context context, int resID) {
        try {
            if (context != null) {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage(context.getString(resID));
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(false);
                    progressDialog.show();
                } else {
                    progressDialog.setMessage(context.getString(resID));
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(false);

                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                }
            }
        } catch (Throwable throwable) {
//            Log.i(TAG, "showProgressBar: throwable :- " + throwable.getMessage());
        }
    }

    public static void hideProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Throwable ignored) {

        }
        progressDialog = null;
    }

    public static void gotoFeedBack(Context context, int rating, String s) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") ||
                    info.activityInfo.name.toLowerCase().contains("gmail")) best = info;

        String versionName = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName + "." + packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{MAIL_TO_FEEDBACK});
        if (rating == 0 && s == null) {
            intent.putExtra(Intent.EXTRA_TEXT, "Version :- " + versionName);
        } else if (rating != -1) {
            intent.putExtra(Intent.EXTRA_TEXT, "Version :- " + versionName + "\n Rating :- " + rating + "\n\n " + s);//Add mail body
        } else {
            intent.putExtra(Intent.EXTRA_TEXT, "Version :- " + versionName);
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + context.getString(R.string.app_name) + " app");
        intent.setPackage("com.google.android.gm");

        if (best != null)
            intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);

        try {
            context.startActivity(intent);
            PreferencesManager.setRateUs(context, true);
        } catch (ActivityNotFoundException exception) {
            // Only browser apps are available, or a browser is the default app for this intent
        }
    }

    public static void setLightStatusBarColor(View view, Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            view.setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.ads_install_color));
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.bg_setting));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.bg_setting));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.white));
        }
    }

    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim().equals("") || text.trim()
                .length() <= 0);
    }

    public static void setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static void openPrivacyPolicy(Context context) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_LINK));
            context.startActivity(browserIntent);
        } catch (Throwable throwable) {
            Log.i(TAG, "openPrivacyPolicy: " + throwable.getMessage());
        }
    }

    public static boolean getBooleanValue(Context c, String key, boolean value) {
        sp = c.getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        return sp.getBoolean(key, value);
    }

    public static void goToFeedBack(@NotNull Activity context) {
        StringBuilder stringBuilder = new StringBuilder("\n\n");
        stringBuilder.append("\n\n--- Tell Us Your Thoughts above this line ---\n\n");
        stringBuilder.append("Package: " + context.getPackageName() + "\n");
        stringBuilder.append("Version: " + BuildConfig.VERSION_NAME + "\n");
        stringBuilder.append("Version Code: " + BuildConfig.VERSION_CODE + "\n");
        stringBuilder.append("Device: " + Build.BRAND + " " + Build.MODEL + "\n");
        stringBuilder.append("SDK: " + Build.VERSION.SDK_INT + "\n");
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, MAIL_TO_FEEDBACK);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + " Support");
        intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
        context.startActivity(intent);
    }
}
