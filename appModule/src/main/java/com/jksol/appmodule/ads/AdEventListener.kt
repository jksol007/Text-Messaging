package com.jksol.appmodule.ads

interface AdEventListener {
    fun onAdLoaded(`object`: Any?)
    fun onAdClosed()
    fun onLoadError(errorCode: String?)
}