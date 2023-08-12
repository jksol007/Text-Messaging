package com.text.messages.sms.emoji.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.text.messages.sms.emoji.BuildConfig;
import com.text.messages.sms.emoji.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.inject.Singleton;


/**
 * Author: BHAVEN SHAH
 * Date: 01-09-2021
 * Organization: Erasoft Technology
 * Email: erasoft.bhaven@gmail.com
 */

/*public class AdvertiseHandler implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    private final String TAG = AdvertiseHandler.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static AdvertiseHandler sInstance;
    private Application myApplication;
    private final List<NativeAd> nativeAdList = new ArrayList<>();
    private boolean isAppOpen = false;
    private boolean isShowingAd = false;

    private boolean isAppOpenAdEnable = false;
    private final List<OnCompleteListener> nativeAdLoadCompleteList = new ArrayList<>();
    public boolean isAppStartUpAdsEnabled = true;

    private InterstitialAd mInterstitialAd;
    private boolean interstitialAdsLoading = false;

    private AppOpenAd appOpenAd = null;
    private boolean isAppOpenAdLoading = false;
    private long appOpenLoadTime = 0L;
    private final long ADS_MAX_LOAD_TIME = 5000L;
    private final int MAX_NATIVE_ADS_STORE = 4;
    public boolean isNeedOpenAdRequest = false;
    private Activity activity;
    private boolean isNativeAdsLoading = false;

    private AdsLoadsListener appAdsListener;
    private final Random randomGenerator;

    private final boolean loadOnFailed = true;
    private final List<String> bannerAdsIDList = new ArrayList<>();
    private final List<String> interstitialAdsIDList = new ArrayList<>();
    private final List<String> nativeAdsIDList = new ArrayList<>();
    private final List<String> appOpenAdsIDList = new ArrayList<>();
    public boolean isSplashAdShown = false;

    @Singleton
    private AdvertiseHandler(@NonNull Application application) {
        this();

        myApplication = application;
        myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Singleton
    private AdvertiseHandler() {

        randomGenerator = new Random();

        bannerAdsIDList.clear();
        interstitialAdsIDList.clear();
        nativeAdsIDList.clear();
        appOpenAdsIDList.clear();

        bannerAdsIDList.add("/22387492205,22667063805/com.chating.messages.text.messaging.Banner0.1650441468");
        interstitialAdsIDList.add("/22387492205,22667063805/com.chating.messages.text.messaging.Interstitial0.1650441518");
        nativeAdsIDList.add("/22387492205,22667063805/com.chating.messages.text.messaging.Native0.1650441490");
        appOpenAdsIDList.add("/22387492205,22667063805/com.chating.messages.text.messaging.AppOpen0.1650441535");

        if (BuildConfig.DEBUG) {
            List<String> testDeviceIds = new ArrayList<>();
            testDeviceIds.add("25555CF3B61C05BDA80C839A7D9A20EC");
            testDeviceIds.add("1685D79BA73277929C48A9FCCD3336AA");
            testDeviceIds.add("13360D68428B5437B7006DE167FD90EC");
            testDeviceIds.add("79D0A9065DC733DF4E5FE2349926C84C");
            testDeviceIds.add("8153549C826A9696C1923E874F61E887");
            testDeviceIds.add("E80FAB95779C3663F40B59EC5A8AA783");
            RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
        }
    }

    @NonNull
    public static AdvertiseHandler getInstance(@NonNull Application application) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String process = Application.getProcessName();
            if (!application.getPackageName().equals(process))
                WebView.setDataDirectorySuffix(process);
        }
        MobileAds.initialize(application.getApplicationContext());

        if (sInstance == null) {
            synchronized (AdvertiseHandler.class) {
                if (sInstance == null) {
                    sInstance = new AdvertiseHandler(application);
                }
            }
        }

        return sInstance;
    }

    @NonNull
    public static AdvertiseHandler getInstance() {
        if (sInstance == null) {
            synchronized (AdvertiseHandler.class) {
                if (sInstance == null) {
                    sInstance = new AdvertiseHandler();
                }
            }
        }

        return sInstance;
    }

    private synchronized void Log(String logMessage) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, logMessage);
        }
    }

    @NonNull
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public void showProgress(@NonNull final Activity activity) {
        if (AppUtils.isContextActive(activity)) {
            activity.runOnUiThread(() -> AppUtils.showProgressDialog(activity, R.string.ads_loading_msg));
        }
    }

    public void hideProgress(@NonNull final Activity activity) {
        if (AppUtils.isContextActive(activity)) {
            activity.runOnUiThread(AppUtils::hideProgressDialog);
        }
    }

    private boolean isNetworkNotAvailable(@NonNull final Context context) {
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

    private boolean isPurchased(@NonNull final Context context) {
//        boolean purchase = PreferenceHelper.getBooleanValue(context, PreferenceHelper.IS_APP_PURCHASED, false);
//        Log( "isPurchased: purchase :- " + purchase);
//        return purchase;
        return false;
    }

    *//* TODO Banner ads *//*
    public void loadBannerAds(@NonNull final Context context, final int index, @Nullable final ViewGroup bannerAdLayout, @Nullable final AdListener adListener) {
        loadBannerAds(context, context.getString(R.string.admob_banner_ads_id), index, bannerAdLayout, adListener);
    }

    private void loadBannerAds(@NonNull final Context context, @NonNull final String bannerID, final int index, @Nullable final ViewGroup bannerAdLayout, @Nullable final AdListener adListener) {

        if (!AppUtils.isContextActive(context) || isShowingAd) {
            Log("loadBannerAds: context :- " + context + " isShowingAd :- " + isShowingAd);
            if (adListener != null) {
                adListener.onAdFailedToLoad(new LoadAdError(-2, "context null", "", null, null));
            }
            return;
        }

        if (isPurchased(context)) {
            Log("loadBannerAds: isPurchased ");
            if (adListener != null) {
                adListener.onAdFailedToLoad(new LoadAdError(-4, "purchased", "", null, null));
            }
            return;
        }

        if (isNetworkNotAvailable(context)) {
            Log("loadBannerAds: no network");
            if (adListener != null) {
                adListener.onAdFailedToLoad(new LoadAdError(-3, "network error", "", null, null));
            }
            return;
        }

        if (bannerAdLayout == null) {
            Log("loadBannerAds: bannerAdLayout NULL");
            if (adListener != null) {
                adListener.onAdFailedToLoad(new LoadAdError(-1, "null", "", null, null));
            }
            return;
        }

        AdView adView = new AdView(context);
        adView.setAdUnitId(bannerID);
        bannerAdLayout.addView(adView);

        AdSize adSize = getAdSize(context);
        adView.setAdSize(adSize);
        Log("loadBannerAds: loading");

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();

                bannerAdLayout.setVisibility(View.GONE);

                if (adListener != null) {
                    adListener.onAdClosed();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log("onAdFailedToLoad: loadBannerAds loadAdError :- " + loadAdError.getMessage());

                if (loadOnFailed && index >= 0 && bannerAdsIDList.size() > index) {
                    loadBannerAds(context, bannerAdsIDList.get(index), index + 1, bannerAdLayout, adListener);
                } else {
                    if (adListener != null) {
                        adListener.onAdFailedToLoad(loadAdError);
                    } else {
                        bannerAdLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log("onAdLoaded: loadBannerAds");

                bannerAdLayout.setVisibility(View.VISIBLE);

                if (adListener != null) {
                    adListener.onAdLoaded();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();

                if (adListener != null) {
                    adListener.onAdOpened();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();

                if (adListener != null) {
                    adListener.onAdClicked();
                }
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();

                if (adListener != null) {
                    adListener.onAdImpression();
                }
            }
        });

        adView.loadAd(getAdRequest());
    }

    @NonNull
    private AdSize getAdSize(@NonNull Context context) {

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    *//* TODO Interstitial ads *//*

    public void loadInterstitialAds(@NonNull final Context context) {
        loadInterstitialAds(context, null);
    }

    public void loadInterstitialAds(@NonNull final Context context, @Nullable final AdsLoadsListener adsListener) {
        loadInterstitialAds(context, context.getString(R.string.admob_interstitial_ads_id), 0, adsListener);
    }

    private void loadInterstitialAds(@NonNull final Context context, @NonNull final String interstitialID, final int index, @Nullable final AdsLoadsListener adsListener) {
        this.appAdsListener = adsListener;

        if (!interstitialAdsLoading) {

            if (!AppUtils.isContextActive(context) || isShowingAd) {
                Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + isShowingAd);
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isPurchased(context)) {
                Log("onAdFailedToLoad: loadInterstitialAds isPurchased");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isNetworkNotAvailable(context)) {
                Log("onAdFailedToLoad: loadInterstitialAds no network");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (mInterstitialAd != null) {
                Log("onAdLoaded: loadInterstitialAds already");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdLoaded();
                    appAdsListener = null;
                }
                return;
            }

            interstitialAdsLoading = true;
            Log("loadInterstitialAds: interstitialAdsLoading ");

            InterstitialAd.load(context, interstitialID, getAdRequest(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    Log("onAdLoaded: loadInterstitialAds ");
                    mInterstitialAd = interstitialAd;
                    interstitialAdsLoading = false;

                    if (appAdsListener != null) {
                        appAdsListener.onAdLoaded();
                    }
                    appAdsListener = null;
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log("onAdFailedToLoad: loadInterstitialAds loadAdError :- " + loadAdError.getMessage());

                    interstitialAdsLoading = false;
                    mInterstitialAd = null;

                    if (loadOnFailed && index >= 0 && interstitialAdsIDList.size() > index) {
                        loadInterstitialAds(context, interstitialAdsIDList.get(index), index + 1, adsListener);
                    } else {
                        if (appAdsListener != null) {
                            appAdsListener.onAdsLoadFailed();
                            appAdsListener = null;
                        }
                    }
                }
            });
        } else {
            Log("loadInterstitialAds: loading ads");
        }
    }

    public boolean isInterstitialAdsAvailableToShow(@NonNull final Activity activity) {
        return !isShowingAd && mInterstitialAd != null && !isPurchased(activity);
    }

    public void showInterstitialAds(@NonNull final Activity activity, @Nullable final AdsListener adsListener) {
        showInterstitialAds(activity, false, adsListener, false);
    }

    public void showInterstitialAds(@NonNull final Activity activity, final boolean showProgressBar, @Nullable final AdsListener adsListener) {
        showInterstitialAds(activity, showProgressBar, adsListener, false);
    }

    public void showInterstitialAds(@NonNull final Activity activity, final boolean showProgressBar, @Nullable final AdsListener adsListener, final boolean isNeedNewRequest) {
        if (isInterstitialAdsAvailableToShow(activity)) {
            if (showProgressBar) {
                showProgress(activity);
                new Handler(Looper.myLooper()).postDelayed(() -> {
                    hideProgress(activity);
                    showInterstitialAds(activity, 1, adsListener, isNeedNewRequest);
                }, 1000);
            } else {
                showInterstitialAds(activity, 1, adsListener, isNeedNewRequest);
            }
        } else {
            if (adsListener != null) {
                adsListener.onAdsLoadFailed();
            }
        }
    }

    public void showInterstitialAds(@NonNull final Activity activity, int _id, @Nullable final AdsListener adsListener, final boolean isNeedNewRequest) {
        Log("showInterstitialAds: _id :- " + _id + " isShowingAd :- " + isShowingAd);
        if (isAppOpen && !isShowingAd) {

            if (isPurchased(activity)) {
                Log("showInterstitialAds isPurchased");
                if (adsListener != null) {
                    adsListener.onAdsClose();
                }
                isShowingAd = false;
                return;
            }

            if (mInterstitialAd != null) {

                if (_id != 1) {
                    final int min = 1;
                    final int max = _id;
                    _id = randomGenerator.nextInt((max - min) + 1) + min;
                    Log("showInterstitialAds: _id :- " + _id);
                    if (_id != 1) {
                        if (adsListener != null) {
                            adsListener.onAdsClose();
                        }
                        return;
                    }
                }

                isAppStartUpAdsEnabled = false;
                isShowingAd = true;
                Log("showInterstitialAds: isShowingAd");

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log("onAdDismissedFullScreenContent: ");
                        if (adsListener != null) {
                            adsListener.onAdsClose();
                        }

                        mInterstitialAd = null;
                        isShowingAd = false;

                        if (isNeedNewRequest) {
                            loadInterstitialAds(activity, null);
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        // Called when fullscreen content failed to show.
                        Log("onAdFailedToShowFullScreenContent: ");
                        if (adsListener != null) {
                            adsListener.onAdsLoadFailed();
                        }

                        isShowingAd = false;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {

                        mInterstitialAd = null;
                        isShowingAd = true;

                        if (adsListener != null) {
                            adsListener.onAdsOpened();
                        }
                    }
                });

                mInterstitialAd.show(activity);

            } else {

                isAppStartUpAdsEnabled = false;
                if (isNeedNewRequest) {
                    loadInterstitialAds(activity, null);
                }

                if (adsListener != null) {
                    adsListener.onAdsLoadFailed();
                }
            }
        } else {
            Log("showInterstitialAds: app in background");

            if (adsListener != null) {
                adsListener.onAdsLoadFailed();
            }
        }
    }

    *//* TODO Native ads *//*
    private void loadNativeAds(@NonNull Context context, @Nullable final OnCompleteListener listener) {
        loadNativeAds(context, 1, context.getString(R.string.admob_native_ads_id), 0, listener);
    }

    private void loadNativeAds(@NonNull Context context, final int size, @NonNull final String nativeAdId, final int index, @Nullable final OnCompleteListener listener) {
        final int[] i = {0};

        if (!AppUtils.isContextActive(context)) {
            Log("loadNativeAds: context :- " + context);
            if (listener != null) {
                listener.OnCompleted(nativeAdList);
            }
            isNativeAdsLoading = false;
            return;
        }

        if (isPurchased(context)) {
            Log("loadNativeAds: isPurchased");
            if (listener != null) {
                listener.OnCompleted(nativeAdList);
            }
            isNativeAdsLoading = false;
            return;
        }

        if (isNetworkNotAvailable(context)) {
            Log("loadNativeAds: no network");
            if (listener != null) {
                listener.OnCompleted(nativeAdList);
            }
            isNativeAdsLoading = false;
            return;
        }

        if (nativeAdList.size() > MAX_NATIVE_ADS_STORE) {
            Log("loadNativeAds: loading nativeAdList :- " + nativeAdList.size());
            if (listener != null) {
                listener.OnCompleted(nativeAdList);
            }
            isNativeAdsLoading = false;
            return;
        }

        if (listener != null && !nativeAdLoadCompleteList.contains(listener)) {
            nativeAdLoadCompleteList.add(listener);
        }

        if (!isNativeAdsLoading) {

            isNativeAdsLoading = true;
            Log("loadNativeAds: isNativeAdsLoading ");

            AdLoader adLoader =
                    new AdLoader.Builder(context, nativeAdId)
                            .forNativeAd(nativeAdList::add)
                            .withAdListener(
                                    new AdListener() {
                                        @Override
                                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                            Log("onAdFailedToLoad: loadNativeAds loadAdError :- " + loadAdError.getMessage());

                                            isNativeAdsLoading = false;

                                            if (loadOnFailed && index >= 0 && nativeAdsIDList.size() > index) {
                                                loadNativeAds(context, size, nativeAdsIDList.get(index), index + 1, null);
                                            } else {
                                                for (OnCompleteListener onCompleteListener : nativeAdLoadCompleteList) {
                                                    if (onCompleteListener != null) {
                                                        onCompleteListener.OnCompleted(nativeAdList);
                                                    }
                                                }
                                                nativeAdLoadCompleteList.clear();
                                            }
                                        }

                                        @Override
                                        public void onAdLoaded() {
                                            super.onAdLoaded();
                                            Log("onAdLoaded: fetchNativeAdsBeforeLoads ");
                                            i[0]++;

                                            if (i[0] == size) {
                                                for (OnCompleteListener onCompleteListener : nativeAdLoadCompleteList) {
                                                    if (onCompleteListener != null) {
                                                        onCompleteListener.OnCompleted(nativeAdList);
                                                    }
                                                }
                                                nativeAdLoadCompleteList.clear();
                                            }

                                            isNativeAdsLoading = false;
                                        }
                                    })
                            .withNativeAdOptions(
                                    new NativeAdOptions.Builder()
                                            .setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build())
                                            .build())
                            .build();

            adLoader.loadAds(getAdRequest(), size);
        }
    }

    public void showNativeAds(
            @NonNull final Context context,
            @Nullable final ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout,
            @Nullable final AdsListener listener) {

        if (AppUtils.isContextActive(context) && rootView != null) {

            if (isPurchased(context)) {
                Log("showNativeAds: isPurchased");
                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.GONE);
                    }
                } else {
                    listener.onAdsLoadFailed();
                }
                return;
            }

            if (listener != null) {
                listener.onAdsLoadStart();
            }

            Log("showNativeAds: size :- " + nativeAdList.size());

            if (nativeAdList.size() > 0) {
                finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener, false);

                if (nativeAdList.size() < MAX_NATIVE_ADS_STORE) {

                    int _id = MAX_NATIVE_ADS_STORE;
                    final int min = 1;
                    final int max = _id;
                    _id = randomGenerator.nextInt((max - min) + 1) + min;
                    Log("showNativeAds: _id :- " + _id);
                    if (_id != 1) {
                        return;
                    }

                    loadNativeAds(context, nativeAdList -> finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener, false));
                }
            } else {
                loadNativeAds(context, nativeAdList -> finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener, false));
            }

        } else {
            if (listener == null) {
                if (rootView != null) {
                    rootView.setVisibility(View.GONE);
                }
            } else {
                listener.onAdsLoadFailed();
            }
        }
    }

    public void finalLoadNativeAds(
            @NonNull Context context,
            @Nullable ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout,
            @Nullable final AdsListener listener,
            final boolean isNeedNewRequest) {

        Log("finalLoadNativeAds: size :- " + nativeAdList.size());
        if (AppUtils.isContextActive(context) && nativeAdList.size() > 0) {

            if (isPurchased(context)) {
                Log("finalLoadNativeAds: isPurchased");
                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.GONE);
                    }
                } else {
                    listener.onAdsLoadFailed();
                }
                return;
            }

            NativeAd nativeAd;
            if (nativeAdList.size() > 1) {
                List<NativeAd> tempNativeAdList = new ArrayList<>(nativeAdList);
                Collections.shuffle(tempNativeAdList);
                nativeAd = tempNativeAdList.get(0);
            } else {
                nativeAd = nativeAdList.get(0);
            }

            if (nativeAd == null) {
                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.GONE);
                    }
                } else {
                    listener.onAdsLoadFailed();
                }
                return;
            }

            final View view = LayoutInflater.from(context).inflate(resourceLayout, null);
            if (view instanceof NativeAdView) {

                NativeAdView adView = (NativeAdView) view;
                adView.setTag(NativeAdView.class.getName());

                final MediaView mediaView = adView.findViewById(R.id.ad_media);
                if (mediaView != null) {
                    if (!isShowMedia || nativeAd.getMediaContent() == null) {
                        mediaView.setVisibility(View.GONE);
                    } else {
                        mediaView.setMediaContent(nativeAd.getMediaContent());
                        mediaView.setVisibility(View.VISIBLE);
                        adView.setMediaView(mediaView);
                    }
                }

                final TextView adTitle = adView.findViewById(R.id.ad_headline);
                if (adTitle != null) {
                    adTitle.setText(nativeAd.getHeadline());
                    adView.setHeadlineView(adTitle);
                }

                final TextView adDescription = adView.findViewById(R.id.ad_body);
                if (adDescription != null) {
                    if (nativeAd.getBody() == null || nativeAd.getBody().trim().isEmpty()) {
                        adDescription.setVisibility(View.GONE);

                        if (adTitle != null) {
                            adTitle.setMaxLines(3);
                            adTitle.setSingleLine(false);
                        }

                    } else {
                        adDescription.setText(nativeAd.getBody());
                        adView.setBodyView(adDescription);
                        adDescription.setVisibility(View.VISIBLE);
                    }
                }

//                final TextView ad_price = adView.findViewById(R.id.ad_price);
//                if (ad_price != null) {
//                    String price = nativeAd.getPrice();
//                    if (price != null && !price.trim().isEmpty()) {
//                        ad_price.setText(price);
//                        adView.setPriceView(adTitle);
//                        ad_price.setVisibility(View.VISIBLE);
//                    } else {
//                        ad_price.setVisibility(View.INVISIBLE);
//                    }
//                }
//
//                final TextView ad_store = adView.findViewById(R.id.ad_store);
//                if (ad_store != null) {
//                    String store = nativeAd.getStore();
//                    if (store != null && !store.trim().isEmpty()) {
//                        ad_store.setText(store);
//                        adView.setStoreView(ad_store);
//                        ad_store.setVisibility(View.VISIBLE);
//                    } else {
//                        ad_store.setVisibility(View.INVISIBLE);
//                    }
//                }

//                final TextView ad_advertiser = adView.findViewById(R.id.ad_advertiser);
//                if (ad_advertiser != null) {
//                    String advertiser = nativeAd.getAdvertiser();
//                    if (advertiser != null && !advertiser.trim().isEmpty()) {
//                        ad_advertiser.setText(advertiser);
//                        adView.setAdvertiserView(ad_advertiser);
//                        ad_advertiser.setVisibility(View.VISIBLE);
//                    } else {
//                        ad_advertiser.setVisibility(View.INVISIBLE);
//                    }
//                }

                final AppCompatButton callToAction = adView.findViewById(R.id.ad_call_to_action);
                if (callToAction != null) {
                    callToAction.setText(nativeAd.getCallToAction());
                    adView.setCallToActionView(callToAction);
                }

//                final RatingBar ad_stars = adView.findViewById(R.id.ad_stars);
//                if (ad_stars != null) {
//                    if (nativeAd.getStarRating() == null || nativeAd.getStarRating() == null) {
//                        ad_stars.setVisibility(View.GONE);
//                    } else {
//                        ad_stars.setRating(nativeAd.getStarRating().floatValue());
//                        ad_stars.setVisibility(View.VISIBLE);
//                        adView.setStarRatingView(ad_stars);
//                    }
//                }

                ImageView imageView = adView.findViewById(R.id.ad_app_icon);
                if (imageView != null) {
                    if (nativeAd.getIcon() == null || nativeAd.getIcon().getDrawable() == null) {
                        if (nativeAd.getMediaContent() != null && nativeAd.getMediaContent().getMainImage() != null) {
                            imageView.setImageDrawable(nativeAd.getMediaContent().getMainImage());
                            imageView.setVisibility(View.VISIBLE);
                            adView.setIconView(imageView);
                            adView.findViewById(R.id.ad_app_icon_Lay).setVisibility(View.VISIBLE);
                        } else {
                            imageView.setVisibility(View.GONE);
                            adView.findViewById(R.id.ad_app_icon_Lay).setVisibility(View.GONE);
                        }
                    } else {
                        imageView.setImageDrawable(nativeAd.getIcon().getDrawable());
                        imageView.setVisibility(View.VISIBLE);
                        adView.setIconView(imageView);
                        adView.findViewById(R.id.ad_app_icon_Lay).setVisibility(View.VISIBLE);
                    }
                }
                adView.setNativeAd(nativeAd);

                if (rootView != null) {
                    rootView.removeAllViews();
                    rootView.addView(adView);
                }

                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.VISIBLE);
                    }
                } else {
                    listener.onAdsOpened();
                }

            } else {
                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.GONE);
                    }
                } else {
                    listener.onAdsLoadFailed();
                }
            }

        } else {

            if (AppUtils.isContextActive(context) && nativeAdList.size() <= 0 && isNeedNewRequest) {
                loadNativeAds(context, nativeAdList -> {
                    if (nativeAdList != null && nativeAdList.size() > 0) {
                        finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener, false);
                    }
                });
            } else {
                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.GONE);
                    }
                } else {
                    listener.onAdsLoadFailed();
                }
            }
        }
    }

    public boolean isNativeAdsAvailable(Context context) {
        return nativeAdList.size() > 0 && !isPurchased(context);
    }

    private void loadAdOpenAds(@NonNull final Activity activity, @NonNull final String appOpenId, final int index) {
        Log("loadAdOpenAds: isAppOpenAdLoading :- " + isAppOpenAdLoading);
        if (!isAppOpenAdLoading) {

            if (!AppUtils.isContextActive(activity) || isShowingAd) {
                Log("loadAdOpenAds: isShowingAd :- " + isShowingAd);
                isAppOpenAdLoading = false;
                return;
            }

            if (isPurchased(activity)) {
                Log("loadAdOpenAds purchase");
                isAppOpenAdLoading = false;
                return;
            }

            if (isNetworkNotAvailable(activity)) {
                Log("loadAdOpenAds: no network");
                isAppOpenAdLoading = false;
                return;
            }

            if (appOpenAd != null) {
                Log("loadAdOpenAds: onAdLoaded open ad already");
                isAppOpenAdLoading = false;
                showAdIfAvailable(activity);
                return;
            }

            isAppOpenAdLoading = true;
            Log("loadAdOpenAds: isAppOpenAdLoading ");
            if (index == 0) {
                appOpenLoadTime = System.currentTimeMillis();
            }

            AppOpenAd.AppOpenAdLoadCallback loadCallback =
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd ad) {
                            super.onAdLoaded(ad);
                            Log("loadAdOpenAds: onAdLoaded open ad loaded");

                            appOpenAd = ad;
                            isAppOpenAdLoading = false;

                            long current = System.currentTimeMillis();
                            if (current - appOpenLoadTime <= ADS_MAX_LOAD_TIME) {
                                showAdIfAvailable(activity);
                            }
                            appOpenLoadTime = 0L;
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            Log("loadAdOpenAds onAdFailedToLoad: loadAdError :- " + loadAdError.getMessage());
                            isAppOpenAdLoading = false;

                            if (loadOnFailed && index >= 0 && appOpenAdsIDList.size() > index) {
                                loadAdOpenAds(activity, appOpenAdsIDList.get(index), index + 1);
                            } else {
                                appOpenLoadTime = 0L;
                            }
                        }
                    };

            AppOpenAd.load(
                    activity,
                    appOpenId,
                    getAdRequest(),
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    loadCallback);
        } else {
            appOpenLoadTime = System.currentTimeMillis();
        }
    }

    private void showAdIfAvailable(@NonNull final Activity activity) {

        if (appOpenAd != null && !isShowingAd && isAppOpen) {

            if (isPurchased(activity)) {
                Log("onAdFailedToLoad: showAdIfAvailable purchased");
                isShowingAd = false;
                return;
            }

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            Log("loadAdOpenAds: onAdDismissedFullScreenContent: ");
                            appOpenAd = null;
                            isShowingAd = false;
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log("loadAdOpenAds: Error display show ad." + adError.getMessage());
                            isShowingAd = false;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log("loadAdOpenAds: onAdShowedFullScreenContent: ");
                            isShowingAd = true;
                        }
                    };

            isShowingAd = true;
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(activity);

        } else {
            Log("Can not show ad. appOpenAd :- " + appOpenAd + " isShowingAd :- " + isShowingAd);
//            loadAdOpenAds(appOpenId);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log("onActivityCreated: ");
        this.activity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log("onActivityStarted: ");
        this.activity = activity;
        isAppOpen = true;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        this.activity = activity;

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            isAppOpen = true;

            Log("onActivityResumed: isAppOpenAdEnable :- " + isAppOpenAdEnable + " isNeedOpenAdRequest :- " + isNeedOpenAdRequest + " isShowingAd :- " + isShowingAd);
            if (isAppOpenAdEnable && isNeedOpenAdRequest && !isShowingAd) {
                loadAdOpenAds(activity, activity.getString(R.string.admob_app_start_ads_id), 0);
            }

            isAppOpenAdEnable = false;
            isNeedOpenAdRequest = true;
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log("onActivityPaused: ");
        this.activity = activity;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            isAppOpenAdEnable = isApplicationBroughtToBackground();
            isAppOpen = false;
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log("onActivityStopped: ");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        Log("onActivitySaveInstanceState: ");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log("onActivityDestroyed: ");
        isAppOpenAdEnable = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            isAppOpen = true;

            Log("onEnterForeground: isAppOpenAdEnable :- " + isAppOpenAdEnable + " isNeedOpenAdRequest :- " + isNeedOpenAdRequest + " isShowingAd :- " + isShowingAd);
            if (isAppOpenAdEnable && isNeedOpenAdRequest && !isShowingAd) {
                loadAdOpenAds(activity, activity.getString(R.string.admob_app_start_ads_id), 0);
            }

            isAppOpenAdEnable = false;
            isNeedOpenAdRequest = true;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log("onEnterBackground: ");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            isAppOpenAdEnable = true;
            isAppOpen = false;
        }
    }

    private boolean isApplicationBroughtToBackground() {
        if (myApplication != null) {
            ActivityManager am = (ActivityManager) myApplication.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks != null && !tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    return topActivity != null && !topActivity.getPackageName().equals(myApplication.getPackageName());
                }
            }
        }
        return false;
    }

    public interface OnCompleteListener {
        void OnCompleted(List<NativeAd> nativeAdList);
    }

    public interface AdsLoadsListener {
        void onAdLoaded();

        void onAdsLoadFailed();
    }

    public interface AdsListener {
        void onAdsLoadStart();

        void onAdsOpened();

        void onAdsClose();

        void onAdsLoadFailed();
    }

}*/

