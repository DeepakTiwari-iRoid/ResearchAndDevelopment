package com.app.research

import android.app.Application
import com.app.research.health_connect.HealthConnectManager
import timber.log.Timber

class ResearchApplication : Application() {

    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

}