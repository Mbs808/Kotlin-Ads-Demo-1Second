package com.origin.moreads.ads.adsload

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.origin.moreads.MainApplication
import com.origin.moreads.ads.utils.AdsConstant
import java.util.Date

class AppOpenManager(private val mainApplication: MainApplication) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private val TAG = "AppOpenAds"

    companion object {
        var appOpenAd: AppOpenAd? = null

        var isShowingOpenAds = false
    }

    private var loadTime: Long = 0
    private lateinit var loadCallback: AppOpenAd.AppOpenAdLoadCallback
    private var currentActivity: Activity? = null

    init {
        Log.e(TAG, "AppOpenAds_init")

        mainApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onActivityCreated(activity: Activity, budnle: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        Log.e(TAG, "AppOpenAds_onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        Log.e(TAG, "AppOpenAds_onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean =
        appOpenAd != null && wasLoadTimeLessThanNHoursAgo()

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4
    }

    // fetch openAds
    fun fetchAd(adID: String) {
        Log.e(TAG, "AppOpenAds_isAdAvailable_::${isAdAvailable()}")

        if (isAdAvailable()) {
            return
        }

        Log.e(TAG, "AppOpenAds_load_start")

        Log.e(TAG, "AppOpenAds_id__::: $adID")

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.e(TAG, "AppOpenAds_onAdLoaded")

                appOpenAd = ad
                loadTime = Date().time
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "AppOpenAds_failed_load$error")
            }
        }

        val request = getAdRequest()
        AppOpenAd.load(
            mainApplication,
            adID,
            request,
            loadCallback
        )
    }

    override fun onStart(owner: LifecycleOwner) {
        // Show the ad (if available) when the app moves to foreground.
        Handler(Looper.getMainLooper()).postDelayed({
            showAdIfAvailable()
        },200)
    }

    private fun showAdIfAvailable() {
        Log.e(TAG, "AppOpenAds_showAdIfAvailable")

        Log.e(TAG, "AppOpenAds -----isShowingAd---:::$isShowingOpenAds")
        Log.e(TAG, "AppOpenAds -----isInterAdsShowed---:::${AdsConstant.isInterAdsShowed}")
        Log.e(TAG, "AppOpenAds -----isAdAvailable---:::${isAdAvailable()}")

        if (AdsConstant.isInterAdsShowed){
            Log.e(TAG, "AppOpenAds_inter_showed_return")
            return
        }

        if (AdsConstant.isPermissionDialogShowed){
            Log.e(TAG, "AppOpenAds_isPermissionDialog_showed")
            return
        }

        if (!isShowingOpenAds && isAdAvailable()) {
            Log.e(TAG, "AppOpenAds_showAdIfAvailable::::11::::")
            isShowingOpenAds = true

            val fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.e(TAG, "AppOpenAds_ad_dismiss")

                    isShowingOpenAds = false
                    appOpenAd = null

                    fetchAd(AdsConstant.AppOpenAds)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "AppOpenAds_failed_to_show$error")
                    isShowingOpenAds = false
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingOpenAds = true
                    Log.e(TAG, "AppOpenAds_show_full")
                }
            }

            currentActivity?.let {
                Log.e(TAG, "simpleName: ${it.javaClass.simpleName}")
                appOpenAd?.run {
                    this.fullScreenContentCallback = fullScreenContentCallback
                    show(it)
                }
            }
        } else {
            Log.e(TAG, "AppOpenAds_reload_ad")

            isShowingOpenAds = false

            fetchAd(AdsConstant.AppOpenAds)
        }
    }
}