@Singleton
public class AdvertiseHandler implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    public static final int CURRENT_NATIVE_AD_TYPE = -20;
    @SuppressLint("StaticFieldLeak")
    private static volatile AdvertiseHandler sInstance;
    private final long ADS_MAX_LOAD_TIME = 3000L;
    private final List<NativeAd> nativeAdList = new ArrayList<>();
    private final ArrayList<Listener> nativeAdLoadCompleteList = new ArrayList<>();
    private final int MAX_NATIVE_ADS_STORE = 10;
    public boolean isAppStartUpAdsEnabled = true, isDisableAds = false, isSplash = false;
    private Activity activity;
    private Application myApplication = null;
    private boolean isSDKInitialized = false, isAppOpen = false, isShowingAd = false;
    private InterstitialAd mInterstitialAd;
    public boolean isSplashAdShown = false;
    private boolean interstitialAdsLoading = false;

    private AppOpenAd appOpenAd = null;
    private Listener appAdsListener;
    private long appOpenLoadTime = 0L;
    private FullScreenContentCallback fullScreenContentCallback;
    private NativeAd exitNativeAds;
    private long interstitialAdsLoadTime = 0L;
    //    private boolean isAppOpenAdLoading = false, isNeedOpenAdRequest = false, isAppOpenAdEnable = false;
    private boolean isNativeAdsLoading = false, isAfterCallScreenNativeAdsLoading = false, isExitNativeAdsLoading = false;
    private Listener tempListener;

    private final boolean loadOnFailed = true;
    private final List<String> bannerAdsIDList = new ArrayList<>();
    private final List<String> interstitialAdsIDList = new ArrayList<>();
    private final List<String> nativeAdsIDList = new ArrayList<>();
