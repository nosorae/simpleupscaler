package com.yessorae.simpleupscaler

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SimpleUpscaleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initMobileAdmob()
    }

    private fun initMobileAdmob() {
        MobileAds.initialize(this)
    }
}
