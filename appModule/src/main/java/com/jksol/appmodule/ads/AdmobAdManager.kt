package com.jksol.appmodule.ads

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class AdmobAdManager {
    var mNativeRateAd: NativeAd? = null
    fun LoadNativeRateAd(
        context: Context?,
        nativeAdID: String?,
        adEventListener: AdEventListener?
    ) {
        if (!TextUtils.isEmpty(nativeAdID)) {
            val builder = AdLoader.Builder(context!!, nativeAdID!!)
            builder.forNativeAd { unifiedNativeAd: NativeAd? ->
                mNativeRateAd = unifiedNativeAd
                adEventListener?.onAdLoaded(unifiedNativeAd)
            }.withAdListener(object : AdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    adEventListener?.onLoadError(loadAdError.message)
                    Log.e("TAG30", "onAdFailedToLoad: " + loadAdError.code)
                  /*  ads faild to load adex Ids*/
                    LoadNativeRateAd(
                        context,
                        "/22387492205,22667063805/com.text.messages.sms.emoji.Native0.1650441490",
                        null
                    )

                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                }
            })
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            builder.withNativeAdOptions(adOptions)
            val adLoader = builder.build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    companion object {
        private var singleton: AdmobAdManager? = null
        val instance: AdmobAdManager?
            get() {
                if (singleton == null) {
                    singleton = AdmobAdManager()
                }
                return singleton
            }
    }
}