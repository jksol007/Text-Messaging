package com.jksol.appmodule.exit

import android.content.Context
import com.jksol.appmodule.ads.AdmobAdManager
import com.jksol.appmodule.utils.Constants

class ExitNativeAdInit(context: Context, nativeId: String) {

    init {
        Constants.nativeId = nativeId
        AdmobAdManager.instance?.LoadNativeRateAd(context, nativeId, null)
    }
}