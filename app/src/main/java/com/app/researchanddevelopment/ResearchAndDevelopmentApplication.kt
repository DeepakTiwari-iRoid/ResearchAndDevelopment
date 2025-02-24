package com.app.researchanddevelopment

import android.app.Application
import com.app.researchanddevelopment.wearables.HealthConnectManager

class ResearchAndDevelopmentApplication : Application() {
    
    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

}