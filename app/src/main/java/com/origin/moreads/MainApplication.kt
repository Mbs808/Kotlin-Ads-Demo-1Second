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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        // Initialize the Google Mobile Ads SDK on a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(applicationContext) {}
        }

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

        var firebaseAnalytics: FirebaseAnalytics? = null

    }
}