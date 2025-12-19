package com.origin.moreads.ads.adsload

import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.origin.moreads.ads.utils.AdsConstant

object GoogleInterstitialAds {

    private const val TAG = "GoogleInter"

    private var interstitialAd: InterstitialAd? = null

    var originalAdsShown = 0
    private var adError = false
    private var adDisplayAttempts = 0
    private var isTimerRunning = false


    fun loadInterstitial(activity: Activity) {
        if (originalAdsShown >= AdsConstant.googleInterMaxInterAdsShow) {
            Log.e(TAG, "Max interstitial ads shown.")
            return
        }

        AdsConstant.isSplashInterCall = true

        Log.e(TAG, "InterAds_request_load")
        InterstitialAd.load(
            activity,
            AdsConstant.interstitialAds,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(currentAd: InterstitialAd) {
                    Log.e(TAG, "InterAds_onAdLoaded")

                    interstitialAd = currentAd
                    adError = false

                    currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            Log.e(TAG, "InterAds_AdShowedFull")

                            AdsConstant.firstTime = true

                            originalAdsShown++
                            loadInterstitial(activity)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.e(TAG, "InterAds_AdDismissedFull")

                            startCooldownTimer()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "InterAds_AdFailedToShow")

                            interstitialAd = null
                            adError = true
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "InterAds_AdFailedToLoad_${error.message}")

                    interstitialAd = null
                    adError = true
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, from: String) {
        if (originalAdsShown >= AdsConstant.googleInterMaxInterAdsShow) {
            Log.e(TAG, "showInterstitial ::--- show max---")
            return
        }

        Log.e(TAG, "InterAds_req_show_${from}")

        if (!AdsConstant.firstTime && !isTimerRunning) {
            interstitialAd?.let {
                Log.e(TAG, "InterAds_show_${from}")
                it.show(activity)
            }
        } else {
            adDisplayAttempts++
            Log.e(TAG, "adsClick = $adDisplayAttempts")

            if (shouldShowAd()) {
                interstitialAd?.let {
                    Log.e(TAG, "InterAds_Show_else_${from}")
                    it.show(activity)
                }
            }
        }
    }

    private fun shouldShowAd(): Boolean {
        return if (!isTimerRunning && adDisplayAttempts > AdsConstant.googleInterGapBetweenTwoInter && originalAdsShown < AdsConstant.googleInterMaxInterAdsShow) {
            adDisplayAttempts = 0
            Log.e(TAG, "InterAds_adsShowOrNot_true")
            true
        } else {
            Log.e(TAG, "InterAds_adsShowOrNot_false")
            false
        }
    }

    private fun startCooldownTimer() {
        Log.e(TAG, "Starting timer...")
        isTimerRunning = true

        object : CountDownTimer(AdsConstant.googleInterCountDownTimer, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.e(TAG, "Inter_Timer: ${millisUntilFinished / 1000}s remaining")
            }

            override fun onFinish() {
                isTimerRunning = false
                Log.e(TAG, "Inter timer finished")
            }
        }.start()
    }


}