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

object OnBoardingFullAd {

    /** Unified Native Ads Loaded Variables **/
    var onBFullNativeAds: NativeAd? = null

    var isOnBFullLoadingMutableLiveData: MutableLiveData<Boolean?> = MutableLiveData(null)
    var isLoadingInOnBoarding = false
    var isLoadingInLang = false

    private fun getAdRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun loadGoogleNativeAd(context: Context, adId: String, nativeAdReference: (NativeAd?) -> Unit) {
        Log.e("Ads_Demo", "onBAFull_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("onBAFull_LoadStart", Bundle())

        val builder = AdLoader.Builder(context, adId).forNativeAd { nativeAd ->
            nativeAdReference(nativeAd)
        }
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e("Ads_Demo", "onBAFull_Fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("onBAFull_Fail", Bundle())

                nativeAdReference(null)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e("Ads_Demo", "onBAFull_Loaded")
                MainApplication.firebaseAnalytics?.logEvent("onBAFull_Loaded", Bundle())

            }

            override fun onAdClicked() {
                Log.e("Ads_Demo", "onBAFull_Clicked")
                MainApplication.firebaseAnalytics?.logEvent("onBAFull_Clicked", Bundle())
            }
        }).build()
        adLoader.loadAd(getAdRequest())
    }
}