package com.text.messages.sms.emoji.feature.main

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.text.messages.sms.emoji.common.PreferencesManager
import java.util.*

open class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            updateLanguage(this@BaseActivity)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(updateLanguage(newBase!!))
    }

    open fun updateLanguage(context: Context): Context? {
        var languageCode: String = PreferencesManager.getLanguage(context)!!
        val resources = context.resources
        val configuration = resources.configuration
        if (languageCode == null || languageCode.trim { it <= ' ' }.isEmpty()) {
            try {
                languageCode = configuration.locale.language
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        Log.d("TAG", "updateLanguage:1 " + languageCode)
        if (languageCode != null && !languageCode.trim { it <= ' ' }.isEmpty()) {
            languageCode = languageCode.toLowerCase()
            val locale = Locale(languageCode)
            //            Locale.setDefault(locale);
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                return context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            Log.d("TAG", "updateLanguage:2 " + languageCode)


        }
        return context
    }
}