/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.text.messages.sms.emoji.common

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.res.Configuration
import androidx.core.provider.FontRequest
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import androidx.multidex.MultiDexApplication
import com.chating.messages.common.session.SessionManager
import com.jksol.appmodule.ads.AppOpenManagerNew
import com.jksol.appmodule.utils.AppMetricaInit
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.ads.AppUtils
import com.text.messages.sms.emoji.common.util.FileLoggingTree
import com.text.messages.sms.emoji.injection.AppComponentManager
import com.text.messages.sms.emoji.injection.appComponent
import com.text.messages.sms.emoji.manager.AnalyticsManager
import com.text.messages.sms.emoji.manager.BillingManager
import com.text.messages.sms.emoji.manager.ReferralManager
import com.text.messages.sms.emoji.migration.QkMigration
import com.text.messages.sms.emoji.migration.QkRealmMigration
import com.text.messages.sms.emoji.util.CrashlyticsTree
import com.text.messages.sms.emoji.util.NightModeManager
import com.uber.rxdogtag.RxDogTag
import com.uber.rxdogtag.autodispose.AutoDisposeConfigurer
import dagger.android.*
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MessagesApplication : MultiDexApplication(), HasActivityInjector, HasBroadcastReceiverInjector,
    HasServiceInjector {

    /**
     * Inject these so that they are forced to initialize
     */
    @Suppress("unused")
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Suppress("unused")
    @Inject
    lateinit var qkMigration: QkMigration

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingBroadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var fileLoggingTree: FileLoggingTree

    @Inject
    lateinit var nightModeManager: NightModeManager

    @Inject
    lateinit var realmMigration: QkRealmMigration

    @Inject
    lateinit var referralManager: ReferralManager

    val ONESIGNAL_APP_ID = "d07c2273-fbc6-4b37-9a60-71f35db8ca09"

    override fun onCreate() {
        super.onCreate()


        SessionManager.getInstance(applicationContext)

        AppMetricaInit(this, "46050aa4-c4a2-47e4-83a7-a09c2364c827")
        AppComponentManager.init(this)
        appComponent.inject(this)
        AppOpenManagerNew(this,  resources.getString(R.string.admob_app_start_ads_id))


        Realm.init(applicationContext)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .compactOnLaunch()
                .migration(realmMigration)
                .schemaVersion(QkRealmMigration.SchemaVersion)
                .allowWritesOnUiThread(true)
                .build()
        )

        qkMigration.performMigration()

        nightModeManager.updateCurrentTheme()

        try {
            val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs
            )
            EmojiCompat.init(FontRequestEmojiCompatConfig(applicationContext, fontRequest))
        } catch (ignore: Exception) {
        }

        Timber.plant(Timber.DebugTree(), CrashlyticsTree(), fileLoggingTree)

        try {
            RxDogTag.builder()
                .configureWith(AutoDisposeConfigurer::configure)
                .install()
        } catch (ignore: Exception) {
        }

        /*OneSignal.initWithContext(applicationContext)
        OneSignal.setAppId(ONESIGNAL_APP_ID)*/

        /*val config: YandexMetricaConfig =
            YandexMetricaConfig.newConfigBuilder("cec41e69-e497-46a5-93ae-f75743ade113").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)*/

        GlobalScope.launch(Dispatchers.IO) {
            referralManager.trackReferrer()
            billingManager.checkForPurchases()
            billingManager.queryProducts()
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return dispatchingBroadcastReceiverInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        var lang: String = PreferencesManager.getLanguage(this)
        if (!AppUtils.isEmptyString(lang)) {
            AppUtils.setLocale(this, lang)
        } else {
            AppUtils.setLocale(this, "eng")
        }

        super.onConfigurationChanged(newConfig)
    }

    override fun attachBaseContext(base: Context?) {
        var lang: String = PreferencesManager.getLanguage(base)
        if (!AppUtils.isEmptyString(lang)) {
            AppUtils.setLocale(base, lang)
        } else {
            AppUtils.setLocale(base, "eng")
        }
        super.attachBaseContext(base)
    }
}