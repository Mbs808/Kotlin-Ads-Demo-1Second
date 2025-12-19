package com.origin.moreads.ads.firebasegetdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.origin.moreads.MainApplication
import com.origin.moreads.ads.adsload.MoreAppDataLoader
import com.origin.moreads.ads.adsload.PreviewLangAdsLoad
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.ads.utils.UpdateDialogAction
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.utils.EventLog
import java.lang.ref.WeakReference
import java.util.Locale

class RemoteConfigManager(activity: Activity) {

    companion object {
        private const val TAG = "firebase----"
        private const val TIMEOUT_SECONDS = 120
    }

    private val activityRef = WeakReference(activity)
    private val handler = Handler(Looper.getMainLooper())
    private var seconds = 0
    private var isRunning = false
    private var remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private val timeoutRunnable = object : Runnable {
        override fun run() {
            val time = String.format(Locale.getDefault(), "%02d", seconds % 60)
            Log.e(TAG, "remoteData_time_$time")

            if (seconds >= TIMEOUT_SECONDS) {
                Log.e(EventLog, "remoteData_Timeout_$seconds")
                MainApplication.firebaseAnalytics?.logEvent("remoteData_Timeout_$seconds", Bundle())
                stopTimer()
                return
            }

            seconds++
            if (isRunning) handler.postDelayed(this, 1000)
        }
    }

    init {
        Log.e(EventLog, "remoteData_load_start")
        MainApplication.firebaseAnalytics?.logEvent("remoteData_load_start", Bundle())

        startTimer()

        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
        )

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            stopTimer()

            if (!task.isSuccessful) {
                Log.e(EventLog, "remoteData_failed")
                MainApplication.firebaseAnalytics?.logEvent("remoteData_failed", Bundle())
                return@addOnCompleteListener
            }

            AdsConstant.isLoadedAdID = true

            Log.e(EventLog, "remoteData_load_success")
            MainApplication.firebaseAnalytics?.logEvent("remoteData_load_success", Bundle())

            /** Update ads constants from firebase **/
            updateAdConstants(remoteConfig)

