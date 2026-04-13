package com.app.research

import android.app.Application
import com.app.research.health_connect.HealthConnectManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.uber.h3core.H3Core
import timber.log.Timber

class ResearchApplication : Application() {

    companion object {

        lateinit var instance: ResearchApplication
            private set
    }

    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

    lateinit var locationClient: FusedLocationProviderClient
        private set

    lateinit var h3: H3Core
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        locationClient = LocationServices.getFusedLocationProviderClient(this@ResearchApplication)
        h3 = H3Core.newSystemInstance()

        Timber.plant(Timber.DebugTree())
    }

}