//    private final List<String> appOpenAdsIDList = new ArrayList<>();

    private Random randomGenerator;
    private WebView webView = null;
    public static RewardedAd mRewardedAd;
    public AdView Adview;

    @Singleton
    private AdvertiseHandler() {
        init();

    }

    @NonNull
    public static AdvertiseHandler getInstance(@NonNull Application application) {

        if (sInstance == null) {
            synchronized (AdvertiseHandler.class) {
                if (sInstance == null) {
                    sInstance = new AdvertiseHandler();
                }
            }
        }

        sInstance.setApplication(application);
        return sInstance;
    }

    @NonNull
    public static AdvertiseHandler getInstance() {
        if (sInstance == null) {
            synchronized (AdvertiseHandler.class) {
                if (sInstance == null) {
                    sInstance = new AdvertiseHandler();
                }
            }
        }

        return sInstance;
    }

    private void init() {

        isSDKInitialized = false;

        randomGenerator = new Random();
        nativeAdList.clear();
        appAdsListener = null;

        bannerAdsIDList.clear();
        interstitialAdsIDList.clear();
        nativeAdsIDList.clear();
//        appOpenAdsIDList.clear();

//        bannerAdsIDList.add("/22387492205,22667063805/com.chating.messages.chat.fun.Banner0.1650441468");
        interstitialAdsIDList.add("/22387492205,22844267357/com.chating.messages.chat.fun.Interstitial1.1668416397");
        nativeAdsIDList.add("/22387492205,22844267357/com.chating.messages.chat.fun.Native1.1668416338");
//        appOpenAdsIDList.add("ca-app-pub-2710573691140013/6165401211");
        isAfterCallScreenNativeAdsLoading = false;
        isExitNativeAdsLoading = false;
        exitNativeAds = null;

        webView = null;
    }

    private void setApplication(Application application) {
        if (application != null) {
            myApplication = application;
        }

        if (myApplication != null) {
            myApplication.unregisterActivityLifecycleCallbacks(this);
            ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);

            myApplication.registerActivityLifecycleCallbacks(this);
            ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        }
    }

    private void Log(String logMessage) {
        if (BuildConfig.DEBUG) {
            Log.i("AdvertiseHandler", logMessage);
        }
    }

    @NonNull
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isShowingAd() {
        return isShowingAd;
    }

    public void showProgress(@NonNull final Activity activity1) {
        if (AppUtils.isContextActive(activity)) {
            activity.runOnUiThread(() -> AppUtils.showProgressDialog(activity, R.string.ads_loading_msg));
        } else if (AppUtils.isContextActive(activity1)) {
            activity1.runOnUiThread(() -> AppUtils.showProgressDialog(activity1, R.string.ads_loading_msg));
        }
    }

    public void hideProgress(@NonNull final Activity activity1) {
        if (AppUtils.isContextActive(activity)) {
            activity.runOnUiThread(AppUtils::hideProgressDialog);
        } else if (AppUtils.isContextActive(activity1)) {
            activity1.runOnUiThread(AppUtils::hideProgressDialog);
        }
    }

    private boolean isNetworkNotAvailable(@NonNull final Context context) {
        return AppUtils.isNetworkNotAvailable(context);
    }

    public boolean isPurchased(@NonNull final Context context) {
        boolean purchase = AppUtils.getBooleanValue(context, AppUtils.IS_APP_PURCHASED, false);
        Log("isPurchased: purchase :- " + purchase);
//        return purchase;
        return purchase;
    }

    /* TODO Banner ads */

    public void loadBannerAds(@NonNull final Context context, @Nullable final ViewGroup bannerAdLayout, @Nullable Listener adListener) {
        loadBannerAds(context, context.getString(R.string.admob_banner_ads_id), 0, bannerAdLayout, adListener);
    }


   public static boolean IsBannerLoded = false;
    public void loadBannerAds(@NonNull final Context context, @NonNull final String bannerID, final int index, @Nullable Listener adListener) {

        if (!AppUtils.isContextActive(context)) {
            Log("loadBannerAds: context null");
            return;
        }

        if (isShowingAd) {
            Log("loadBannerAds: isShowingAd :- " + true);
            if (adListener != null) {
                adListener.onAdsLoadFailed();
            }
            return;
        }

        if (isPurchased(context)) {
            Log("loadBannerAds: isPurchased ");
            if (adListener != null) {
                adListener.onAdsLoadFailed();
            }
            return;
        }

        if (isNetworkNotAvailable(context)) {
            Log("loadBannerAds: no network");


            if (adListener != null) {
                adListener.onAdsLoadFailed();
            }
            return;
        }

//        if (bannerAdLayout == null) {
//            Log("loadBannerAds: bannerAdLayout NULL");
//            if (adListener != null) {
//                adListener.onAdsLoadFailed();
//            }
//            return;
//        }

        Adview = new AdView(context.getApplicationContext());
        Adview.setAdUnitId(bannerID);
//        bannerAdLayout.addView(adView);

        AdSize adSize = getAdSize(context);
        Adview.setAdSize(adSize);
        Log("loadBannerAds: loading");
     //   this.adview = adView;
        Adview.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();

                if (AppUtils.isContextActive(context)) {
//                    bannerAdLayout.setVisibility(View.GONE);

                    if (adListener != null) {
                        adListener.onAdsClosed();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                IsBannerLoded = false;
                Log("onAdFailedToLoad: loadBannerAds loadAdError :- " + loadAdError.getMessage() + " bannerID :- " + bannerID);

                if (AppUtils.isContextActive(context)) {
                    if (loadOnFailed && index >= 0 && bannerAdsIDList.size() > index) {
                        loadBannerAds(context, bannerAdsIDList.get(index), index + 1, adListener);
                    } else {
                        if (adListener != null) {
                            adListener.onAdsLoadFailed();
                        }
//                        else {
//                            bannerAdLayout.setVisibility(View.GONE);
//                        }
                    }
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log("onAdLoaded: loadBannerAds");

                IsBannerLoded = true;

                if (AppUtils.isContextActive(context)) {
//                    bannerAdLayout.setVisibility(View.VISIBLE);

                    if (adListener != null) {
                        adListener.onAdsShowing();
                    }
                }
            }
        });

        Adview.loadAd(getAdRequest());
    }


    private void loadBannerAds(@NonNull final Context context, @NonNull final String bannerID, final int index, @Nullable final ViewGroup bannerAdLayout, @Nullable Listener adListener) {

        if (!AppUtils.isContextActive(context)) {
            Log("loadBannerAds: context null");
            return;
        }

        if (isShowingAd) {
            Log("loadBannerAds: isShowingAd :- " + true);
            if (adListener != null) {
                adListener.onAdsLoadFailed();
            }
            return;
        }

        if (isPurchased(context)) {
            Log("loadBannerAds: isPurchased ");
            if (adListener != null) {
                adListener.onAdsLoadFailed();
            }
            return;
        }

        if (isNetworkNotAvailable(context)) {
            Log("loadBannerAds: no network");
            if (adListener != null) {
                adListener.onAdsLoadFailed();
            }
            return;
        }

        if (bannerAdLayout == null) {
            Log("loadBannerAds: bannerAdLayout NULL");
            if (adListener != null) {
                adListener.onAdsLoadFailed();
            }
            return;
        }

        AdView adView = new AdView(context.getApplicationContext());
        adView.setAdUnitId(bannerID);
        bannerAdLayout.addView(adView);

        AdSize adSize = getAdSize(context);
        adView.setAdSize(adSize);
        Log("loadBannerAds: loading");

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();

                if (AppUtils.isContextActive(context)) {
                    bannerAdLayout.setVisibility(View.GONE);

                    if (adListener != null) {
                        adListener.onAdsClosed();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log("onAdFailedToLoad: loadBannerAds loadAdError :- " + loadAdError.getMessage() + " bannerID :- " + bannerID);

                if (AppUtils.isContextActive(context)) {
                    if (loadOnFailed && index >= 0 && bannerAdsIDList.size() > index) {
                        loadBannerAds(context, bannerAdsIDList.get(index), index + 1, bannerAdLayout, adListener);
                    } else {
                        if (adListener != null) {
                            adListener.onAdsLoadFailed();
                        } else {
                            bannerAdLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log("onAdLoaded: loadBannerAds");

                if (AppUtils.isContextActive(context)) {
                    bannerAdLayout.setVisibility(View.VISIBLE);

                    if (adListener != null) {
                        adListener.onAdsShowing();
                    }
                }
            }
        });

        adView.loadAd(getAdRequest());
    }

    @NonNull
    private AdSize getAdSize(@NonNull Context context) {

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    /* TODO Interstitial ads */

    public boolean isInterstitialAdsAvailableToShow(@NonNull final Activity activity) {
        Log.i("ADS 1459", String.valueOf(isShowingAd) + "" + String.valueOf(isDisableAds) + "" + String.valueOf(isAppOpen) + "" + String.valueOf(mInterstitialAd) + "" + AppUtils.isContextActive(activity) + "" + isPurchased(activity));
        return !isShowingAd && !isDisableAds && isAppOpen && mInterstitialAd != null && AppUtils.isContextActive(activity) && !isPurchased(activity);
    }

//    public void loadRewardedAds() {
//        loadRewardedAds(null);
//    }

    public void loadInterstitialAds() {
        loadInterstitialAds(null);
    }

//    public void loadRewardedAds(@Nullable Listener adsListener) {
//
//        if (!AppUtils.isContextActive(activity)) {
//            Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + isShowingAd);
//            return;
//        }
//
//        loadRewardedAds(activity, activity.getString(R.string.admob_rewarded_id), adsListener);
//    }

    private void loadRewardedAds(@NonNull final Activity activity, @NonNull final String RewardedID, @Nullable Listener adsListener) {
        this.appAdsListener = null;
        this.appAdsListener = adsListener;
        Log("onAdLoaded: loadInterstitialAds appAdsListener :- " + appAdsListener);


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (AppUtils.isContextActive(activity)) {


                    RewardedAd.load(activity, RewardedID, getAdRequest(), new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (appAdsListener != null) {
                                appAdsListener.onAdsLoadFailed();
                                appAdsListener = null;
                            }
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            super.onAdLoaded(rewardedAd);


                            Log.e("TAG397", "onAdLoaded: " + rewardedAd);
                            mRewardedAd = rewardedAd;


                            if (appAdsListener != null) {
                                appAdsListener.onAdsLoadCompleted();
                            }
                        }
                    });


//                    InterstitialAd.load(activity.getApplicationContext(), interstitialID, getAdRequest(), new InterstitialAdLoadCallback() {
//                        @Override
//                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                            super.onAdLoaded(interstitialAd);
//                            Log("onAdLoaded: loadInterstitialAds appAdsListener onAdLoaded :- " + appAdsListener);
//                            mInterstitialAd = interstitialAd;
//                            interstitialAdsLoading = false;
//
//                            if (appAdsListener != null) {
////                                    long current = System.currentTimeMillis();
////                                    if ((current - interstitialAdsLoadTime) <= ADS_MAX_LOAD_TIME) {
//                                appAdsListener.onAdsLoadCompleted();
////                                    } else {
////                                        Log("onAdLoaded: loadInterstitialAds time out ");
////                                        appAdsListener.onAdsLoadFailed();
////                                    }
//                            }
//
//                            interstitialAdsLoadTime = 0L;
//                            appAdsListener = null;
//                        }
//
//                        @Override
//                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                            super.onAdFailedToLoad(loadAdError);
//
//                            if (appAdsListener != null) {
//                                appAdsListener.onAdsLoadFailed();
//                                appAdsListener = null;
//                            }
//                        }
//                    });
                }
            }
        });

