package com.text.messages.sms.emoji.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.text.messages.sms.emoji.manager.WidgetManager
import com.text.messages.sms.emoji.receiver.NightModeReceiver
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NightModeManager @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val widgetManager: WidgetManager
) {

    fun updateCurrentTheme() {
        when (prefs.nightMode.get()) {
            Preferences.NIGHT_MODE_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            Preferences.NIGHT_MODE_OFF -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            Preferences.NIGHT_MODE_ON -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            Preferences.NIGHT_MODE_AUTO -> {
                val nightStartTime = getPreviousInstanceOfTime(prefs.nightStart.get())
                val nightEndTime = getPreviousInstanceOfTime(prefs.nightEnd.get())

                // If the last nightStart was more recent than the last nightEnd, then it's night time
                val night = nightStartTime > nightEndTime
                prefs.night.set(night)
                AppCompatDelegate.setDefaultNightMode(
                    when (night) {
                        true -> AppCompatDelegate.MODE_NIGHT_YES
                        false -> AppCompatDelegate.MODE_NIGHT_NO
                    }
                )
                widgetManager.updateTheme()
            }
        }
    }

    fun updateNightMode(mode: Int) {
        prefs.nightMode.set(mode)

        try {
            // If it's not on auto mode, set the appropriate night mode
            if (mode != Preferences.NIGHT_MODE_AUTO) {
                Log.i("TAG", "updateNightMode: mode 1  :- $mode")
                prefs.night.set(mode == Preferences.NIGHT_MODE_ON)
                AppCompatDelegate.setDefaultNightMode(
                    when (mode) {
                        Preferences.NIGHT_MODE_OFF -> AppCompatDelegate.MODE_NIGHT_NO
                        Preferences.NIGHT_MODE_ON -> AppCompatDelegate.MODE_NIGHT_YES
                        Preferences.NIGHT_MODE_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        else -> AppCompatDelegate.MODE_NIGHT_NO
                    }
                )
                widgetManager.updateTheme()
            }
            updateAlarms()
        } catch (e: Exception) {
            Log.i("TAG", "updateNightMode: e :- " + e.message)
        }
    }

    fun setNightStart(hour: Int, minute: Int) {
        prefs.nightStart.set("$hour:$minute")
        updateAlarms()
    }

    fun setNightEnd(hour: Int, minute: Int) {
        prefs.nightEnd.set("$hour:$minute")
        updateAlarms()
    }

    private fun updateAlarms() {
        val dayCalendar = createCalendar(prefs.nightEnd.get())
        val day = Intent(context, NightModeReceiver::class.java)
        val dayIntent = PendingIntent.getBroadcast(
            context, 0, day,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nightCalendar = createCalendar(prefs.nightStart.get())
        val night = Intent(context, NightModeReceiver::class.java)
        val nightIntent = PendingIntent.getBroadcast(
            context, 1, night,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        context.sendBroadcast(day)
        context.sendBroadcast(night)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (prefs.nightMode.get() == Preferences.NIGHT_MODE_AUTO) {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                dayCalendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                dayIntent
            )
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                nightCalendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                nightIntent
            )
        } else {
            alarmManager.cancel(dayIntent)
            alarmManager.cancel(nightIntent)
        }
    }

    private fun createCalendar(time: String): Calendar {
        val calendar = parseTime(time)

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        }
    }

    /**
     * Parses the hour and minute out of the [time], which should be formatted h:mm
     */
    fun parseTime(time: String): Calendar {
        return tryOrNull {
            val parsedTime = SimpleDateFormat("H:mm", Locale.US).parse(time)
            Calendar.getInstance().apply { this.time = parsedTime }
        } ?: tryOrNull {
            // Parse the legacy timestamp format (<=3.1.3)
            val parsedTime = SimpleDateFormat("h:mm a", Locale.US).parse(time)
            Calendar.getInstance().apply { this.time = parsedTime }
        } ?: Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 18) }
    }

    /**
     * Returns a Calendar set to the most recent occurrence of this time
     */
    private fun getPreviousInstanceOfTime(time: String): Calendar {
        val currentTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)
        val calendar = createCalendar(time)

        while (calendar.timeInMillis > currentTime) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return calendar
    }

}