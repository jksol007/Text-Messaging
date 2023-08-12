package com.jksol.appmodule.utils

import android.app.Application
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig

class AppMetricaInit(var context: Application, appMetricaId: String) {

    init {
        Thread {
            val config =
                YandexMetricaConfig.newConfigBuilder(appMetricaId)
                    .build()
            YandexMetrica.activate(context, config)
            YandexMetrica.enableActivityAutoTracking(context)
        }.start()
    }
}