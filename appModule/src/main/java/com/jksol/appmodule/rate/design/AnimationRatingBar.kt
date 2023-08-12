package com.jksol.appmodule.rate.design

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.util.AttributeSet
import java.util.*

open class AnimationRatingBar : BaseRatingBar {
    protected var mHandler: Handler? = null
    protected var mRunnable: Runnable? = null
    protected var mRunnableToken = UUID.randomUUID().toString()

     constructor(context: Context) : super(context) {
        init()
    }

     constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

     constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mHandler = Handler()
    }

     fun postRunnable(runnable: Runnable?, ANIMATION_DELAY: Long) {
        if (mHandler == null) {
            mHandler = Handler()
        }
        val timeMillis = SystemClock.uptimeMillis() + ANIMATION_DELAY
        mHandler!!.postAtTime(runnable!!, mRunnableToken, timeMillis)
    }
}