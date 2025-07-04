package com.origin.moreads.ads.utils

import com.origin.moreads.ads.model.moredata.MoreAppData

object AdsConstant {

    var onBoarding_1_BigNative = "ca-app-pub-3940256099942544/2247696110"
    var onBoarding_2_BigNative = "ca-app-pub-3940256099942544/2247696110"
    var onBoarding_Full_BigNative = "ca-app-pub-3940256099942544/2247696110"

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
    var playStoreLink = ""
    var maxAdContentRating = "PG"
    var splashCloseTimer = "no"
    var splashAppOpenShow = "yes"
    var onlineSplashAppOpen = "no"

    var isShow_onBoardingScreen = "yes"
    var isShow_onBoarding_1Ads = "yes"
    var isShow_onBoarding_2Ads = "yes"
    var isShow_onBoarding_FullAds = "yes"
    var onBoarding_FullTimer:Long = 6000L


     var isAdsClick = false


    /** Sub: Global Local Variables **/
    var isSplashInterCall = false

    /*** Language Server Variables ***/
    var showBigNativeLanguage = "yes"
    var showLanguageNativeAd = "yes"

    /** Google Interstitial Ad Server Variable **/
    var googleInterMaxInterAdsShow = 6
    var googleInterGapBetweenTwoInter = 5
    var googleInterCountDownTimer:Long = 10000L
    var firstTime = false

    /** Exit App Server Variables **/
    var showAdsExitDialog = "no"

    /** More App Ads Server Variables **/
    var showMoreAppLanguage = "no"
    var onlyShowMoreAppLanguage = "no"
    var showMoreAppNative = "yes"
    var showMoreAppNativeBanner = "yes"
    var showMoreAppBanner = "yes"
    var onlyShowMoreAppNative = "no"
    var onlyShowMoreAppNativeBanner = "no"
    var onlyShowMoreAppBanner = "no"

    var moreAppUrl = "http://68.183.94.44/moreapps/api/"
    var moreAppAccountName = "xyz"

    /** More App Ads Data Store **/
    var moreAppDataList = arrayListOf<MoreAppData>()
    var adCounter = -1

    /** Splash Activity Local Variables **/
    var isLoadedAdID = false
    var isSplashShowed = false
    var pauseResume = false

    var isMoreAdsDataLoadProgress = false

}