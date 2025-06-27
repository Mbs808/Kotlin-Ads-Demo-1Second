package com.origin.moreads

import android.app.Application
import android.util.Log
import com.origin.moreads.extensions.prefsHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.yariksoffice.lingver.Lingver

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        MobileAds.initialize(applicationContext)

        val languageCode = prefsHelper.languageCode
        Lingver.init(this, languageCode)

        FirebaseApp.initializeApp(applicationContext)
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "${task.exception} Fetching FCM registration token failed")
                    return@OnCompleteListener
                }
                // Get new FCM registration token
            })



    }

    companion object {
        private const val TAG = "MainApplication"
        private const val ONE_SIGNAL_APP_ID = "dec1bfad-db7a-4265-91bd-ff3a0ac971e2"

        var firebaseAnalytics: FirebaseAnalytics? = null

    }


}