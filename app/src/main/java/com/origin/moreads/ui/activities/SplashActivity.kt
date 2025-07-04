package com.origin.moreads.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.KeyEvent
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.adsload.GoogleInterstitialAds
import com.origin.moreads.ads.firebasegetdata.RemoteConfigManager
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.ads.utils.AdsUtils
import com.origin.moreads.ads.utils.AdsUtils.isConnected
import com.origin.moreads.extensions.hasAllPermissions
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.ui.activities.main.MainActivity
import com.origin.moreads.ui.activities.onboard.OnBoardingActivity
import com.origin.moreads.utils.PERMISSION_REQUEST_NOTIFICATION_LIST

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private var splashHandler: Handler? = null
    private var splashRunnable: Runnable? = null

    private var progressBarAnimation: ProgressBarAnimation? = null

    private var remoteConfigManager: RemoteConfigManager? = null

    private lateinit var consentInformation: ConsentInformation

    private val milliSec = 1000L

    private lateinit var tvLoadingState: TextView
    private lateinit var loadingProgressBar: ProgressBar

    companion object {
        private const val TAG = "SplashAct"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkLauncherRedundant()

        initializeViews()

        handleAdsStates()

        if (isConnected(this)) {
            requestConsentForm()
        } else {
            startTimer(milliSec)
        }

        Log.e("Ads_Demo", "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())

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
        AdsConstant.isSplashShowed = false
        GoogleInterstitialAds.originalAdsShow = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasAllPermissions(PERMISSION_REQUEST_NOTIFICATION_LIST)) {
                prefsHelper.rateUsDialogCounter++
            }
        } else {
            prefsHelper.rateUsDialogCounter++
        }

        if (prefsHelper.moreAppUrl.isNotEmpty() && prefsHelper.mreAppAccountName.isNotEmpty()) {
            remoteConfigManager?.loadMoreAppData()
        }
    }

    private fun initializeViews() {
        tvLoadingState = findViewById(R.id.tvLoadingState)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        progressBarAnimation = ProgressBarAnimation(loadingProgressBar, 0f, 360f).apply {
            duration = milliSec
        }
    }

    private fun requestConsentForm() {
        val debugSettings = ConsentDebugSettings.Builder(this)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("5382046924EDFB0B8FDDB4A016AC88A7")
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this, params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this,
                    ConsentForm.OnConsentFormDismissedListener {
                        startTimer(milliSec)
                    }
                )
            },
            {
                startTimer(milliSec)
            }
        )
    }

    private fun startTimer(duration: Long) {
        progressBarAnimation?.duration = duration
        loadingProgressBar.startAnimation(progressBarAnimation)

        if (AdsUtils.isConnected(this)) {
            remoteConfigManager = RemoteConfigManager(activity = this)
        }

        splashHandler = Handler(Looper.getMainLooper())
        splashRunnable = Runnable {
            startIntentToNextActivity()
        }
        splashHandler?.postDelayed(splashRunnable!!, duration)
    }

    private fun startIntentToNextActivity() {
        if (AdsConstant.isSplashShowed) return

        AdsConstant.isSplashShowed = true
        val nextActivity = when {
            !prefsHelper.isLanguageSelected -> ContinueActivity::class.java

            else -> MainActivity::class.java
        }
        startIntent(nextActivity)
        finish()
    }

    override fun onPause() {
        super.onPause()
        Log.e("Ads_Demo", "${TAG}_onPause")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onPause", Bundle())

        AdsConstant.pauseResume = true
        splashHandler?.removeCallbacks(splashRunnable ?: return)
    }

    override fun onResume() {
        super.onResume()
        Log.e("Ads_Demo", "${TAG}_onResume")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onResume", Bundle())

        if (AdsConstant.pauseResume) {
            AdsConstant.pauseResume = false
            splashHandler?.postDelayed(splashRunnable ?: return, 10)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Ads_Demo", "${TAG}_onDestroy")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onDestroy", Bundle())

        splashHandler?.removeCallbacksAndMessages(null)
        splashHandler = null
        splashRunnable = null
        progressBarAnimation?.cancel()
        progressBarAnimation = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e("Ads_Demo", "${TAG}_onBackPressed")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_onBackPressed", Bundle())
            finish()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    inner class ProgressBarAnimation(
        private val progressBar: ProgressBar,
        private val from: Float,
        private val to: Float
    ) : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            val value = from + (to - from) * interpolatedTime
            progressBar.progress = value.toInt()

            val status = when (value.toInt()) {
                in 50..99 -> getString(R.string.loading)
                in 100..199 -> getString(R.string.preparing)
                else -> getString(R.string.ready_now)
            }
            tvLoadingState.text = status
        }
    }

}

