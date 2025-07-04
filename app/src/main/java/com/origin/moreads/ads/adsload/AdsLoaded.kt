package com.origin.moreads.ads.adsload

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.origin.moreads.ads.utils.AdsConstant
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.origin.moreads.MainApplication
import com.origin.moreads.ui.activities.main.MainActivity
import com.origin.moreads.ui.activities.main.MainActivity.Companion

object AdsLoaded {

    /** Unified Native Ads Loaded Variables **/
    var languageUnifiedNativeAds: NativeAd? = null
    var exitDialogUnifiedNativeAds: NativeAd? = null

    var isLanguageAdLoadingMutableLiveData: MutableLiveData<Boolean?> = MutableLiveData(null)
    var isLoadingInLanguage = false
    var isLanguageLoadingInSplash = false

    private fun getAdRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun loadGoogleNativeAd(context: Context, adId: String, nativeAdReference: (NativeAd?) -> Unit) {
        Log.e("Ads_Demo", "AdsLoaded_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("AdsLoaded_LoadStart", Bundle())

        val builder = AdLoader.Builder(context, adId).forNativeAd { nativeAd ->
            nativeAdReference(nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e("Ads_Demo", "AdsLoaded_Fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("AdsLoaded_Fail", Bundle())

                nativeAdReference(null)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e("Ads_Demo", "AdsLoaded_Loaded")
                MainApplication.firebaseAnalytics?.logEvent("AdsLoaded_Loaded", Bundle())
            }

            override fun onAdClicked() {
                Log.e("Ads_Demo", "AdsLoaded_Clicked")
                MainApplication.firebaseAnalytics?.logEvent("AdsLoaded_Clicked", Bundle())

                AdsConstant.isAdsClick = true
            }
        }).build()
        adLoader.loadAd(getAdRequest())
    }
}