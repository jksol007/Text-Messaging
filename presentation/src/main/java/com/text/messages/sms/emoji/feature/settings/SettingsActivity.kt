package com.text.messages.sms.emoji.feature.settings

import android.content.Intent
import android.os.Bundle
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.base.MessagesThemedActivity
import com.text.messages.sms.emoji.feature.main.MainActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.container_activity.*
import kotlinx.android.synthetic.main.settings_controller.bannerAdLayout

class SettingsActivity : MessagesThemedActivity() {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(SettingsController()))
        }
        loadBottomBannerAdd()
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
//            super.onBackPressed()
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun loadBottomBannerAdd() {
        /*advertiseHandler.showNativeAds(
            this@SettingsActivity,
            bannerAdLayout,
            false,
            R.layout.native_ads_admob, null
        )*/
        advertiseHandler.loadBannerAds(this@SettingsActivity,
            bannerAdLayout,null)
    }

}