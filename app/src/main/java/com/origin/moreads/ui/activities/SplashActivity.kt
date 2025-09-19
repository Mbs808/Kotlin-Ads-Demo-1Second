package com.origin.moreads.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.UpdateDialogManager
import com.origin.moreads.ads.adsload.GoogleInterstitialAds
import com.origin.moreads.ads.adsload.MoreAppDataLoader
import com.origin.moreads.ads.adsload.PreviewLangAdsLoad
import com.origin.moreads.ads.firebasegetdata.RemoteConfigManager
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.databinding.ActivitySplashBinding
import com.origin.moreads.extensions.hasAllPermissions
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.ui.activities.main.MainActivity
import com.origin.moreads.utils.EventLog
import com.origin.moreads.utils.PERMISSION_REQUEST_NOTIFICATION_LIST

class SplashActivity : BaseActivity() {
    private val TAG = "SplashAct"
    private var splashHandler: Handler? = null
    private var splashRunnable: Runnable? = null
    private var remoteConfigManager: RemoteConfigManager? = null
    private val milliSec = 1000L
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkLauncherRedundant()

        if (AdsConstant.isConnected(this)) {
            if (prefsHelper.moreAppUrl.isNotEmpty() && prefsHelper.moreAppAccountName.isNotEmpty()) {
                MoreAppDataLoader.loadMoreAppData(
                    prefsHelper.moreAppUrl,
                    prefsHelper.moreAppAccountName
                )
            }
        }

        handleAdsStates()

        // Clear preload language ads
        PreviewLangAdsLoad.clearLanguageAd()

        startTimer(milliSec)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "SplashAct_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("SplashAct_onBackPressed", Bundle())

                finishAffinity()
            }
        })

        Log.e(EventLog, "SplashAct_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("SplashAct_onCreate", Bundle())
    }

    private fun checkLauncherRedundant() {
        if ((!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER))
            && intent.action == Intent.ACTION_MAIN
        ) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun handleAdsStates() {
        AdsConstant.pauseResume = false
        AdsConstant.isLoadedAdID = false
        AdsConstant.isIntentNext = false
        AdsConstant.isUpdateDialogShowed = false

        GoogleInterstitialAds.originalAdsShown = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasAllPermissions(PERMISSION_REQUEST_NOTIFICATION_LIST)) {
                prefsHelper.rateUsDialogCounter++
            }
        } else {
            prefsHelper.rateUsDialogCounter++
        }
    }

    private fun startTimer(duration: Long) {
        // preload language native ads
        if (AdsConstant.isConnected(this@SplashActivity) && !prefsHelper.isLanguageSelected && AdsConstant.showLanguageNativeAd == "yes") {
            PreviewLangAdsLoad.loadLanguageNativeAds(this)
        }

        if (AdsConstant.isConnected(this)) {
            remoteConfigManager = RemoteConfigManager(activity = this)
        }

        splashHandler = Handler(Looper.getMainLooper())

        splashRunnable = Runnable {
            if (AdsConstant.isConnected(this) && AdsConstant.updateNow == "yes") {
                val dialog = UpdateDialogManager.currentDialog
                if (dialog != null && dialog.isShowing) {
                    return@Runnable
                }
            }
            startIntentToNextActivity()
        }
        splashHandler?.postDelayed(splashRunnable!!, duration)
    }

    private fun startIntentToNextActivity() {
        if (!AdsConstant.isIntentNext) {
            AdsConstant.isIntentNext = true

            val nextActivity = when {
                !prefsHelper.isLanguageSelected -> ContinueActivity::class.java

                else -> MainActivity::class.java
            }

            startIntent(nextActivity)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "SplashAct_onPause")

        AdsConstant.pauseResume = true
        splashHandler?.removeCallbacks(splashRunnable ?: return)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "SplashAct_onResume")

        if (AdsConstant.pauseResume) {
            AdsConstant.pauseResume = false
            splashHandler?.postDelayed(splashRunnable ?: return, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "SplashAct_onDestroy")

        splashHandler?.removeCallbacksAndMessages(null)
        splashHandler = null
        splashRunnable = null
    }
}

