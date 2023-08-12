package com.chating.messages.common.session

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import java.util.*
import javax.inject.Singleton

/**
 * Author: BHAVEN SHAH
 * Date: 01-09-2021
 * Organization: Erasoft Technology
 */

@Singleton
class SessionManager @Singleton private constructor(context: Context) {
    private val myEdit: SharedPreferences.Editor
    private val sharedPreferences: SharedPreferences
    private val SHARED_PREFS = "smart_messages"
    private val screenTime: Long = 20000

    fun setFirstTime(value: Boolean) {
        myEdit.putBoolean("isFirstTime", value)
        myEdit.commit()
    }

    fun getFirstTime(): Boolean {
        return sharedPreferences.getBoolean("isFirstTime", false)
    }
    fun setRateUs(rateUsYesClick: Boolean) {
        myEdit.putBoolean(SHARED_PREFS_RATE_US, rateUsYesClick).apply()
    }

    // 12hrs
    val rate: Boolean
        get() {
            var isEnabled = sharedPreferences.getBoolean(SHARED_PREFS_RATE_US, false)
            if (!isEnabled) {
                val time = rateUsShowTime
                if (time == 0L) {
                    isEnabled = false
                } else {
                    val date = Date()
                    val date1 = Date(time)
                    var calc = Math.abs(date.time - date1.time)
                    calc /= 1000
                    isEnabled = calc < 43200 // 12hrs
                }
            }
            if (!isEnabled) {
                val min = 1
                val max = 2
                val _id = Random().nextInt(max - min + 1) + min
                isEnabled = _id != 1
            }
            return isEnabled
        }

    fun setRateUsShowTime() {
        myEdit.putLong(SHARED_PREFS_RATE_US_LAST_SHOW_TIME, Date().time).apply()
    }

    private val rateUsShowTime: Long
        get() = sharedPreferences.getLong(SHARED_PREFS_RATE_US_LAST_SHOW_TIME, 0L)

    var screenAdded: Boolean
        get() = sharedPreferences.getBoolean(SHARED_AFTER_CALL_SCREEN_ADDED, false)
        set(isScreenAdded) {
            myEdit.putBoolean(SHARED_AFTER_CALL_SCREEN_ADDED, isScreenAdded).apply()
        }

    var contactKeyboardType: Boolean
        get() = sharedPreferences.getBoolean(SHARED_CONTACT_KEYBOARD, true)
        set(isKeyboard) {
            myEdit.putBoolean(SHARED_CONTACT_KEYBOARD, isKeyboard).apply()
        }

    val afterCallScreenEnabled: Boolean
        get() = sharedPreferences.getBoolean(SHARED_AFTER_CALL_SCREEN_ENABLED, false)

    fun setAfterCallScreen(isAfterCallScreenEnabled: Boolean) {
        myEdit.putBoolean(SHARED_AFTER_CALL_SCREEN_ENABLED, isAfterCallScreenEnabled).apply()
    }

    fun disableScreenAdded(isImmediate: Boolean) {
        if (screenAdded) {
            if (isImmediate) {
                screenAdded = false
            } else {
                Handler(Looper.myLooper()!!).postDelayed({ screenAdded = false }, screenTime)
            }
        }
    }

    companion object {
        private const val SHARED_PREFS_RATE_US = "shared_pref_rate_us"
        private const val SHARED_PREFS_RATE_US_LAST_SHOW_TIME = "shared_pref_rate_us_show_time"
        private const val SHARED_AFTER_CALL_SCREEN_ADDED = "after_call_screen_added"
        private const val SHARED_CONTACT_KEYBOARD = "contact_keyboard"
        private const val SHARED_AFTER_CALL_SCREEN_ENABLED = "after_call_screen_enabled"

        @Volatile
        private var sInstance: SessionManager? = null

        @JvmStatic
        fun getInstance(context: Context): SessionManager {
            if (sInstance == null) {
                synchronized(this) {
                    sInstance = SessionManager(context)
                }
            }
            return sInstance!!
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        myEdit = sharedPreferences.edit()
        myEdit.apply()
    }
}