//        if (!interstitialAdsLoading) {
//
//            if (!AppUtils.isContextActive(activity)) {
//                Log("onAdFailedToLoad: loadInterstitialAds activity null");
//                return;
//            }
//
//            if (isShowingAd) {
//                Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + true);
//                interstitialAdsLoading = false;
//                if (appAdsListener != null) {
//                    appAdsListener.onAdsLoadFailed();
//                    appAdsListener = null;
//                }
//                return;
//            }
//
//            if (isPurchased(activity)) {
//                Log("onAdFailedToLoad: loadInterstitialAds isPurchased");
//                interstitialAdsLoading = false;
//                if (appAdsListener != null) {
//                    appAdsListener.onAdsLoadFailed();
//                    appAdsListener = null;
//                }
//                return;
//            }
//
//            if (isNetworkNotAvailable(activity)) {
//                Log("onAdFailedToLoad: loadInterstitialAds no network");
//                interstitialAdsLoading = false;
//                if (appAdsListener != null) {
//                    appAdsListener.onAdsLoadFailed();
//                    appAdsListener = null;
//                }
//                return;
//            }
//
//            if (mInterstitialAd != null) {
//                Log("onAdLoaded: loadInterstitialAds already ");
//                interstitialAdsLoading = false;
//                if (appAdsListener != null) {
//                    appAdsListener.onAdsLoadCompleted();
//                    appAdsListener = null;
//                }
//                return;
//            }
//
//            Log("loadInterstitialAds: interstitialAdsLoading ");
//            interstitialAdsLoading = true;
//            if (index == 0) {
//                interstitialAdsLoadTime = System.currentTimeMillis();
//            }
//
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (AppUtils.isContextActive(activity)) {
//                        InterstitialAd.load(activity.getApplicationContext(), interstitialID, getAdRequest(), new InterstitialAdLoadCallback() {
//                            @Override
//                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                                super.onAdLoaded(interstitialAd);
//                                Log("onAdLoaded: loadInterstitialAds appAdsListener onAdLoaded :- " + appAdsListener);
//                                mInterstitialAd = interstitialAd;
//                                interstitialAdsLoading = false;
//
//                                if (appAdsListener != null) {
////                                    long current = System.currentTimeMillis();
////                                    if ((current - interstitialAdsLoadTime) <= ADS_MAX_LOAD_TIME) {
//                                    appAdsListener.onAdsLoadCompleted();
////                                    } else {
////                                        Log("onAdLoaded: loadInterstitialAds time out ");
////                                        appAdsListener.onAdsLoadFailed();
////                                    }
//                                }
//
//                                interstitialAdsLoadTime = 0L;
//                                appAdsListener = null;
//                            }
//
//                            @Override
//                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                                super.onAdFailedToLoad(loadAdError);
//                                Log("onAdFailedToLoad: loadInterstitialAds loadAdError :- " + loadAdError.getMessage() + " interstitialID :- " + interstitialID);
//                                interstitialAdsLoading = false;
//                                mInterstitialAd = null;
//
//                                if (loadOnFailed && index >= 0 && interstitialAdsIDList.size() > index) {
////                                    if (!AppUtils.isContextActive(activity)) {
////                                        appAdsListener = null;
////                                    }
//
//                                    loadInterstitialAdsonAdFailed(AdvertiseHandler.this.activity,activity.getString(R.string.admob_interstitial_ads_id), appAdsListener);
////                                    loadInterstitialAds(AdvertiseHandler.this.activity, interstitialAdsIDList.get(index), index + 1, appAdsListener);
//                                } else {
//                                    if (appAdsListener != null) {
//                                        appAdsListener.onAdsLoadFailed();
//                                        appAdsListener = null;
//                                    }
//                                    interstitialAdsLoadTime = 0L;
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//        } else {
//            Log("loadInterstitialAds: loading ads");
//            interstitialAdsLoadTime = System.currentTimeMillis();
//        }
    }

    public void loadInterstitialAds(Activity activity, String interId,@Nullable Listener adsListener) {

        if (!AppUtils.isContextActive(activity)) {
            Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + isShowingAd);
            return;
        }

        loadInterstitialAds(activity, interId, 0, adsListener);
    }

    public void loadInterstitialAds(Activity activity, @Nullable Listener adsListener) {

        if (!AppUtils.isContextActive(activity)) {
            Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + isShowingAd);
            return;
        }

        loadInterstitialAds(activity, activity.getString(R.string.admob_splash_interstitial_ads_id), 0, adsListener);
    }

    public void loadInterstitialAds(@Nullable Listener adsListener) {

        if (!AppUtils.isContextActive(activity)) {
            Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + isShowingAd);
            return;
        }

        loadInterstitialAds(activity, activity.getString(R.string.admob_splash_interstitial_ads_id), 0, adsListener);
    }

    public void loadInterstitialAds(int _id, Listener listener) {

        if (_id != 1) {
            final int min = 1;
            final int max = _id;
            _id = randomGenerator.nextInt((max - min) + 1) + min;
            Log("showInterstitialAds: _id :- " + _id);

            if (_id == 1) {
                Log("showInterstitialAds: Ads Type :- Admob");
                loadInterstitialAds(activity, activity.getString(R.string.admob_splash_interstitial_ads_id), 0, _id, listener);
            } else if (_id == 2) {
                Log("showInterstitialAds: Ads Type :- Admob Adex");

                loadInterstitialAds(activity, activity.getString(R.string.admob_splash_interstitial_ads_id), 0, _id, listener);
            }

            if (_id != 1) {
                if (listener != null) {
                    listener.onAdsClosed();
                }
                return;
            }
        }

//        loadInterstitialAds(listener);
    }

    private void loadInterstitialAds(@NonNull final Activity activity, @NonNull final String interstitialID, final int index, final int random_int, @Nullable Listener adsListener) {
        this.appAdsListener = null;
        this.appAdsListener = adsListener;
        Log("onAdLoaded: loadInterstitialAds appAdsListener :- " + appAdsListener);

        if (!interstitialAdsLoading) {

            if (!AppUtils.isContextActive(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds activity null");
                return;
            }

            if (isShowingAd) {
                Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + true);
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isPurchased(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds isPurchased");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isNetworkNotAvailable(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds no network");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (mInterstitialAd != null) {
                Log("onAdLoaded: loadInterstitialAds already ");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadCompleted();
                    appAdsListener = null;
                }
                return;
            }

            Log("loadInterstitialAds: interstitialAdsLoading ");
            interstitialAdsLoading = true;
            if (index == 0) {
                interstitialAdsLoadTime = System.currentTimeMillis();
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AppUtils.isContextActive(activity)) {
                        InterstitialAd.load(activity.getApplicationContext(), interstitialID, getAdRequest(), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                super.onAdLoaded(interstitialAd);
                                Log("onAdLoaded: loadInterstitialAds appAdsListener onAdLoaded :- " + appAdsListener);
                                mInterstitialAd = interstitialAd;
                                interstitialAdsLoading = false;

                                if (appAdsListener != null) {
//                                    long current = System.currentTimeMillis();
//                                    if ((current - interstitialAdsLoadTime) <= ADS_MAX_LOAD_TIME) {
                                    appAdsListener.onAdsLoadCompleted();
//                                    } else {
//                                        Log("onAdLoaded: loadInterstitialAds time out ");
//                                        appAdsListener.onAdsLoadFailed();
//                                    }
                                }

                                interstitialAdsLoadTime = 0L;
                                appAdsListener = null;
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                Log("onAdFailedToLoad: loadInterstitialAds loadAdError :- " + loadAdError.getMessage() + " interstitialID :- " + interstitialID);
                                interstitialAdsLoading = false;
                                mInterstitialAd = null;

                                if (random_int == 1) {
//                                    if (!AppUtils.isContextActive(activity)) {
//                                        appAdsListener = null;
//                                    }
                                    Log("showInterstitialAds: Ads Type :- Admob Adex");

                                    loadInterstitialAdsonAdFailed(AdvertiseHandler.this.activity, activity.getString(R.string.admob_splash_interstitial_ads_id), appAdsListener);
                                } else if (random_int == 2) {
                                    Log("showInterstitialAds: Ads Type :- Admob");

                                    loadInterstitialAdsonAdFailed(AdvertiseHandler.this.activity, activity.getString(R.string.admob_splash_interstitial_ads_id), appAdsListener);

                                } else {
                                    if (appAdsListener != null) {
                                        appAdsListener.onAdsLoadFailed();
                                        appAdsListener = null;
                                    }
                                    interstitialAdsLoadTime = 0L;
                                }
                            }
                        });
                    }
                }
            });
        } else {
            Log("loadInterstitialAds: loading ads");
            interstitialAdsLoadTime = System.currentTimeMillis();
        }
    }


    private void loadInterstitialAds(@NonNull final Activity activity, @NonNull final String interstitialID, final int index, @Nullable Listener adsListener) {
        this.appAdsListener = null;
        this.appAdsListener = adsListener;
        Log("onAdLoaded: loadInterstitialAds appAdsListener :- " + appAdsListener);

        if (!interstitialAdsLoading) {

            if (!AppUtils.isContextActive(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds activity null");
                return;
            }

            if (isShowingAd) {
                Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + true);
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isPurchased(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds isPurchased");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isNetworkNotAvailable(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds no network");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (mInterstitialAd != null) {
                Log("onAdLoaded: loadInterstitialAds already ");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadCompleted();
                    appAdsListener = null;
                }
                return;
            }

            Log("loadInterstitialAds: interstitialAdsLoading ");
            interstitialAdsLoading = true;
            if (index == 0) {
                interstitialAdsLoadTime = System.currentTimeMillis();
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AppUtils.isContextActive(activity)) {
                        InterstitialAd.load(activity.getApplicationContext(), interstitialID, getAdRequest(), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                super.onAdLoaded(interstitialAd);
                                Log("onAdLoaded: loadInterstitialAds appAdsListener onAdLoaded :- " + appAdsListener);
                                mInterstitialAd = interstitialAd;
                                interstitialAdsLoading = false;

                                if (appAdsListener != null) {
//                                    long current = System.currentTimeMillis();
//                                    if ((current - interstitialAdsLoadTime) <= ADS_MAX_LOAD_TIME) {
                                    appAdsListener.onAdsLoadCompleted();
//                                    } else {
//                                        Log("onAdLoaded: loadInterstitialAds time out ");
//                                        appAdsListener.onAdsLoadFailed();
//                                    }
                                }

                                interstitialAdsLoadTime = 0L;
                                appAdsListener = null;
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                Log("onAdFailedToLoad: loadInterstitialAds loadAdError :- " + loadAdError.getMessage() + " interstitialID :- " + interstitialID);
                                interstitialAdsLoading = false;
                                mInterstitialAd = null;

                                if (loadOnFailed && index >= 0 && interstitialAdsIDList.size() > index) {
//                                    if (!AppUtils.isContextActive(activity)) {
//                                        appAdsListener = null;
//                                    }

                                    loadInterstitialAdsonAdFailed(AdvertiseHandler.this.activity, activity.getString(R.string.admob_splash_interstitial_ads_id), appAdsListener);
//                                    loadInterstitialAds(AdvertiseHandler.this.activity, interstitialAdsIDList.get(index), index + 1, appAdsListener);
                                } else {
                                    if (appAdsListener != null) {
                                        appAdsListener.onAdsLoadFailed();
                                        appAdsListener = null;
                                    }
                                    interstitialAdsLoadTime = 0L;
                                }
                            }
                        });
                    }
                }
            });
        } else {
            Log("loadInterstitialAds: loading ads");
            interstitialAdsLoadTime = System.currentTimeMillis();
        }
    }


    private void loadInterstitialAdsonAdFailed(@NonNull final Activity activity, @NonNull final String interstitialID, @Nullable Listener adsListener) {
        this.appAdsListener = null;
        this.appAdsListener = adsListener;
        Log("onAdLoaded: loadInterstitialAds appAdsListener :- " + appAdsListener);

        if (!interstitialAdsLoading) {

            if (!AppUtils.isContextActive(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds activity null");
                return;
            }

            if (isShowingAd) {
                Log("onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + true);
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isPurchased(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds isPurchased");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isNetworkNotAvailable(activity)) {
                Log("onAdFailedToLoad: loadInterstitialAds no network");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (mInterstitialAd != null) {
                Log("onAdLoaded: loadInterstitialAds already ");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadCompleted();
                    appAdsListener = null;
                }
                return;
            }


            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AppUtils.isContextActive(activity)) {
                        InterstitialAd.load(activity.getApplicationContext(), interstitialID, getAdRequest(), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                super.onAdLoaded(interstitialAd);
                                Log("onAdLoaded: loadInterstitialAds appAdsListener onAdLoaded :- " + appAdsListener);
                                mInterstitialAd = interstitialAd;
                                interstitialAdsLoading = false;

                                if (appAdsListener != null) {
//                                    long current = System.currentTimeMillis();
//                                    if ((current - interstitialAdsLoadTime) <= ADS_MAX_LOAD_TIME) {
                                    appAdsListener.onAdsLoadCompleted();
//                                    } else {
//                                        Log("onAdLoaded: loadInterstitialAds time out ");
//                                        appAdsListener.onAdsLoadFailed();
//                                    }
                                }


                                appAdsListener = null;
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
//                                Log("onAdFailedToLoad: loadInterstitialAds loadAdError :- " + loadAdError.getMessage() + " interstitialID :- " + interstitialID);
//                                interstitialAdsLoading = false;
//                                mInterstitialAd = null;
//
//                                if (loadOnFailed && index >= 0 && interstitialAdsIDList.size() > index) {
////                                    if (!AppUtils.isContextActive(activity)) {
////                                        appAdsListener = null;
////                                    }
//                                    loadInterstitialAds(AdvertiseHandler.this.activity, interstitialAdsIDList.get(index), index + 1, appAdsListener);
//                                } else {
//                                    if (appAdsListener != null) {
//                                        appAdsListener.onAdsLoadFailed();
//                                        appAdsListener = null;
//                                    }
//                                    interstitialAdsLoadTime = 0L;
//                                }
                            }
                        });
                    }
                }
            });
        } else {
            Log("loadInterstitialAds: loading ads");
            interstitialAdsLoadTime = System.currentTimeMillis();
        }
    }

    public void showInterstitialAds(@NonNull final Activity activity) {
        showInterstitialAds(activity, null);
    }

    public void showInterstitialAds(@NonNull final Activity activity, @Nullable Listener adsListener) {
        showInterstitialAds(activity, false, adsListener, false);
    }

    public void showInterstitialAds(@NonNull final Activity activity, final boolean showProgressBar, @Nullable Listener adsListener) {
        showInterstitialAds(activity, showProgressBar, adsListener, false);
    }

    public void showInterstitialAds(@NonNull final Activity activity, final boolean showProgressBar, @Nullable Listener adsListener, final boolean isNeedNewRequest) {

        if (!AppUtils.isContextActive(activity)) {
            Log("showInterstitialAds activity null");
            return;
        }
        Log.e("TAG2079", "showInterstitialAds: " + isInterstitialAdsAvailableToShow(activity));
        if (isInterstitialAdsAvailableToShow(activity)) {
            if (showProgressBar) {
                showProgress(activity);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showInterstitialAds(activity, 1, adsListener, isNeedNewRequest);
                    hideProgress(activity);
                }, 1500);
            } else {
                showInterstitialAds(activity, 1, adsListener, isNeedNewRequest);
            }
        } else {
            Log("showInterstitialAds: else isShowingAd :- " + isShowingAd + " isDisableAds :- " + isDisableAds + " isAppOpen :- "
                    + isAppOpen + " mInterstitialAd :- " + "may null" + " isPurchased :- " + isPurchased(activity) + " activity :- may null");

            if (adsListener != null) {
                adsListener.onAdsLoadFailed();
            }
        }
    }

    public void showInterstitialAds(@NonNull final Activity activity, int _id, @Nullable Listener adsListener) {
        showInterstitialAds(activity, _id, adsListener, false);
    }

    public void showInterstitialAds(@NonNull final Activity activity, int _id, @Nullable Listener adsListener, final boolean isNeedNewRequest) {
        finalShowInterstitialAds(activity, _id, adsListener, isNeedNewRequest);
    }

    private void finalShowInterstitialAds(@NonNull final Activity activity, int _id, @Nullable Listener adsListener, final boolean isNeedNewRequest) {
        Log("showInterstitialAds: _id :- " + _id + " isShowingAd :- " + isShowingAd);

        if (!AppUtils.isContextActive(activity)) {
            Log("showInterstitialAds activity null");
            return;
        }
        if (isInterstitialAdsAvailableToShow(activity)) {
            isShowingAd = true;

            if (isPurchased(activity)) {
                Log("showInterstitialAds isPurchased");
                if (adsListener != null) {
                    adsListener.onAdsClosed();
                }
                isShowingAd = false;
                return;
            }

            if (mInterstitialAd != null) {

                if (_id != 1) {
                    final int min = 1;
                    final int max = _id;
                    final int ad_id = randomGenerator.nextInt((max - min) + 1) + min;
                    Log("showInterstitialAds: _id :- " + ad_id);
                    if (ad_id != 1) {
                        if (adsListener != null) {
                            adsListener.onAdsClosed();
                        }
                        isShowingAd = false;
                        return;
                    }
                }

                isAppStartUpAdsEnabled = false;
                isShowingAd = true;
                Log("showInterstitialAds: isShowingAd");

                fullScreenContentCallback = new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log("onAdDismissedFullScreenContent: ");

                        mInterstitialAd = null;
                        isShowingAd = false;

                        if (adsListener != null) {
                            adsListener.onAdsClosed();
                        }

                        if (isNeedNewRequest) {
                            loadInterstitialAds(null);
                        }

                        fullScreenContentCallback = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        // Called when fullscreen content failed to show.
                        Log("onAdFailedToShowFullScreenContent: ");
                        if (adsListener != null) {
                            adsListener.onAdsLoadFailed();
                        }


                        isShowingAd = false;
                        fullScreenContentCallback = null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {


                        mInterstitialAd = null;
                        isShowingAd = true;

                        if (adsListener != null) {
                            adsListener.onAdsShowing();
                        }
                    }
                };
                mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);

                mInterstitialAd.show(activity);

            } else {
                isAppStartUpAdsEnabled = false;
                isShowingAd = false;

                if (isNeedNewRequest) {
                    loadInterstitialAds(null);
                }

                if (adsListener != null) {
                    adsListener.onAdsLoadFailed();
                }
            }
        } else {
            Log("showInterstitialAds: else isShowingAd :- " + isShowingAd + " isDisableAds :- " + isDisableAds + " isAppOpen :- "
                    + isAppOpen + " mInterstitialAd :- " + "may null" + " isPurchased :- " + isPurchased(activity) + " activity :- may null");

            if (isNeedNewRequest && mInterstitialAd == null) {
                loadInterstitialAds(null);
            }

            if (adsListener != null) {
                adsListener.onAdsLoadFailed();
            }
        }