            /** Log ads constants **/
            logAdValues()

        }
    }

    private fun updateAdConstants(config: FirebaseRemoteConfig) {
        fun get(key: String): String = config.getString(key).trim()

        val interstitialAds = get("interstitialAds")
        val nativeAds = get("nativeAds")
        val nativeBannerAds = get("nativeBannerAds")
        val bannerAds = get("bannerAds")
        val nativeLanguageAds = get("nativeLanguageAds")
        val nativeBannerLanguageAds = get("nativeBannerLanguageAds")
        val nativeExitDialogAds = get("nativeExitDialogAds")

        val showNativeShimmer = get("showNativeShimmer")
        val showNativeBannerShimmer130 = get("showNativeBannerShimmer130")
        val showNativeBannerShimmer100 = get("showNativeBannerShimmer100")
        val showNativeBannerShimmer80 = get("showNativeBannerShimmer80")
        val showNativeBannerShimmer60 = get("showNativeBannerShimmer60")
        val showBannerShimmer = get("showBannerShimmer")

        val playStoreLink = get("playStoreLink")
        val showBigNativeLanguage = get("showBigNativeLanguage")
        val showLanguageNativeAd = get("showLanguageNativeAd")
        val showMoreAppLanguage = get("showMoreAppLanguage")
        val onlyShowMoreAppLanguage = get("onlyShowMoreAppLanguage")
        val showMoreAppNative = get("showMoreAppNative")
        val showMoreAppNativeBanner = get("showMoreAppNativeBanner")
        val showMoreAppBanner = get("showMoreAppBanner")
        val onlyShowMoreAppNative = get("onlyShowMoreAppNative")
        val onlyShowMoreAppNativeBanner = get("onlyShowMoreAppNativeBanner")
        val onlyShowMoreAppBanner = get("onlyShowMoreAppBanner")
        val showAdsExitDialog = get("showAdsExitDialog")

        val updateNow = get("updateNow")

        val googleInterMaxInterAdsShow = get("googleInterMaxInterAdsShow")
        val googleInterGapBetweenTwoInter = get("googleInterGapBetweenTwoInter")
        val googleInterCountDownTimer = get("googleInterCountDownTimer")
        val firstTime = get("firstTime")

        // Assign only when non-empty
        if (interstitialAds.isNotEmpty()) AdsConstant.interstitialAds = interstitialAds
        if (nativeAds.isNotEmpty()) AdsConstant.nativeAds = nativeAds
        if (nativeBannerAds.isNotEmpty()) AdsConstant.nativeBannerAds = nativeBannerAds
        if (bannerAds.isNotEmpty()) AdsConstant.bannerAds = bannerAds
        if (nativeLanguageAds.isNotEmpty()) AdsConstant.nativeLanguageAds = nativeLanguageAds
        if (nativeBannerLanguageAds.isNotEmpty()) AdsConstant.nativeBannerLanguageAds = nativeBannerLanguageAds
        if (nativeExitDialogAds.isNotEmpty()) AdsConstant.nativeExitDialogAds = nativeExitDialogAds

        if (showNativeShimmer.isNotEmpty()) AdsConstant.showNativeShimmer = showNativeShimmer
        if (showNativeBannerShimmer130.isNotEmpty()) AdsConstant.showNativeBannerShimmer130 = showNativeBannerShimmer130
        if (showNativeBannerShimmer100.isNotEmpty()) AdsConstant.showNativeBannerShimmer100 = showNativeBannerShimmer100
        if (showNativeBannerShimmer80.isNotEmpty()) AdsConstant.showNativeBannerShimmer80 = showNativeBannerShimmer80
        if (showNativeBannerShimmer60.isNotEmpty()) AdsConstant.showNativeBannerShimmer60 = showNativeBannerShimmer60
        if (showBannerShimmer.isNotEmpty()) AdsConstant.showBannerShimmer = showBannerShimmer

        if (playStoreLink.isNotEmpty()) AdsConstant.playStoreLink = playStoreLink
        if (showBigNativeLanguage.isNotEmpty()) AdsConstant.showBigNativeLanguage = showBigNativeLanguage
        if (showLanguageNativeAd.isNotEmpty()) AdsConstant.showLanguageNativeAd = showLanguageNativeAd
        if (showMoreAppLanguage.isNotEmpty()) AdsConstant.showMoreAppLanguage = showMoreAppLanguage
        if (onlyShowMoreAppLanguage.isNotEmpty()) AdsConstant.onlyShowMoreAppLanguage = onlyShowMoreAppLanguage
        if (showMoreAppNative.isNotEmpty()) AdsConstant.showMoreAppNative = showMoreAppNative
        if (showMoreAppNativeBanner.isNotEmpty()) AdsConstant.showMoreAppNativeBanner = showMoreAppNativeBanner
        if (showMoreAppBanner.isNotEmpty()) AdsConstant.showMoreAppBanner = showMoreAppBanner
        if (onlyShowMoreAppNative.isNotEmpty()) AdsConstant.onlyShowMoreAppNative = onlyShowMoreAppNative
        if (onlyShowMoreAppNativeBanner.isNotEmpty()) AdsConstant.onlyShowMoreAppNativeBanner = onlyShowMoreAppNativeBanner
        if (onlyShowMoreAppBanner.isNotEmpty()) AdsConstant.onlyShowMoreAppBanner = onlyShowMoreAppBanner
        if (showAdsExitDialog.isNotEmpty()) AdsConstant.showAdsExitDialog = showAdsExitDialog

        if (updateNow.isNotEmpty()) AdsConstant.updateNow = updateNow

        if (googleInterMaxInterAdsShow.isNotEmpty()) {
            AdsConstant.googleInterMaxInterAdsShow = googleInterMaxInterAdsShow.toIntOrNull() ?: 3
        }

        if (googleInterGapBetweenTwoInter.isNotEmpty()) {
            AdsConstant.googleInterGapBetweenTwoInter = googleInterGapBetweenTwoInter.toIntOrNull() ?: 2
        }

        if (googleInterCountDownTimer.isNotEmpty()) {
            AdsConstant.googleInterCountDownTimer = googleInterCountDownTimer.toLongOrNull() ?: 10000L
        }

        if (firstTime.isNotEmpty()) AdsConstant.firstTime = firstTime.toBoolean()

        // SharedPreferences updates
        activityRef.get()?.prefsHelper?.apply {
            get("moreAppUrl").let {
                if (it.isNotEmpty() && it != moreAppUrl) moreAppUrl = it
                AdsConstant.moreAppUrl = it
            }

            get("moreAppAccountName").let {
                if (it.isNotEmpty() && it != moreAppAccountName) moreAppAccountName = it
                AdsConstant.moreAccountName = it
            }
        }

        /** Trigger update dialog if needed **/
        if (AdsConstant.updateNow == "yes") {
            activityRef.get()?.sendBroadcast(Intent(UpdateDialogAction.SHOW_UPDATE_DIALOG))
        }

        /** Load more app data if not loaded from splash **/
        activityRef.get()?.let {
            if (shouldReloadMoreAppData(it)) reloadMoreAppData(it)
        }

        /** preload language native ads **/
//        activityRef.get()?.let {
//            loadLanguageScreenAds(it)
//        }

    }

    private fun shouldReloadMoreAppData(activity: Activity): Boolean {
        return AdsConstant.isConnected(activity) && MoreAppDataLoader.didLastLoadFail && !MoreAppDataLoader.isLoading
    }

    private fun reloadMoreAppData(activity: Activity) {
        val url = activity.prefsHelper.moreAppUrl
        val account = activity.prefsHelper.moreAppAccountName
        if (url.isNotEmpty() && account.isNotEmpty()) {
            MoreAppDataLoader.loadMoreAppData(url, account)
        }
    }

    private fun loadLanguageScreenAds(activity: Activity) {
        if (AdsConstant.isConnected(activity) && !activity.prefsHelper.isLanguageSelected && AdsConstant.showLanguageNativeAd == "yes") {
            PreviewLangAdsLoad.loadLanguageNativeAds(activity)
        }
    }

    private fun startTimer() {
        isRunning = true
        seconds = 0
        handler.post(timeoutRunnable)
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(timeoutRunnable)
    }

    private fun logAdValues() {
        Log.e(TAG, "Interstitial::: ${AdsConstant.interstitialAds}")
        Log.e(TAG, "Native::: ${AdsConstant.nativeAds}")
        Log.e(TAG, "NativeBanner::: ${AdsConstant.nativeBannerAds}")
        Log.e(TAG, "Banner::: ${AdsConstant.bannerAds}")
        Log.e(TAG, "NativeLanguage::: ${AdsConstant.nativeLanguageAds}")
        Log.e(TAG, "NativeBannerLanguage::: ${AdsConstant.nativeBannerLanguageAds}")
        Log.e(TAG, "NativeExitDialog::: ${AdsConstant.nativeExitDialogAds}")

        Log.e(TAG, "ShowNativeShimmer::: ${AdsConstant.showNativeShimmer}")
        Log.e(TAG, "ShowNativeBannerShimmer130::: ${AdsConstant.showNativeBannerShimmer130}")
        Log.e(TAG, "ShowNativeBannerShimmer100::: ${AdsConstant.showNativeBannerShimmer100}")
        Log.e(TAG, "ShowNativeBannerShimmer80::: ${AdsConstant.showNativeBannerShimmer80}")
        Log.e(TAG, "ShowNativeBannerShimmer60::: ${AdsConstant.showNativeBannerShimmer60}")
        Log.e(TAG, "ShowBannerShimmer::: ${AdsConstant.showBannerShimmer}")

        Log.e(TAG, "PlayStoreLink::: ${AdsConstant.playStoreLink}")
        Log.e(TAG, "ShowBigNativeLanguage::: ${AdsConstant.showBigNativeLanguage}")
        Log.e(TAG, "ShowLanguageNativeAd::: ${AdsConstant.showLanguageNativeAd}")
        Log.e(TAG, "ShowMoreAppLanguage::: ${AdsConstant.showMoreAppLanguage}")
        Log.e(TAG, "OnlyShowMoreAppLanguage::: ${AdsConstant.onlyShowMoreAppLanguage}")
        Log.e(TAG, "ShowMoreAppNative::: ${AdsConstant.showMoreAppNative}")
        Log.e(TAG, "ShowMoreAppNativeBanner::: ${AdsConstant.showMoreAppNativeBanner}")
        Log.e(TAG, "ShowMoreAppBanner::: ${AdsConstant.showMoreAppBanner}")
        Log.e(TAG, "OnlyShowMoreAppNative::: ${AdsConstant.onlyShowMoreAppNative}")
        Log.e(TAG, "OnlyShowMoreAppNativeBanner::: ${AdsConstant.onlyShowMoreAppNativeBanner}")
        Log.e(TAG, "OnlyShowMoreAppBanner::: ${AdsConstant.onlyShowMoreAppBanner}")
        Log.e(TAG, "ShowAdsExitDialog::: ${AdsConstant.showAdsExitDialog}")

        Log.e(TAG, "GoogleInterMaxInterAdsShow::: ${AdsConstant.googleInterMaxInterAdsShow}")
        Log.e(TAG, "GoogleInterGapBetweenTwoInter::: ${AdsConstant.googleInterGapBetweenTwoInter}")
        Log.e(TAG, "GoogleInterCountDownTimer::: ${AdsConstant.googleInterCountDownTimer}")
        Log.e(TAG, "FirstTime::: ${AdsConstant.firstTime}")

        Log.e(TAG, "MoreAppUrl::: ${AdsConstant.moreAppUrl}")
        Log.e(TAG, "MoreAccountName::: ${AdsConstant.moreAccountName}")

        Log.e(TAG, "updateNow::: ${AdsConstant.updateNow}")

    }
}
