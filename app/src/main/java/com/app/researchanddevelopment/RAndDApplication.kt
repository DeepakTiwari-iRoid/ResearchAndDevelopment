package com.app.researchanddevelopment

import android.app.Application
import com.app.researchanddevelopment.health_connect.HealthConnectManager

class RAndDApplication : Application() {
    
    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

}