//        activity.runOnUiThread(() -> {
//            if (isInterstitialAdsAvailableToShow(activity)) {
//                isShowingAd = true;
//
//                if (isPurchased(activity)) {
//                    Log("showInterstitialAds isPurchased");
//                    if (adsListener != null) {
//                        adsListener.onAdsClosed();
//                    }
//                    isShowingAd = false;
//                    return;
//                }
//
//                if (mInterstitialAd != null) {
//
//                    if (_id != 1) {
//                        final int min = 1;
//                        final int max = _id;
//                        final int ad_id = randomGenerator.nextInt((max - min) + 1) + min;
//                        Log("showInterstitialAds: _id :- " + ad_id);
//                        if (ad_id != 1) {
//                            if (adsListener != null) {
//                                adsListener.onAdsClosed();
//                            }
//                            isShowingAd = false;
//                            return;
//                        }
//                    }
//
//                    isAppStartUpAdsEnabled = false;
//                    isShowingAd = true;
//                    Log("showInterstitialAds: isShowingAd");
//
//                    fullScreenContentCallback = new FullScreenContentCallback() {
//                        @Override
//                        public void onAdDismissedFullScreenContent() {
//                            Log("onAdDismissedFullScreenContent: ");
//
//                            mInterstitialAd = null;
//                            isShowingAd = false;
//                            AppOpenManagerNew.isShowingAd = false;
//
//                            if (adsListener != null) {
//                                adsListener.onAdsClosed();
//                            }
//
//                            if (isNeedNewRequest) {
//                                loadInterstitialAds(null);
//                            }
//
//                            fullScreenContentCallback = null;
//                        }
//
//                        @Override
//                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                            // Called when fullscreen content failed to show.
//                            Log("onAdFailedToShowFullScreenContent: ");
//                            if (adsListener != null) {
//                                adsListener.onAdsLoadFailed();
//                            }
//                            AppOpenManagerNew.isShowingAd = false;
//
//                            isShowingAd = false;
//                            fullScreenContentCallback = null;
//                        }
//
//                        @Override
//                        public void onAdShowedFullScreenContent() {
//                            AppOpenManagerNew.isShowingAd = true;
//
//                            mInterstitialAd = null;
//                            isShowingAd = true;
//
//                            if (adsListener != null) {
//                                adsListener.onAdsShowing();
//                            }
//                        }
//                    };
//                    mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
//
//                    mInterstitialAd.show(activity);
//
//                } else {
//                    isAppStartUpAdsEnabled = false;
//                    isShowingAd = false;
//
//                    if (isNeedNewRequest) {
//                        loadInterstitialAds(null);
//                    }
//
//                    if (adsListener != null) {
//                        adsListener.onAdsLoadFailed();
//                    }
//                }
//            }
//            else {
//                Log("showInterstitialAds: else isShowingAd :- " + isShowingAd + " isDisableAds :- " + isDisableAds + " isAppOpen :- "
//                        + isAppOpen + " mInterstitialAd :- " + "may null" + " isPurchased :- " + isPurchased(activity) + " activity :- may null");
//
//                if (isNeedNewRequest && mInterstitialAd == null) {
//                    loadInterstitialAds(null);
//                }
//
//                if (adsListener != null) {
//                    adsListener.onAdsLoadFailed();
//                }
//            }
//        });
    }

    /* TODO Native ads */
    public void loadNativeAds(String nativeids) {
        loadNativeAds(null, nativeids);
    }

    public void loadNativeAds(@Nullable Listener listener, String nativeids, Activity activity) {


        if (!AppUtils.isContextActive(activity)) {
            Log("loadNativeAds: activity null");
            return;
        }

        loadNativeAds(activity, 1, nativeids, 1, listener);
    }

    public void loadNativeAds(@Nullable Listener listener, String nativeids) {


        if (!AppUtils.isContextActive(activity)) {
            Log("loadNativeAds: activity null");
            return;
        }

        loadNativeAds(activity, 1, nativeids, 1, listener);
    }

    private void loadNativeAds(@Nullable Listener listener) {

        if (!AppUtils.isContextActive(activity)) {
            Log("loadNativeAds: activity null");
            return;
        }

        loadNativeAds(activity, 1, activity.getString(R.string.admob_native_ads_id), 0, listener);
    }

    private void loadNativeAds(@NonNull Activity activity, final int size, @NonNull final String nativeAdId, final int index, @Nullable Listener listener) {
        Log.e("AdvertiseHandler", "loadNativeAds: " + nativeAdId);

        if (!AppUtils.isContextActive(activity)) {
            Log("loadNativeAds: activity null");
            return;
        }

        Log.e("TAG1236", "loadNativeAds: " + isPurchased(activity));
        if (isPurchased(activity)) {
            Log("loadNativeAds: isPurchased");
            if (listener != null) {
                listener.onAdsLoadCompleted();
            }
            isNativeAdsLoading = false;
            return;
        }

        if (isNetworkNotAvailable(activity)) {
            Log("loadNativeAds: no network");
            if (listener != null) {
                listener.onAdsLoadCompleted();
            }
            isNativeAdsLoading = false;
            return;
        }

        if (nativeAdList.size() > MAX_NATIVE_ADS_STORE) {
            Log("loadNativeAds: loading nativeAdList :- " + nativeAdList.size());
            if (listener != null) {
                listener.onAdsLoadCompleted();
            }
            isNativeAdsLoading = false;
            return;
        }


        if (listener != null && !nativeAdLoadCompleteList.contains(listener)) {
            nativeAdLoadCompleteList.add(listener);
        }

        if (!isNativeAdsLoading) {

            final int[] i = {0};
            isNativeAdsLoading = true;
            Log("loadNativeAds: isNativeAdsLoading listener :- " + listener);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AppUtils.isContextActive(activity)) {
                        AdLoader adLoader = new AdLoader.Builder(activity.getApplicationContext(), nativeAdId)
                                .forNativeAd(nativeAdList::add)
                                .withAdListener(
                                        new AdListener() {
                                            @SuppressLint("NewApi")
                                            @Override
                                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                                                Log("onAdFailedToLoad: loadNativeAds loadAdError :- " + loadAdError.getMessage() + " nativeAdId :- " + nativeAdId + "index ID :-" + index);
                                                i[0]++;

                                                if (i[0] == size) {
                                                    isNativeAdsLoading = false;
                                                    if (loadOnFailed && index >= 0 && nativeAdsIDList.size() > index) {
                                                        Log.e("TAG852", "onAdFailedToLoad: " + loadAdError.getMessage());
                                                        loadNativeAds(AdvertiseHandler.this.activity, size, nativeAdsIDList.get(index), index + 1, listener);
                                                    } else {


                                                        for (Listener onCompleteListener : nativeAdLoadCompleteList) {
                                                            if (onCompleteListener != null) {
                                                                onCompleteListener.onAdsLoadCompleted();

                                                                // ConcurrentModificationException Ni problume hati ne Atle
                                                                //onCompleteListener = null;
                                                            }
                                                        }
                                                        nativeAdLoadCompleteList.clear();
                                                    }
                                                }
                                            }

                                            @SuppressLint("NewApi")
                                            @Override
                                            public void onAdLoaded() {
                                                super.onAdLoaded();
                                                i[0]++;
                                                Log.e("AdvertiseHandler", "onAdLoaded: fetchNativeAdsBeforeLoads i[0] :- " + i[0] + " size :- " + size);
//                                                Log("onAdLoaded: fetchNativeAdsBeforeLoads i[0] :- " + i[0] + " size :- " + size);

                                                if (i[0] == size) {

//                                                    Listener listener=new Listener();
//                                                    listener.onAdsLoadCompleted();

//                                                    for (Iterator<Listener> iterator = nativeAdLoadCompleteList.iterator(); iterator.hasNext(); ) {
//                                                        Listener onCompleteListener  = iterator.next();
//                                                        onCompleteListener.onAdsLoadCompleted();
//
//                                                    }

//
                                                    for (Iterator<Listener> it = nativeAdLoadCompleteList.iterator(); it.hasNext(); ) {
                                                        Listener onCompleteListener = it.next();
//                                                    for (Listener onCompleteListener : nativeAdLoadCompleteList) {
                                                        Log("onAdLoaded: fetchNativeAdsBeforeLoads onCompleteListener :- " + onCompleteListener);
                                                        if (onCompleteListener != null) {
                                                            onCompleteListener.onAdsLoadCompleted();
                                                            onCompleteListener = null;
                                                        }
                                                    }
                                                    nativeAdLoadCompleteList.clear();
                                                    isNativeAdsLoading = false;
                                                }
                                            }
                                        })
                                .withNativeAdOptions(
                                        new NativeAdOptions.Builder()
                                                .setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build())
                                                .build())
                                .build();

                        adLoader.loadAds(getAdRequest(), size);
                    }
                }
            });
        } else {
            Log("loadNativeAds: isNativeAdsLoading isNativeAdsLoading :- " + true);
        }
    }


    public void showNativeAds(
            @NonNull final Context context,
            @Nullable final ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout) {
        showNativeAds(context, rootView, isShowMedia, resourceLayout, null);
    }

    public void showNativeAds(
            @NonNull final Context context,
            @Nullable final ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout,
            @Nullable Listener listener) {

        if (!AppUtils.isContextActive(context)) {
            Log("showNativeAds: isContextActive null");
            return;
        }

        if (rootView == null) {
            Log("showNativeAds: rootView null");
            if (listener != null) {
                listener.onAdsLoadFailed();
            }
            return;
        }

        if (isPurchased(context)) {
            Log("showNativeAds: isPurchased");
            if (listener != null) {
                listener.onAdsLoadFailed();
            }
            return;
        }

        if (listener != null) {
            listener.onNativeAdsShowStart();
        }
        Log("showNativeAds: size :- " + nativeAdList.size());

        if (nativeAdList.size() > 0) {
            finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener, false);

            if (nativeAdList.size() < MAX_NATIVE_ADS_STORE) {

                int _id = MAX_NATIVE_ADS_STORE;
                final int min = 1;
                final int max = _id;
                _id = randomGenerator.nextInt((max - min) + 1) + min;
                Log("showNativeAds: _id :- " + _id);
                if (_id != 1) {
                    return;
                }

                tempListener = new Listener() {
                    @Override
                    public void onAdsLoadCompleted() {
                        finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener);
                        tempListener = null;
                    }
                };
                loadNativeAds(tempListener);
            }
        } else {
            tempListener = new Listener() {
                @Override
                public void onAdsLoadCompleted() {
                    finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener);
                    tempListener = null;
                }
            };
            loadNativeAds(tempListener);
        }
    }

    public void finalLoadNativeAds(
            @NonNull Context context,
            @Nullable ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout) {
        finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, null);
    }

    public void finalLoadNativeAds(
            @NonNull Context context,
            @Nullable ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout,
            @Nullable Listener listener) {
        finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener, false);
    }

    public void finalLoadNativeAds(
            @NonNull Context context,
            @Nullable ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout,
            final boolean isNeedNewRequest) {
        finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, null, isNeedNewRequest);
    }

    public void finalLoadExitNativeAds(
            @NonNull Context context,
            @Nullable ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout) {

        if (!AppUtils.isContextActive(context)) {
            Log("finalLoadNativeAds: isContextActive null");
            return;
        }


        if (isPurchased(context)) {
            Log("finalLoadExitNativeAds: isPurchased");
            if (rootView != null) {
                rootView.setVisibility(View.GONE);
            }
            return;
        }

        if (exitNativeAds != null) {
            Log("finalLoadExitNativeAds: load from exitNativeAds");
            finalLoadNativeAds1(context, rootView, isShowMedia, resourceLayout, exitNativeAds, null);
        } else {
            Log("finalLoadExitNativeAds: load from normal");
            finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout);
        }
    }

    public void finalLoadNativeAds(
            @NonNull Context context,
            @Nullable ViewGroup rootView,
            final boolean isShowMedia,
            final @LayoutRes int resourceLayout,
            @Nullable Listener listener,
            final boolean isNeedNewRequest) {

        Log("finalLoadNativeAds: size :- " + nativeAdList.size());

        if (!AppUtils.isContextActive(context)) {
            Log("finalLoadNativeAds: isContextActive null");
            return;
        }

        if (nativeAdList.size() > 0) {

            if (isPurchased(context)) {
                Log("finalLoadNativeAds: isPurchased");
                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.GONE);
                    }
                } else {
                    listener.onAdsLoadFailed();
                }
                return;
            }

            NativeAd nativeAd;
            if (nativeAdList.size() > 1) {
                List<NativeAd> tempNativeAdList = new ArrayList<>(nativeAdList);
                Collections.shuffle(tempNativeAdList);
                nativeAd = tempNativeAdList.get(0);
            } else {
                nativeAd = nativeAdList.get(0);
            }

            finalLoadNativeAds1(context, rootView, isShowMedia, resourceLayout, nativeAd, listener);

        } else {

            if (isNeedNewRequest) {
                tempListener = new Listener() {
                    @Override
                    public void onAdsLoadCompleted() {
                        if (nativeAdList.size() > 0) {
                            finalLoadNativeAds(context, rootView, isShowMedia, resourceLayout, listener);
                        }
                        tempListener = null;
                    }
                };
                loadNativeAds(tempListener);
            } else {
                if (listener == null) {
                    if (rootView != null) {
                        rootView.setVisibility(View.GONE);
                    }
                } else {
                    listener.onAdsLoadFailed();
                }
            }
        }
    }

    private void finalLoadNativeAds1(Context context, ViewGroup rootView, boolean isShowMedia, int resourceLayout, NativeAd nativeAd, Listener listener) {

        if (!AppUtils.isContextActive(context)) {
            Log("finalLoadNativeAds1: isContextActive null");
            return;
        }

        if (nativeAd == null) {
            if (listener == null) {
                if (rootView != null) {
                    rootView.setVisibility(View.GONE);
                }
            } else {
                listener.onAdsLoadFailed();
            }
            return;
        }

        final View view = LayoutInflater.from(context).inflate(resourceLayout, null);
        if (view instanceof NativeAdView) {

            NativeAdView adView = (NativeAdView) view;
            adView.setTag(NativeAdView.class.getName());

            final MediaView mediaView = adView.findViewById(R.id.ad_media);
            final View media_lay = adView.findViewById(R.id.media_lay);
            if (mediaView != null) {
                if (!isShowMedia || nativeAd.getMediaContent() == null) {
                    mediaView.setVisibility(View.GONE);

                    if (media_lay != null) {
                        media_lay.setVisibility(View.GONE);
                    }
                } else {
                    mediaView.setMediaContent(nativeAd.getMediaContent());
                    mediaView.setVisibility(View.VISIBLE);

                    if (media_lay != null) {
                        media_lay.setVisibility(View.VISIBLE);
                    }

                    adView.setMediaView(mediaView);
                }
            } else {
                if (media_lay != null) {
                    media_lay.setVisibility(View.GONE);
                }
            }

            final TextView adTitle = adView.findViewById(R.id.ad_headline);
            if (adTitle != null) {
                adTitle.setText(nativeAd.getHeadline());
                adView.setHeadlineView(adTitle);
            }

            final TextView adDescription = adView.findViewById(R.id.ad_body);
            if (adDescription != null) {
                if (nativeAd.getBody() == null || nativeAd.getBody().trim().isEmpty()) {
                    adDescription.setVisibility(View.GONE);

                    if (adTitle != null) {
                        adTitle.setMaxLines(3);
                        adTitle.setSingleLine(false);
                    }

                } else {
                    adDescription.setText(nativeAd.getBody());
                    adView.setBodyView(adDescription);
                    adDescription.setVisibility(View.VISIBLE);
                }
            }

//                final TextView ad_price = adView.findViewById(R.id.ad_price);
//                if (ad_price != null) {
//                    String price = nativeAd.getPrice();
//                    if (price != null && !price.trim().isEmpty()) {
//                        ad_price.setText(price);
//                        adView.setPriceView(adTitle);
//                        ad_price.setVisibility(View.VISIBLE);
//                    } else {
//                        ad_price.setVisibility(View.INVISIBLE);
//                    }
//                }
//
//                final TextView ad_store = adView.findViewById(R.id.ad_store);
//                if (ad_store != null) {
//                    String store = nativeAd.getStore();
//                    if (store != null && !store.trim().isEmpty()) {
//                        ad_store.setText(store);
//                        adView.setStoreView(ad_store);
//                        ad_store.setVisibility(View.VISIBLE);
//                    } else {
//                        ad_store.setVisibility(View.INVISIBLE);
//                    }
//                }

//                final TextView ad_advertiser = adView.findViewById(R.id.ad_advertiser);
//                if (ad_advertiser != null) {
//                    String advertiser = nativeAd.getAdvertiser();
//                    if (advertiser != null && !advertiser.trim().isEmpty()) {
//                        ad_advertiser.setText(advertiser);
//                        adView.setAdvertiserView(ad_advertiser);
//                        ad_advertiser.setVisibility(View.VISIBLE);
//                    } else {
//                        ad_advertiser.setVisibility(View.INVISIBLE);
//                    }
//                }

            final AppCompatButton callToAction = adView.findViewById(R.id.ad_call_to_action);
            if (callToAction != null) {
                callToAction.setText(nativeAd.getCallToAction());
                adView.setCallToActionView(callToAction);
            }

//                final RatingBar ad_stars = adView.findViewById(R.id.ad_stars);
//                if (ad_stars != null) {
//                    if (nativeAd.getStarRating() == null || nativeAd.getStarRating() == null) {
//                        ad_stars.setVisibility(View.GONE);
//                    } else {
//                        ad_stars.setRating(nativeAd.getStarRating().floatValue());
//                        ad_stars.setVisibility(View.VISIBLE);
//                        adView.setStarRatingView(ad_stars);
//                    }
//                }

            ImageView imageView = adView.findViewById(R.id.ad_app_icon);
            if (imageView != null) {
                if (nativeAd.getIcon() == null || nativeAd.getIcon().getDrawable() == null) {
                    if (nativeAd.getMediaContent() != null && nativeAd.getMediaContent().getMainImage() != null) {
                        imageView.setImageDrawable(nativeAd.getMediaContent().getMainImage());
                        imageView.setVisibility(View.VISIBLE);
                        adView.setIconView(imageView);
                        adView.findViewById(R.id.ad_app_icon_Lay).setVisibility(View.VISIBLE);
                    } else {
                        imageView.setVisibility(View.GONE);
                        adView.findViewById(R.id.ad_app_icon_Lay).setVisibility(View.GONE);
                    }
                } else {
                    imageView.setImageDrawable(nativeAd.getIcon().getDrawable());
                    imageView.setVisibility(View.VISIBLE);
                    adView.setIconView(imageView);
                    adView.findViewById(R.id.ad_app_icon_Lay).setVisibility(View.VISIBLE);
                }
            }
            adView.setNativeAd(nativeAd);

            if (rootView != null) {
                rootView.removeAllViews();
                rootView.addView(adView);
            }

            if (listener == null) {
                if (rootView != null) {
                    rootView.setVisibility(View.VISIBLE);
                }
            } else {
                listener.onAdsShowing();
            }

        } else {
            if (listener == null) {
                if (rootView != null) {
                    rootView.setVisibility(View.GONE);
                }
            } else {
                listener.onAdsLoadFailed();
            }
        }
    }

    public void loadExitNativeAds(@NonNull Activity activity) {

        if (!AppUtils.isContextActive(activity)) {
            Log("loadExitNativeAds: activity null");
            return;
        }

        if (isPurchased(activity)) {
            Log("loadExitNativeAds: isPurchased");
            isExitNativeAdsLoading = false;
            return;
        }

        if (isNetworkNotAvailable(activity)) {
            Log("loadExitNativeAds: no network");
            isExitNativeAdsLoading = false;
            return;
        }

        if (exitNativeAds != null) {
            Log("loadExitNativeAds: exitNativeAds loaded");
            isExitNativeAdsLoading = false;
            return;
        }

        if (!isExitNativeAdsLoading) {

            isExitNativeAdsLoading = true;
            int index = 0;
            Log("loadExitNativeAds: isNativeAdsAfterCallLoading ");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AppUtils.isContextActive(activity)) {
                        AdLoader adLoader =
                                new AdLoader.Builder(activity.getApplicationContext(), activity.getString(R.string.admob_exit_native_ads_id))
                                        .forNativeAd(nativeAd -> exitNativeAds = nativeAd)
                                        .withAdListener(
                                                new AdListener() {
                                                    @Override
                                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                                        Log("onAdFailedToLoad: loadExitNativeAds loadAdError :- " + loadAdError.getMessage());

                                                        isExitNativeAdsLoading = false;
                                                        if (loadOnFailed && nativeAdsIDList.size() > index) {
                                                            loadNativeAds(activity, 1, nativeAdsIDList.get(index), index + 1, null);
                                                        }
                                                    }

                                                    @Override
                                                    public void onAdLoaded() {
                                                        super.onAdLoaded();
                                                        Log("onAdLoaded: fetchNativeAdsBeforeLoads ");
                                                        isExitNativeAdsLoading = false;
                                                    }
                                                })
                                        .withNativeAdOptions(
                                                new NativeAdOptions.Builder()
                                                        .setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build())
                                                        .build())
                                        .build();

                        adLoader.loadAds(getAdRequest(), 1);
                    }
                }
            });
        }
    }

    private void loadAfterCallScreenNativeAds(@NonNull Activity activity, @Nullable final Listener listener) {

        if (!AppUtils.isContextActive(activity)) {
            Log("loadAfterCallScreenNativeAds: activity null");
            return;
        }

        if (isPurchased(activity)) {
            Log("loadAfterCallScreenNativeAds: isPurchased");
            isAfterCallScreenNativeAdsLoading = false;
            if (listener != null) {
                listener.onAdsLoadFailed();
            }
            return;
        }

        if (isNetworkNotAvailable(activity)) {
            Log("loadAfterCallScreenNativeAds: no network");
            isAfterCallScreenNativeAdsLoading = false;
            if (listener != null) {
                listener.onAdsLoadFailed();
            }
            return;
        }

        if (!isAfterCallScreenNativeAdsLoading) {

            isAfterCallScreenNativeAdsLoading = true;
            int index = 0;
            Log("loadNativeAds: isNativeAdsAfterCallLoading ");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AppUtils.isContextActive(activity)) {
                        AdLoader adLoader =
                                new AdLoader.Builder(activity.getApplicationContext(), activity.getString(R.string.admob_native_ads_id))
                                        .forNativeAd(nativeAd -> {
                                            if (AppUtils.isContextActive(activity)) {
                                                if (listener != null) {
                                                    listener.onNativeAdsLoaded(nativeAd);
                                                }
                                            }
                                        })
                                        .withAdListener(
                                                new AdListener() {
                                                    @Override
                                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                                        Log("onAdFailedToLoad: loadNativeAds loadAdError :- " + loadAdError.getMessage());

                                                        isAfterCallScreenNativeAdsLoading = false;

                                                        if (loadOnFailed && nativeAdsIDList.size() > index) {
                                                            loadNativeAds(activity, 1, nativeAdsIDList.get(index), index + 1, listener);
                                                        } else {
                                                            if (AppUtils.isContextActive(activity)) {
                                                                if (listener != null) {
                                                                    listener.onAdsLoadCompleted();
                                                                }
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onAdLoaded() {
                                                        super.onAdLoaded();
                                                        Log("onAdLoaded: fetchNativeAdsBeforeLoads ");
                                                        isAfterCallScreenNativeAdsLoading = false;
                                                    }
                                                })
                                        .withNativeAdOptions(
                                                new NativeAdOptions.Builder()
                                                        .setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build())
                                                        .build())
                                        .build();

                        adLoader.loadAds(getAdRequest(), 1);
                    }
                }
            });
        }
    }

    public boolean isNativeAdsAvailable(Context context) {
        return nativeAdList.size() > 0 && AppUtils.isContextActive(context) && !isPurchased(context);
    }

    public boolean isExitNativeAdsAvailable(Context context) {
        return AppUtils.isContextActive(context) && !isPurchased(context) && (exitNativeAds != null || nativeAdList.size() > 0);
    }

