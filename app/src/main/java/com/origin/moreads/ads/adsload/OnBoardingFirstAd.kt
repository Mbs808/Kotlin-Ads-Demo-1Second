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

object OnBoardingFirstAd {

    private const val TAG = "AdLoaded"

    /** Unified Native Ads Loaded Variables **/
    var onB1NativeAds: NativeAd? = null

    var isOnB1LoadingMutableLiveData: MutableLiveData<Boolean?> = MutableLiveData(null)
    var isLoadingInOnBoarding = false
    var isLoadingInLang = false

    private fun getAdRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun loadGoogleNativeAd(context: Context, adId: String, nativeAdReference: (NativeAd?) -> Unit) {
        Log.e("onBoarding", "OnBoardingFirstAd------load start----" )
        val builder = AdLoader.Builder(context, adId).forNativeAd { nativeAd ->
            nativeAdReference(nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e("onBoarding", "OnBoardingFirstAd------onAdFailedToLoad----$loadAdError" )

                nativeAdReference(null)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e("onBoarding", "OnBoardingFirstAd------onAdLoaded----" )

            }

            override fun onAdClicked() {

            }
        }).build()
        adLoader.loadAd(getAdRequest())
    }
}