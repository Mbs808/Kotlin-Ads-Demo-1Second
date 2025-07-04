package com.origin.moreads.ads.adsload

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.origin.moreads.MainApplication
import com.origin.moreads.ads.utils.AdsConstant

object OnBoardingSecondAd {

    /** Unified Native Ads Loaded Variables **/
    var onB2NativeAds: NativeAd? = null

    var isOnB2LoadingMutableLiveData: MutableLiveData<Boolean?> = MutableLiveData(null)
    var isLoadingInOnBoarding = false
    var isLoadingInCurrentAct = false

    private fun getAdRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun loadGoogleNativeAd(context: Context, adId: String, nativeAdReference: (NativeAd?) -> Unit) {
        Log.e("Ads_Demo", "onBASecond_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("onBASecond_LoadStart", Bundle())

        val builder = AdLoader.Builder(context, adId).forNativeAd { nativeAd ->
            nativeAdReference(nativeAd)
        }
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)

                Log.e("Ads_Demo", "onBASecond_Fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("onBASecond_Fail", Bundle())

                nativeAdReference(null)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e("Ads_Demo", "onBASecond_Loaded")
                MainApplication.firebaseAnalytics?.logEvent("onBASecond_Loaded", Bundle())

            }

            override fun onAdClicked() {
                Log.e("Ads_Demo", "onBASecond_Clicked")
                MainApplication.firebaseAnalytics?.logEvent("onBASecond_Clicked", Bundle())
            }
        }).build()
        adLoader.loadAd(getAdRequest())
    }
}