//    private void loadAdOpenAds(@NonNull final Activity activity) {
//
//        if (!AppUtils.isContextActive(activity)) {
//            Log("loadAdOpenAds: activity null");
//            return;
//        }
//
//        Log("loadAdOpenAds: isAppOpenAdEnable :- " + isAppOpenAdEnable + " isNeedOpenAdRequest :- " + isNeedOpenAdRequest + " isShowingAd :- " + isShowingAd);
//        if (isAppOpenAdEnable && isNeedOpenAdRequest && !isShowingAd) {
//            loadAdOpenAds(activity, activity.getString(R.string.admob_adex_appopen), 0);
//        }
//    }

//    private void loadAdOpenAds(@NonNull final Activity activity, @NonNull final String appOpenId, final int index) {
//        Log("loadAdOpenAds: isAppOpenAdLoading :- " + isAppOpenAdLoading);
//
//        if (!AppUtils.isContextActive(activity)) {
//            Log("loadAdOpenAds: activity null");
//            return;
//        }
//
//        if (!isAppOpenAdLoading) {
//            isAppOpenAdLoading = true;
//
//            if (isShowingAd) {
//                Log("loadAdOpenAds: isShowingAd :- " + true);
//                isAppOpenAdLoading = false;
//                return;
//            }
//
//            if (isDisableAds) {
//                Log("loadAdOpenAds: isDisableAds ");
//                isAppOpenAdLoading = false;
//                return;
//            }
//
//            if (isPurchased(activity)) {
//                Log("loadAdOpenAds purchase");
//                isAppOpenAdLoading = false;
//                return;
//            }
//
//            if (isNetworkNotAvailable(activity)) {
//                Log("loadAdOpenAds: no network");
//                isAppOpenAdLoading = false;
//                return;
//            }
//
//            if (appOpenAd != null) {
//                Log("loadAdOpenAds: onAdLoaded open ad already");
//                isAppOpenAdLoading = false;
//                showAdIfAvailable();
//                return;
//            }
//
//            isAppOpenAdLoading = true;
//            Log("loadAdOpenAds: isAppOpenAdLoading ");
//            if (index == 0) {
//                appOpenLoadTime = System.currentTimeMillis();
//            }
//
//            AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
//                @Override
//                public void onAdLoaded(@NonNull AppOpenAd ad) {
//                    super.onAdLoaded(ad);
//                    Log("loadAdOpenAds: onAdLoaded open ad loaded");
//
//                    appOpenAd = ad;
//                    isAppOpenAdLoading = false;
//
//                    if (AppUtils.isContextActive(activity)) {
//                        long current = System.currentTimeMillis();
//                        if ((current - appOpenLoadTime) <= ADS_MAX_LOAD_TIME) {
//                            showAdIfAvailable();
//                        }
//                    }
//                    appOpenLoadTime = 0L;
//                }
//
//                @Override
//                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                    super.onAdFailedToLoad(loadAdError);
//                    Log("loadAdOpenAds onAdFailedToLoad: loadAdError :- " + loadAdError.getMessage() + " appOpenId :- " + appOpenId);
//                    isAppOpenAdLoading = false;
//
//                    if (loadOnFailed && index >= 0 && appOpenAdsIDList.size() > index) {
//                        loadAdOpenAds(AdvertiseHandler.this.activity, appOpenAdsIDList.get(index), index + 1);
//                    } else {
//                        appOpenLoadTime = 0L;
//                    }
//                }
//            };
//
//            AppOpenAd.load(
//                    activity.getApplicationContext(),
//                    appOpenId,
//                    getAdRequest(),
//                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//                    loadCallback);
//        } else {
//            appOpenLoadTime = System.currentTimeMillis();
//        }
//    }

