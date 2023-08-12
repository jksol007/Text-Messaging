package com.jksol.appmodule.ads;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.jksol.appmodule.R;
import com.jksol.appmodule.update.AppUpdate;
import com.jksol.appmodule.utils.Constants;

import java.util.Date;
import java.util.List;

public class AppOpenManagerNew implements Application.ActivityLifecycleCallbacks {
    private final String LOG_TAG = "AppOpenManager";
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private  Application myApplication;
    private Activity currentActivity;
    public static boolean isShowingAd = false;
    private long loadTime = 0;
    boolean isBackToFor = false;
    boolean isFor = true;
    public boolean isFirstTime = false;
    public boolean isLoading = false;
//    public boolean isPurchased1;
    public String APP_OPEN_ID;

//    public AppOpenManagerNew(boolean isPurchased){
//
//        isPurchased1=isPurchased;
//        Log.e("TAG51", "AppOpenManagerNew: "+isPurchased1 );
//
//    }
    public AppOpenManagerNew(Application myApplication, String APP_OPEN_ID) {
        Log.e("TAG51", "AppOpenManagerNew: "+APP_OPEN_ID);
        this.APP_OPEN_ID = APP_OPEN_ID;
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);

        Constants.Companion.setAppName(myApplication.getResources().getString(myApplication.getApplicationInfo().labelRes));

