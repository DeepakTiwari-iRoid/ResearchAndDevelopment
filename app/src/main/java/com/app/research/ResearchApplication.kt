package com.app.research

import android.app.Application
import com.app.research.health_connect.HealthConnectManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    override fun onCreate() {
        super.onCreate()
        instance = this
        locationClient = LocationServices.getFusedLocationProviderClient(this@ResearchApplication)
        Timber.plant(Timber.DebugTree())
    }

}