//    private void showAdIfAvailable() {
//
//        if (!AppUtils.isContextActive(activity)) {
//            Log("showAdIfAvailable: activity null");
//            return;
//        }
//
//        activity.runOnUiThread(() -> {
//            if (appOpenAd != null && !isShowingAd && isAppOpen) {
//                isShowingAd = true;
//
//                if (isPurchased(activity)) {
//                    Log("onAdFailedToLoad: showAdIfAvailable purchased");
//                    isShowingAd = false;
//                    return;
//                }
//
//                FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
//                    @Override
//                    public void onAdDismissedFullScreenContent() {
//                        Log("loadAdOpenAds: onAdDismissedFullScreenContent: ");
//                        appOpenAd = null;
//                        isShowingAd = false;
//                    }
//
//                    @Override
//                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                        Log("loadAdOpenAds: Error display show ad." + adError.getMessage());
//                        isShowingAd = false;
//                    }
//
//                    @Override
//                    public void onAdShowedFullScreenContent() {
//                        Log("loadAdOpenAds: onAdShowedFullScreenContent: ");
//                        isShowingAd = true;
//                    }
//                };
//
//                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
//                appOpenAd.show(activity);
//
//            } else {
//                Log("Can not show ad. appOpenAd :- " + "may null" + " isShowingAd :- " + isShowingAd);
////            loadAdOpenAds(appOpenId);
//            }
//        });
//    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log("onActivityCreated: activity :- " + activity + " isSDKInitialized :- " + isSDKInitialized);
        this.activity = activity;
