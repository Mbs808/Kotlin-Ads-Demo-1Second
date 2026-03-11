package com.origin.moreads

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.origin.moreads.ads.receiver.ScreenStateReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the Google Mobile Ads SDK on a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(applicationContext) {
                }
            } catch (e: Exception) {
                Log.e(TAG, "onCreate:::--MobileAds not initialize -${e.message} " )
            }
        }

        FirebaseApp.initializeApp(applicationContext)
        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "${task.exception} Fetching FCM registration token failed")
                    return@OnCompleteListener
                }
                // Get new FCM registration token
            })

        // Register the BroadcastReceiver
        val screenStateReceiver = ScreenStateReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenStateReceiver, intentFilter)

    }

    companion object {
        private const val TAG = "MainApplication"

        var firebaseAnalytics: FirebaseAnalytics? = null
    }
}