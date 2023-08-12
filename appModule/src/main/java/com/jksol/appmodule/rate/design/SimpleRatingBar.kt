package com.jksol.appmodule.rate.design

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange

internal interface SimpleRatingBar {
    var numStars: Int
    var rating: Float
    var starWidth: Int
    var starHeight: Int
    var starPadding: Int

    fun setEmptyDrawable(drawable: Drawable)
    fun setEmptyDrawableRes(@DrawableRes res: Int)
    fun setFilledDrawable(drawable: Drawable)
    fun setFilledDrawableRes(@DrawableRes res: Int)
    fun setMinimumStars(@FloatRange(from = 0.0) minimumStars: Float)
    fun isIndicator(): Boolean
    fun setIsIndicator(indicator: Boolean)
    var isScrollable: Boolean
    var isClickable1: Boolean
    var isClearRatingEnabled: Boolean
    var stepSize: Float
}