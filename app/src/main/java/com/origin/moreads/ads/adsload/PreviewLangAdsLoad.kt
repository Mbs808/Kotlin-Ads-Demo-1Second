package com.origin.moreads.ads.adsload

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.origin.moreads.ads.utils.AdsConstant

object PreviewLangAdsLoad {
    private const val TAG = "lang_native--"

    /** Unified Native Ads Loaded Variables **/
    var languageUnifiedNativeAds: NativeAd? = null

    val isLanguageAdLoadingMutableLiveData = MutableLiveData<Boolean?>()
    var isLoadingInLanguage = false
    var isLoadingInPreview = false

    fun loadLanguageNativeAds(context: Context) {

        if (isLoadingInLanguage || isLoadingInPreview) {
            Log.e(TAG, "PreviewLangAdsLoad_ Ad already loading, skipping.")
            return
        }

        if (AdsConstant.onlyShowMoreAppLanguage == "yes") {
            Log.e(TAG, "PreviewLangAdsLoad_ Ad only shown in more apps section.")
            return
        }

        isLoadingInPreview = true

        Log.e(TAG, "PreviewLangAdsLoad_init")

        val adUnit = if (AdsConstant.showBigNativeLanguage == "yes") {
            AdsConstant.nativeLanguageAds
        } else {
            AdsConstant.nativeBannerLanguageAds
        }

        loadGoogleNativeAd(context, adUnit) { nativeAd ->
            isLoadingInPreview = false
            isLoadingInLanguage = false

            // Destroy previous ad if any
            languageUnifiedNativeAds?.destroy()
            languageUnifiedNativeAds = nativeAd

            isLanguageAdLoadingMutableLiveData.value = nativeAd != null

        }

    }

    private fun loadGoogleNativeAd(
        context: Context,
        adId: String,
        nativeAdReference: (NativeAd?) -> Unit
    ) {
        Log.e(TAG, "PreviewLangAdsLoad_LoadStart")

        val builder = AdLoader.Builder(context.applicationContext, adId).forNativeAd { nativeAd ->
            nativeAdReference(nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e(TAG, "PreviewLangAdsLoad_Fail $loadAdError")
                nativeAdReference(null)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e(TAG, "PreviewLangAdsLoad_Loaded")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.e(TAG, "PreviewLangAdsLoad_Clicked")
                AdsConstant.isAdsClick = true
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun clearLanguageAd() {
        languageUnifiedNativeAds?.destroy()
        languageUnifiedNativeAds = null
        isLanguageAdLoadingMutableLiveData.value = null
        isLoadingInLanguage = false
        isLoadingInPreview = false
    }

}