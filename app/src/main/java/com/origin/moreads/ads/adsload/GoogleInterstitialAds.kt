package com.origin.moreads.ads.adsload

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.origin.moreads.MainApplication
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.utils.EventLog
import java.lang.ref.WeakReference

object GoogleInterstitialAds {
    private const val TAG = "GoogleInter"

    private var interstitialAd: InterstitialAd? = null
    private var weakActivityRef: WeakReference<Activity>? = null

    var originalAdsShown = 0
    private var adError = false
    private var adsClick = 0
    private var isTimerRunning = false

    private fun getAdRequest(): AdRequest {
        val extras = Bundle().apply {
            putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        }
        return AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
    }

    fun loadInterstitial(activity: Activity) {

        if (originalAdsShown >= AdsConstant.googleInterMaxInterAdsShow) {
            Log.e(TAG, "Max interstitial ads shown.")
            return
        }

        AdsConstant.isSplashInterCall = true
        weakActivityRef = WeakReference(activity)

        logEvent("InterAds_request_load")

        InterstitialAd.load(
            activity,
            AdsConstant.interstitialAds,
            getAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    logEvent("InterAds_onAdLoaded")
                    interstitialAd = ad
                    adError = false

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            logEvent("InterAds_AdShowedFull")
                            AdsConstant.firstTime = true
                            originalAdsShown++
                            loadInterstitial(activity) // Preload next
                        }

                        override fun onAdDismissedFullScreenContent() {
                            logEvent("InterAds_AdDismissedFull")
                            startCooldownTimer()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            logEvent("InterAds_AdFailedToShow")
                            interstitialAd = null
                            adError = true
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(EventLog, "InterAds_AdFailedToLoad_${error.message}")
                    MainApplication.firebaseAnalytics?.logEvent("InterAds_AdFailedToLoad", Bundle())

                    interstitialAd = null
                    adError = true
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, from: String) {
        weakActivityRef = WeakReference(activity)

        if (activity.prefsHelper.isInterShow || originalAdsShown >= AdsConstant.googleInterMaxInterAdsShow) {
            Log.e(TAG, "showInterstitial ::--- show max---")
            return
        }

        Log.e(EventLog, "InterAds_req_show_${from}")
        MainApplication.firebaseAnalytics?.logEvent("InterAds_req_show", Bundle())

        if (!AdsConstant.firstTime && !isTimerRunning) {
            interstitialAd?.let {
                Log.e(EventLog, "InterAds_show_${from}")
                MainApplication.firebaseAnalytics?.logEvent("InterAds_show", Bundle())

                it.show(activity)
            } ?: run {
                logEvent("InterAds_First_ex_show")
                if (AdsConstant.isConnected(activity) && adError) {
                    logEvent("InterAds_error_load_again")
                    loadInterstitial(activity)
                }
            }
        } else {
            adsClick++
            Log.e(TAG, "adsClick = $adsClick")

            if (shouldShowAd()) {
                interstitialAd?.let {
                    Log.e(EventLog, "InterAds_Show_else_${from}")
                    MainApplication.firebaseAnalytics?.logEvent("InterAds_Show_else", Bundle())

                    it.show(activity)
                } ?: run {
                    Log.e(EventLog, "InterAds_req_ex_show_${from}")
                    MainApplication.firebaseAnalytics?.logEvent("InterAds_req_ex_show", Bundle())

                    if (AdsConstant.isConnected(activity) && adError) {
                        Log.e(EventLog, "InterAds_req_second_load_${from}")
                        MainApplication.firebaseAnalytics?.logEvent("InterAds_req_second_load", Bundle())

                        loadInterstitial(activity)
                    }
                }
            }
        }
    }

    private fun shouldShowAd(): Boolean {
        return if (!isTimerRunning && adsClick > AdsConstant.googleInterGapBetweenTwoInter && originalAdsShown < AdsConstant.googleInterMaxInterAdsShow) {
            adsClick = 0
            logEvent("InterAds_adsShowOrNot_true")
            true
        } else {
            logEvent("InterAds_adsShowOrNot_false")
            false
        }
    }

    private fun startCooldownTimer() {
        Log.e(TAG, "Starting cooldown timer...")
        isTimerRunning = true

        object : CountDownTimer(AdsConstant.googleInterCountDownTimer, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.e(TAG, "Cooldown: ${millisUntilFinished / 1000}s remaining")
            }

            override fun onFinish() {
                isTimerRunning = false
                Log.e(TAG, "Cooldown timer finished")
            }
        }.start()
    }

    private fun logEvent(event: String) {
        Log.e(EventLog, event)
        MainApplication.firebaseAnalytics?.logEvent(event, Bundle())
    }

}