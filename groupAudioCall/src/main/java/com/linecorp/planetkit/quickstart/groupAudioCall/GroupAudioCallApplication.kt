package com.linecorp.planetkit.quickstart.groupAudioCall

import android.app.Application
import android.util.Log
import com.linecorp.planetkit.PlanetKit

class GroupAudioCallApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = PlanetKit.PlanetKitConfiguration.Builder(applicationContext)
                .setServerUrl(Constants.PLANET_CLOUD_URL)
                .build()

        PlanetKit.initialize(config) { isSuccessful, isVideoHwCodecSupport, userAgent ->
            Log.d("QuickStartApplication", "PlanetKit initialization(isSuccessful=$isSuccessful, " +
                    "isVideoHwCodecSupport=$isVideoHwCodecSupport, userAgent=$userAgent)")
        }
    }
}