package com.app.research

import android.app.Application
import com.app.research.health_connect.HealthConnectManager

class ResearchApplication : Application() {
    
    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

}