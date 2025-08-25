package com.origin.moreads.ads.firebasegetdata

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.origin.moreads.MainApplication
import com.origin.moreads.ads.adsload.MoreAppDataLoader
import com.origin.moreads.ads.adsload.PreviewAdsLoad
import com.origin.moreads.ads.utils.AdsConstant
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

            updateAdConstants(remoteConfig)

            logAdValues()

            // Load more app data
            activityRef.get()?.let {
                if (shouldReloadMoreAppData(it)) reloadMoreAppData(it)
                loadLanguageScreenAds(it)
            }
        }
    }

    private fun updateAdConstants(config: FirebaseRemoteConfig) {
        fun get(key: String): String = config.getString(key).trim()

        AdsConstant.interstitialAds = get("interstitialAds")
        AdsConstant.nativeAds = get("nativeAds")
        AdsConstant.nativeBannerAds = get("nativeBannerAds")
        AdsConstant.bannerAds = get("bannerAds")
        AdsConstant.nativeLanguageAds = get("nativeLanguageAds")
        AdsConstant.nativeBannerLanguageAds = get("nativeBannerLanguageAds")
        AdsConstant.nativeExitDialogAds = get("nativeExitDialogAds")

        AdsConstant.showNativeShimmer = get("showNativeShimmer")
        AdsConstant.showNativeBannerShimmer130 = get("showNativeBannerShimmer130")
        AdsConstant.showNativeBannerShimmer100 = get("showNativeBannerShimmer100")
        AdsConstant.showNativeBannerShimmer80 = get("showNativeBannerShimmer80")
        AdsConstant.showNativeBannerShimmer60 = get("showNativeBannerShimmer60")
        AdsConstant.showBannerShimmer = get("showBannerShimmer")

        AdsConstant.playStoreLink = get("playStoreLink")
        AdsConstant.maxAdContentRating = get("maxAdContentRating")
        AdsConstant.showBigNativeLanguage = get("showBigNativeLanguage")
        AdsConstant.showLanguageNativeAd = get("showLanguageNativeAd")
        AdsConstant.showMoreAppLanguage = get("showMoreAppLanguage")
        AdsConstant.onlyShowMoreAppLanguage = get("onlyShowMoreAppLanguage")
        AdsConstant.showMoreAppNative = get("showMoreAppNative")
        AdsConstant.showMoreAppNativeBanner = get("showMoreAppNativeBanner")
        AdsConstant.showMoreAppBanner = get("showMoreAppBanner")
        AdsConstant.onlyShowMoreAppNative = get("onlyShowMoreAppNative")
        AdsConstant.onlyShowMoreAppNativeBanner = get("onlyShowMoreAppNativeBanner")
        AdsConstant.onlyShowMoreAppBanner = get("onlyShowMoreAppBanner")
        AdsConstant.showAdsExitDialog = get("showAdsExitDialog")

        AdsConstant.googleInterMaxInterAdsShow =
            get("googleInterMaxInterAdsShow").toIntOrNull() ?: 3
        AdsConstant.googleInterGapBetweenTwoInter =
            get("googleInterGapBetweenTwoInter").toIntOrNull() ?: 2
        AdsConstant.googleInterCountDownTimer =
            get("googleInterCountDownTimer").toLongOrNull() ?: 10000L
        AdsConstant.firstTime = get("firstTime").toBoolean()

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
        if (PreviewAdsLoad.isLoadingInLanguage) return
        if (AdsConstant.showLanguageNativeAd != "yes") return
        if (activity.prefsHelper.isLanguageSelected) return
        if (AdsConstant.onlyShowMoreAppLanguage == "yes") return

        val adUnit = if (AdsConstant.showBigNativeLanguage == "yes") {
            AdsConstant.nativeLanguageAds
        } else {
            AdsConstant.nativeBannerLanguageAds
        }

        PreviewAdsLoad.isLanguageLoadingInSplash = true
        PreviewAdsLoad.loadGoogleNativeAd(activity, adUnit) { nativeAd ->
            PreviewAdsLoad.languageUnifiedNativeAds?.destroy()
            PreviewAdsLoad.languageUnifiedNativeAds = nativeAd
            PreviewAdsLoad.isLanguageAdLoadingMutableLiveData.value = nativeAd != null
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
        Log.e(TAG, "MaxAdContentRating::: ${AdsConstant.maxAdContentRating}")
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

    }
}