        AppLifecycleObserver appLifecycleObserver = new AppLifecycleObserver(new AppOpenLifeCycleChange() {
            @Override
            public void onForeground() {

                    Constants.Companion.setAppInBackground(false);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        Log.e(LOG_TAG, "onForeground: "+(!Constants.Companion.isSplashScreen()) );
                        if (!Constants.Companion.isSplashScreen()) {
                            Log.e(LOG_TAG, "onForeground: "+Constants.Companion.isPurchase() );
                            appInForeground();
                        }
                    }


            }

            @Override
            public void onBackground() {
                Constants.Companion.setAppInBackground(true);
            }
        });
        ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {

    }

    public boolean isNetworkAvailable(Context c) {
        ConnectivityManager manager = (ConnectivityManager)
                c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void fetchAd(String appOpenId) {
        if (TextUtils.isEmpty(appOpenId)) {
            return;
        }
        if (isNetworkAvailable(myApplication)) {
            if (!isLoading) {
                if (isAdAvailable()) {
                    showAdIfAvailable();
                    return;
                }
            } else {
                return;
            }
            isLoading = true;
            Log.d(LOG_TAG, "onLOadStart");

            loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    super.onAdLoaded(appOpenAd);
                    AppOpenManagerNew.this.appOpenAd = appOpenAd;
                    isLoading = false;
                    showAdIfAvailable();
                    Log.d(LOG_TAG, "onAdLoaded: " + appOpenAd.getAdUnitId());
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    isLoading = false;

                    Log.e("AppOpenManagerNew", "onAdFailedToLoad: " + loadAdError.getMessage());
                    System.out.println("AD OPEN LOADED : " + loadAdError.getMessage());
                    Log.e(LOG_TAG, "::ERRR" + loadAdError.getMessage() + "::" + loadAdError.getCode());


                    if (!Constants.Companion.isUpdateDialogVisible()) {
                        Log.e(LOG_TAG, "::Done");
                        AppUpdate appUpdate = new AppUpdate(currentActivity);
                        appUpdate.checkForUpdate();
                    }
                    fetchAdFailed(currentActivity.getResources().getString(R.string.admob_app_start_ads_id));
                }
            };

            Log.e(LOG_TAG, "Req");

            AdRequest request = getAdRequest();
            AppOpenAd.load(
                    myApplication, appOpenId, request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);

        }
    }


    public void fetchAdFailed(String appOpenId) {


        if (TextUtils.isEmpty(appOpenId)) {
            return;
        }
        if (isNetworkAvailable(myApplication)) {
            if (!isLoading) {
                if (isAdAvailable()) {
                    showAdIfAvailable();
                    return;
                }
            } else {
                return;
            }
            isLoading = true;
            Log.d(LOG_TAG, "onLOadStart");

            loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    super.onAdLoaded(appOpenAd);
                    AppOpenManagerNew.this.appOpenAd = appOpenAd;
                    isLoading = false;
                    showAdIfAvailable();
                    Log.d(LOG_TAG, "onAdLoaded: " + appOpenAd.getAdUnitId());
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    isLoading = false;

                    Log.e("AppOpenManagerNew", "onAdFailedToLoad: " + loadAdError.getMessage());
                    System.out.println("AD OPEN LOADED : " + loadAdError.getMessage());
                    Log.e(LOG_TAG, "::ERRR" + loadAdError.getMessage() + "::" + loadAdError.getCode());


                    if (!Constants.Companion.isUpdateDialogVisible()) {
                        Log.e(LOG_TAG, "::Done");
                        AppUpdate appUpdate = new AppUpdate(currentActivity);
                        appUpdate.checkForUpdate();
                    }

                }
            };

            Log.e(LOG_TAG, "Req");

            AdRequest request = getAdRequest();
            AppOpenAd.load(
                    myApplication, appOpenId, request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);

        }
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    public void showAdIfAvailable() {
        if (!Constants.Companion.isSplashScreen()) {
            if (!isShowingAd) {
                if (isAdAvailable()) {
                    Log.d(LOG_TAG, "Will show ad.");
                    FullScreenContentCallback fullScreenContentCallback =
                            new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    appOpenAd = null;
                                    isShowingAd = false;
//                                    if (!Constants.Companion.isUpdateDialogVisible()) {
//                                        AppUpdate appUpdate = new AppUpdate(currentActivity);
//                                        appUpdate.checkForUpdate();
//                                    }
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    Log.d(LOG_TAG, "Error display show ad." + adError.getMessage());
                                    isShowingAd = false;
                                    if (!Constants.Companion.isUpdateDialogVisible()) {
                                        AppUpdate appUpdate = new AppUpdate(currentActivity);
                                        appUpdate.checkForUpdate();
                                    }
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    isShowingAd = true;
                                }
                            };

                    Log.d(LOG_TAG, String.valueOf(currentActivity));
                    appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                    appOpenAd.show(currentActivity);
                    isShowingAd = true;

                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        Log.d(LOG_TAG, "Can not show ad.");
                        Log.e(LOG_TAG, "showAdIfAvailable: "+Constants.Companion.isPurchase() );
                        if (!Constants.Companion.isPurchase()){
                            fetchAd(APP_OPEN_ID);
                        }

//                        fetchAd(APP_OPEN_ID);
                    }
                }
            }
        } else {
            if (!Constants.Companion.isUpdateDialogVisible()) {
                AppUpdate appUpdate = new AppUpdate(currentActivity);
                appUpdate.checkForUpdate();
            }
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }


    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
        if (isApplicationBroughtToBackground()) {
            isFor = false;
        } else {
            if (!isFor) {
                isBackToFor = true;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                    if (!Constants.Companion.isSplashScreen()) {


                        Log.e(LOG_TAG, "onActivityResumed: "+Constants.Companion.isPurchase() );
                        if (!Constants.Companion.isPurchase()){
                            fetchAd(APP_OPEN_ID);
                        }


                    }
                }
                Log.d(LOG_TAG, "onStartRESUMEE");
            } else {
                isBackToFor = false;
            }
            isFor = true;
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        currentActivity = activity;
        if (isApplicationBroughtToBackground()) {
            isFor = false;
            if (isFirstTime) {
                isFirstTime = false;
            }
//            fetchAd(myApplication.getString(R.string.app_open_id));
        } else {
            if (!isFor) {
                isBackToFor = true;
//                if (!Constants.isSplashScreen) {
//                    fetchAd(activity.getString(R.string.open_id));
//
//                }
                Log.d(LOG_TAG, "onStart");


            } else {
                isBackToFor = false;
            }
            isFor = true;
        }
    }


    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        currentActivity = activity;
        Log.e("ONBACKKKKKK", "BACKKKK:6666");
        if (isApplicationBroughtToBackground()) {
            isFor = false;
        } else {
            if (!isFor) {
                isBackToFor = true;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                    if (!Constants.Companion.isSplashScreen()) {
                        Log.e(LOG_TAG, "onActivityPostResumed: "+Constants.Companion.isPurchase() );
                        if (!Constants.Companion.isPurchase()){
                            fetchAd(APP_OPEN_ID);
                        }





                    }
                }
                Log.d(LOG_TAG, "onStartRESUMEE");
            } else {
                isBackToFor = false;
            }
            isFor = true;
        }
    }


    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle
            savedInstanceState) {
    }

    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) myApplication.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(myApplication.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle
            outState) {

    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle
            outState) {

    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {

    }

    public void appInForeground() {

        if (!Constants.Companion.isSplashScreen()) {

            Log.e(LOG_TAG, "onForeground: "+Constants.Companion.isPurchase() );
            if (!Constants.Companion.isPurchase()){
                fetchAd(APP_OPEN_ID);
            }


        }
    }
}
