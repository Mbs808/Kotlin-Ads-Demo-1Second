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

object AdsLoaded {

    private const val TAG = "AdLoaded"

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
        Log.d("lanugage---", "splash---" + "load start")
        val builder = AdLoader.Builder(context, adId).forNativeAd { nativeAd ->
            nativeAdReference(nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d("lanugage---", "splash---" + "load failed")
                nativeAdReference(null)
                Log.e(TAG, "loadGoogleNativeAd_failed" + loadAdError.code)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("lanugage---", "splash---" + "load loaded")
                Log.e(TAG, "loadGoogleNativeAd_loaded")
            }

            override fun onAdClicked() {

            }
        }).build()
        adLoader.loadAd(getAdRequest())
    }
}