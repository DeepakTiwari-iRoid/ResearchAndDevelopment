package com.app.researchanddevelopment

import android.app.Application

class ResearchAndDevelopmentApplication : Application() {

    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }


}