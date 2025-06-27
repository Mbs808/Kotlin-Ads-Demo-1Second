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

object GoogleInterstitialAds {

    private const val TAG = "GoogleInterAds"

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
        Log.e(TAG, "request_to_gInter_load")

        if (originalAdsShow == AdsConstant.googleInterMaxInterAdsShow) {
            Log.d(TAG, "return init")
            return
        }

        Log.d(TAG, "init")

        val loadCallback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                adError = false
                Log.e(TAG, "${TAG}_loaded")
                admobInterstitial = interstitialAd

                admobInterstitial?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {

                            startTimer()
                            Log.e(TAG, "${TAG}_dismissedFull")
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            admobInterstitial = null
                            GoogleInterstitialAds.adError = true
                            Log.e(TAG, "${TAG}_failToShowFull$adError")
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.e(TAG, "${TAG}_AdShowedFull")
                            AdsConstant.firstTime = true
                            originalAdsShow++

                            googleInterstitial(activity)
                        }
                    }

            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                admobInterstitial = null
                adError = true
                Log.e(TAG, "${TAG}_AdFailed$error")
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

        if (!AdsConstant.firstTime && !adsShowIntervalTime) {
            Log.e(TAG, "${TAG}_req_$from")
            if (admobInterstitial != null) {
                admobInterstitial?.show(activity)
                Log.e(TAG, "${TAG}_Show_$from")
                Log.e(TAG, "${TAG}_Total_Show")
            } else {
                Log.e(TAG, "${TAG}_First_ex_show")
                if (AdsUtils.isConnected(activity)) {
                    if (adError) {
                        Log.e(TAG, "${TAG}_show_$from")
                        googleInterstitial(activity)
                    }
                }
            }
        } else {
            adsClick++
            Log.d(TAG, "adsClick_$adsClick")
            if (admobInterstitial != null) {

                if (adsShowOrNot()) {
                    Log.e(TAG, "${TAG}_Show_else_$from")
                    admobInterstitial?.show(activity)
                }
            } else {
                if (adsShowOrNot()) {
                    Log.e(TAG, "${TAG}_req_ex_show_$from")
                }
                if (AdsUtils.isConnected(activity)) {
                    if (adError) {
                        Log.e(TAG, "${TAG}_req_second_load_$from")
                        googleInterstitial(activity)
                    }
                }
            }
        }
    }


    private fun adsShowOrNot(): Boolean {
        return if (!adsShowIntervalTime && adsClick > AdsConstant.googleInterGapBetweenTwoInter && originalAdsShow != AdsConstant.googleInterMaxInterAdsShow) {
            adsClick = 0
            Log.d(TAG, "${TAG}_adsShowOrNot: true")
            true
        } else {
            Log.d(TAG, "${TAG}__adsShowOrNot_false")
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