package com.text.messages.sms.emoji.feature.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.ads.AdvertiseHandler
import com.text.messages.sms.emoji.ads.AppUtils
import com.text.messages.sms.emoji.common.PreferencesManager
import com.text.messages.sms.emoji.common.base.MessagesThemedActivity
import com.text.messages.sms.emoji.common.widget.MessagesTextView
import dagger.android.AndroidInjection

class GetStartActivity : MessagesThemedActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {

    private var btn_get_start: AppCompatButton? = null
    private var chkByClick: AppCompatCheckBox? = null
    private var txt_start_terms: MessagesTextView? = null
    private var txt_start_privacy: MessagesTextView? = null
    private var isMain: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_start)
        init()
        UIComponent()

    }

    private fun init() {
        /*advertiseHandler = AdvertiseHandler.getInstance()
        advertiseHandler.isAppStartUpAdsEnabled = true*/
        if (intent.extras != null) {
            isMain = intent.extras!!.getBoolean("isMain", false)
        }
        advertiseHandler = AdvertiseHandler.getInstance()
    }

    private fun UIComponent() {

        chkByClick = findViewById(R.id.chkByClick)
        chkByClick!!.setOnCheckedChangeListener(this)
        txt_start_privacy = findViewById(R.id.txt_start_privacy)
        btn_get_start = findViewById(R.id.btn_get_start)
        btn_get_start = findViewById(R.id.btn_get_start)
        btn_get_start!!.setBackgroundResource(R.drawable.rounded_corner_grey_bg)
        btn_get_start!!.setTextColor(ContextCompat.getColor(this, R.color.color_grey_dark))
        btn_get_start!!.setEnabled(false)
        txt_start_privacy!!.setOnClickListener(this)
        btn_get_start!!.setOnClickListener(this)

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        btn_get_start!!.setEnabled(isChecked)
        btn_get_start!!.setBackgroundResource(if (isChecked) R.drawable.ads_rounded_corner_bg else R.drawable.rounded_corner_grey_bg)
        btn_get_start!!.setTextColor(
            ContextCompat.getColor(
                this,
                if (isChecked) R.color.white else R.color.color_grey_dark
            )
        )
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_get_start ->
                onClickStart()
            R.id.txt_start_privacy ->
                AppUtils.openPrivacyPolicy(this)

        }
    }

    private fun onClickStart() {
        PreferencesManager.setStart(this, true)
        /*startActivity(Intent(this@GetStartActivity, SplashScreen::class.java))
        finish()*/
        if (!AppUtils.isEmptyString(PreferencesManager.getLanguage(this))) {
            startActivity(Intent(this@GetStartActivity, MainActivity::class.java))
            finish()
        } else {
            startActivity(
                Intent(
                    this@GetStartActivity,
                    LanguageActivity::class.java
                ).putExtra("isMain", false)
            )
            finish()
        }
    }

}