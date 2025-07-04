package com.origin.moreads.ads.adsload

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.ads.utils.AdsUtils
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.origin.moreads.MainApplication
import com.origin.moreads.ui.activities.ContinueActivity
import com.origin.moreads.ui.activities.ContinueActivity.Companion

object GoogleInterstitialAds {

    private const val TAG = "InterAds"

    var admobInterstitial: InterstitialAd? = null
    var originalAdsShow = 0
    var adError = false
    var adsShowIntervalTime = false
    private var adsClick = 0

    private fun getAdRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
    }

    fun googleInterstitial(activity: Activity) {

        AdsConstant.isSplashInterCall = true

        Log.e("Ads_Demo", "${TAG}_request_load")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_request_load", Bundle())

        if (originalAdsShow == AdsConstant.googleInterMaxInterAdsShow) {
            Log.d(TAG, "return init")
            return
        }


        Log.e("Ads_Demo", "${TAG}_init")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_init", Bundle())

        val loadCallback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.e("Ads_Demo", "${TAG}_onAdLoaded")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_onAdLoaded", Bundle())

                adError = false
                admobInterstitial = interstitialAd

                admobInterstitial?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.e("Ads_Demo", "${TAG}_AdDismissedFull")
                            MainApplication.firebaseAnalytics?.logEvent("${TAG}_AdDismissedFull", Bundle())

                            startTimer()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("Ads_Demo", "${TAG}_AdFailedToShow")
                            MainApplication.firebaseAnalytics?.logEvent("${TAG}_AdFailedToShow", Bundle())

                            admobInterstitial = null
                            GoogleInterstitialAds.adError = true
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.e("Ads_Demo", "${TAG}_AdShowedFull")
                            MainApplication.firebaseAnalytics?.logEvent("${TAG}_AdShowedFull", Bundle())

                            AdsConstant.firstTime = true
                            originalAdsShow++

                            googleInterstitial(activity)
                        }
                    }

            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("Ads_Demo", "${TAG}_AdFailedToLoad$error")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_AdFailedToLoad", Bundle())

                admobInterstitial = null
                adError = true
            }
        }

        val request = getAdRequest()
        InterstitialAd.load(
            activity,
            AdsConstant.interstitialAds,
            request,
            loadCallback
        )
    }

    fun googleInterstitialShow(activity: Activity, from: String) {
        Log.d(TAG, "req")
        Log.e(TAG, "originalAdsShow$originalAdsShow")
        Log.e(TAG, "googleInterMaxInterAdsShow_${AdsConstant.googleInterMaxInterAdsShow}")
        if (originalAdsShow == AdsConstant.googleInterMaxInterAdsShow) return

        Log.e("Ads_Demo", "${TAG}_req_show_$from")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_req_show_$from", Bundle())

        if (!AdsConstant.firstTime && !adsShowIntervalTime) {

            if (admobInterstitial != null) {
                Log.e("Ads_Demo", "${TAG}_show_$from")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_show_$from", Bundle())

                admobInterstitial?.show(activity)
            } else {

                Log.e("Ads_Demo", "${TAG}_First_ex_show")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_First_ex_show", Bundle())

                if (AdsUtils.isConnected(activity)) {
                    if (adError) {
                        Log.e("Ads_Demo", "${TAG}_error_load_again")
                        MainApplication.firebaseAnalytics?.logEvent("${TAG}_error_load_again", Bundle())

                        googleInterstitial(activity)
                    }
                }
            }
        } else {
            adsClick++
            Log.d(TAG, "adsClick_$adsClick")

            if (admobInterstitial != null) {

                if (adsShowOrNot()) {
                    Log.e("Ads_Demo", "${TAG}_Show_else_$from")
                    MainApplication.firebaseAnalytics?.logEvent("${TAG}_Show_else_$from", Bundle())

                    admobInterstitial?.show(activity)
                }
            } else {
                if (adsShowOrNot()) {
                    Log.e(TAG, "${TAG}_req_ex_show_$from")
                }
                if (AdsUtils.isConnected(activity)) {
                    if (adError) {
                        Log.e("Ads_Demo", "${TAG}_req_second_load_$from")
                        MainApplication.firebaseAnalytics?.logEvent("${TAG}_req_second_load_$from", Bundle())
                        googleInterstitial(activity)
                    }
                }
            }
        }
    }


    private fun adsShowOrNot(): Boolean {
        return if (!adsShowIntervalTime && adsClick > AdsConstant.googleInterGapBetweenTwoInter && originalAdsShow != AdsConstant.googleInterMaxInterAdsShow) {
            adsClick = 0

            Log.e("Ads_Demo", "${TAG}_adsShowOrNot_true")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_adsShowOrNot_true", Bundle())

            true
        } else {
            Log.e("Ads_Demo", "${TAG}_adsShowOrNot_false")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_adsShowOrNot_false", Bundle())

            false
        }
    }

    fun startTimer() {
        Log.d(TAG, "startTimer")
        adsShowIntervalTime = true
        object : CountDownTimer(AdsConstant.googleInterCountDownTimer, 1000) {
            override fun onTick(milliSec: Long) {
                Log.d(TAG, "timer_${(milliSec / 1000)}")
            }

            override fun onFinish() {
                Log.d(TAG, "timerStop")
                adsShowIntervalTime = false
            }
        }.start()
    }

}