//        disableAppOpenAds();
        isAppOpen = true;
        isDisableAds = false;

        if (!isSDKInitialized) {
            if (AppUtils.isContextActive(activity)) {
                isSDKInitialized = true;
                activity.runOnUiThread(() -> {

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            String process = Application.getProcessName();
                            if (!activity.getPackageName().equals(process)) {
                                WebView.setDataDirectorySuffix(process);
                            } else {
                                webView = new WebView(activity);
                                MobileAds.initialize(activity, initializationStatus -> setTestDeviceIDS());
                            }
                        } else {
                            webView = new WebView(activity);
                            MobileAds.initialize(activity, initializationStatus -> setTestDeviceIDS());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void setTestDeviceIDS() {

        if (BuildConfig.DEBUG) {
            if (AppUtils.isContextActive(activity)) {
                List<String> testDeviceIds = new ArrayList<>();
                testDeviceIds.add("25555CF3B61C05BDA80C839A7D9A20EC");
                testDeviceIds.add("1685D79BA73277929C48A9FCCD3336AA");
                testDeviceIds.add("13360D68428B5437B7006DE167FD90EC");
                testDeviceIds.add("79D0A9065DC733DF4E5FE2349926C84C");
                testDeviceIds.add("8153549C826A9696C1923E874F61E887");
                testDeviceIds.add("E80FAB95779C3663F40B59EC5A8AA783");
                testDeviceIds.add("B9CDC01147138D21F9493E49396E9568");
                testDeviceIds.add("DD0F3E1582B3B2B9167686F42E8B8FEE");
                testDeviceIds.add("8828894160442C212EEFD96D5D8DC105");
                RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                MobileAds.setRequestConfiguration(configuration);
            }
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log("onActivityStarted: ");
        this.activity = activity;
        isAppOpen = true;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        this.activity = activity;
        isAppOpen = true;
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//
//            loadAdOpenAds(activity);
////
//            isAppOpenAdEnable = false;
//            isNeedOpenAdRequest = true;
//        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log("onActivityPaused: ");
        this.activity = activity;
        isAppOpen = false;
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//            isAppOpenAdEnable = isApplicationBroughtToBackground();
//        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log("onActivityStopped: ");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        Log("onActivitySaveInstanceState: ");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log("onActivityDestroyed: ");
//        isAppOpenAdEnable = false;

        if (this.activity == activity) {
            this.activity = null;
        }

        if (webView != null) {
            webView.destroy();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        isAppOpen = true;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//
//            loadAdOpenAds(activity);
////
//            isAppOpenAdEnable = false;
//            isNeedOpenAdRequest = true;
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log("onEnterBackground: ");
        isAppOpen = false;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            isAppOpenAdEnable = true;
//        }
    }

    private boolean isApplicationBroughtToBackground() {
        if (activity != null) {
            ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks != null && !tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    return topActivity != null && !topActivity.getPackageName().equals(activity.getPackageName());
                }
            }
        }
        return false;
    }

    public void removeNativeAdsListener(Listener listener) {
        if (nativeAdLoadCompleteList.size() > 0) {
            if (listener != null) {
                nativeAdLoadCompleteList.remove(listener);
            }
            listener = null;
        }
    }

    public void removeInterstitialAdsListener(Listener listener) {
        Log("onAdLoaded: loadInterstitialAds appAdsListener remove :- " + listener);
        if (listener != null && appAdsListener == listener) {
            appAdsListener = null;
        }
        listener = null;
    }

    public void disableAppOpenAds() {
//        isAppOpenAdEnable = false;
//        isNeedOpenAdRequest = false;
    }

    public void setAppOpen() {
        isAppOpen = true;
    }

    public static class Listener {

        public void onNativeAdsShowStart() {
        }

        public void onAdsShowing() {
        }

        public void onAdsLoadCompleted() {
        }

        public void onNativeAdsLoaded(NativeAd nativeAd) {

        }

        public void onAdsClosed() {

        }

        public void onAdsLoadFailed() {
            onAdsClosed();
        }
    }
}