package com.jksol.appmodule.rate.design

import android.view.MotionEvent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

internal object RatingBarUtils {
    private var mDecimalFormat: DecimalFormat? = null
    private const val MAX_CLICK_DISTANCE = 5
    private const val MAX_CLICK_DURATION = 200
    fun isClickEvent(startX: Float, startY: Float, event: MotionEvent): Boolean {
        val duration = (event.eventTime - event.downTime).toFloat()
        if (duration > MAX_CLICK_DURATION) {
            return false
        }
        val differenceX = abs(startX - event.x)
        val differenceY = abs(startY - event.y)
        return !(differenceX > MAX_CLICK_DISTANCE || differenceY > MAX_CLICK_DISTANCE)
    }

    fun calculateRating(partialView: PartialView, stepSize: Float, eventX: Float): Float {
        val decimalFormat = decimalFormat
        val ratioOfView =
            decimalFormat!!.format((eventX - partialView.left) / partialView.width).toFloat()
        val steps = (ratioOfView / stepSize).roundToInt() * stepSize
        return decimalFormat.format((partialView.tag as Int - (1 - steps)).toDouble()).toFloat()
    }

    fun getValidMinimumStars(minimumStars: Float, numStars: Int, stepSize: Float): Float {
        var minimumStar = minimumStars
        if (minimumStars < 0) {
            minimumStar = 0f
        }
        if (minimumStars > numStars) {
            minimumStar = numStars.toFloat()
        }
        if (minimumStars % stepSize != 0f) {
            minimumStar = stepSize
        }
        return minimumStar
    }

    val decimalFormat: DecimalFormat?
        get() {
            if (mDecimalFormat == null) {
                val symbols = DecimalFormatSymbols(Locale.ENGLISH)
                symbols.decimalSeparator = '.'
                mDecimalFormat = DecimalFormat("#.##", symbols)
            }
            return mDecimalFormat
        }
}