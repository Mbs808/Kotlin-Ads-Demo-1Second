package com.origin.moreads.ads.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.origin.moreads.R
import com.origin.moreads.ads.model.MoreAppData
import com.origin.moreads.models.Language

object AdsConstant {

    /** Interstitial Ad Server Variables **/
    var interstitialAds = "ca-app-pub-3940256099942544/1033173712"

    /** Native Server Variables **/
    var nativeAds = "ca-app-pub-3940256099942544/2247696110"

    /** Native Banner Server Variables **/
    var nativeBannerAds = "ca-app-pub-3940256099942544/2247696110"

    /** Banner Ad Server Variables **/
    var bannerAds = "ca-app-pub-3940256099942544/6300978111"

    /** Language Ad Server Variables **/
    var nativeLanguageAds = "ca-app-pub-3940256099942544/2247696110"
    var nativeBannerLanguageAds = "ca-app-pub-3940256099942544/2247696110"

    /** Exit Dialog Native Ad Server Variables **/
    var nativeExitDialogAds = "ca-app-pub-3940256099942544/2247696110"

    /*** Sub: Shimmer Google Ads Variables ***/
    var showNativeShimmer = "yes"
    var showNativeBannerShimmer130 = "yes"
    var showNativeBannerShimmer100 = "yes"
    var showNativeBannerShimmer80 = "yes"
    var showNativeBannerShimmer60 = "yes"
    var showBannerShimmer = "yes"

    /****** Main: Global Variables ******/
    /** Sub: Global Server Variables **/
    var playStoreLink = "https://play.google.com/store/apps/details?id="
    var maxAdContentRating = "PG"

     var isAdsClick = false


    /** Sub: Global Local Variables **/
    var isSplashInterCall = false

    /*** Language Server Variables ***/
    var showBigNativeLanguage = "yes"
    var showLanguageNativeAd = "yes"

    /** Google Interstitial Ad Server Variable **/
    var googleInterMaxInterAdsShow = 3
    var googleInterGapBetweenTwoInter = 2
    var googleInterCountDownTimer:Long = 10000L
    var firstTime = false

    /** Exit App Server Variables **/
    var showAdsExitDialog = "yes"

    /** More App Ads Server Variables **/
    var showMoreAppLanguage = "yes"
    var showMoreAppNative = "yes"
    var showMoreAppNativeBanner = "yes"
    var showMoreAppBanner = "yes"
    var onlyShowMoreAppLanguage = "no"
    var onlyShowMoreAppNative = "no"
    var onlyShowMoreAppNativeBanner = "no"
    var onlyShowMoreAppBanner = "no"

    var moreAppUrl = "http://68.183.94.44/moreapps/api/"
    var moreAccountName = "xyz"

    /** More App Ads Data Store **/
    var moreAppDataList = arrayListOf<MoreAppData>()
    var adCounter = -1

    /** Splash Activity Local Variables **/
    var isLoadedAdID = false
    var pauseResume = false

    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }


    fun getLanguageList(): List<Language> {
        val languageList: MutableList<Language> = mutableListOf()
        languageList.add(Language(R.drawable.ic_english, "English", "en", false))
        languageList.add(Language(R.drawable.ic_hindi, "हिन्दी", "hi", false))
        languageList.add(Language(R.drawable.ic_chinese, "普通话", "zh", false))
        languageList.add(Language(R.drawable.ic_spanish, "Española", "es", false))
        languageList.add(Language(R.drawable.ic_french, "Français", "fr", false))
        languageList.add(Language(R.drawable.ic_arabic, "عربي", "ar", false))
        languageList.add(Language(R.drawable.ic_bengali, "বাংলা", "bn", false))
        languageList.add(Language(R.drawable.ic_russian, "Русский", "ru", false))
        languageList.add(Language(R.drawable.germany, "Deutsch", "de", false))
        languageList.add(Language(R.drawable.japan, "日本", "ja", false))
        languageList.add(Language(R.drawable.ic_portuges, "Português", "pt", false))
        languageList.add(Language(R.drawable.pakistan, "اردو", "ur", false))
        return languageList
    }
}