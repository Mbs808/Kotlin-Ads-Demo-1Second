package com.origin.moreads.ads.firebasegetdata

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.origin.moreads.MainApplication
import com.origin.moreads.ads.api.moredata.MoreDataApiClient
import com.origin.moreads.ads.model.moredata.MoreApp
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.ads.utils.AdsConstant.isMoreAdsDataLoadProgress
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.origin.moreads.ads.adsload.AdsLoaded
import com.origin.moreads.ads.utils.SharedPreferenceHelper
import com.origin.moreads.extensions.prefsHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class RemoteConfigManager(private val activity: Activity) {

    private var firebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var seconds = 0
    var secondsMoreAds = 0
    private var isRunning = false
    private var adsHandler = Handler(Looper.getMainLooper())

    private var adsRunnable = Runnable {
    }

    init {
        Log.e(TAG, "remort_data_loadstart")
        MainApplication.firebaseAnalytics?.logEvent("remort_data_loadstart", Bundle())

        isRunning = true
        startTimer()

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val firebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        firebaseRemoteConfig?.setConfigSettingsAsync(firebaseRemoteConfigSettings)
        firebaseRemoteConfig?.fetchAndActivate()?.addOnCompleteListener {
            if (it.isSuccessful) {

                AdsConstant.isLoadedAdID = true

                val updated = it.result

                Log.e(TAG, "remort_data_success")
                MainApplication.firebaseAnalytics?.logEvent("remort_data_success", Bundle())


                Log.e(TAG, "config_params_updated$updated")
                MainApplication.firebaseAnalytics?.logEvent("remort_data_updated$updated", Bundle())


                /** Interstitial Ad **/
                val interstitialAds = firebaseRemoteConfig?.getString("interstitialAds")

                /** Native **/
                val nativeAds = firebaseRemoteConfig?.getString("nativeAds")

                /** Native Banner **/
                val nativeBannerAds = firebaseRemoteConfig?.getString("nativeBannerAds")

                /** Banner Ad **/
                val bannerAds = firebaseRemoteConfig?.getString("bannerAds")

                /** Language Native Ad **/
                val nativeLanguageAds = firebaseRemoteConfig?.getString("nativeLanguageAds")
                val nativeBannerLanguageAds =
                    firebaseRemoteConfig?.getString("nativeBannerLanguageAds")

                /** Exit Dialog Native Ad **/
                val nativeExitDialogAds = firebaseRemoteConfig?.getString("nativeExitDialogAds")

                /** Shimmer  Ads **/
                val showNativeShimmer =
                    firebaseRemoteConfig?.getString("showNativeShimmer")
                val showNativeBannerShimmer130 =
                    firebaseRemoteConfig?.getString("showNativeBannerShimmer130")
                val showNativeBannerShimmer100 =
                    firebaseRemoteConfig?.getString("showNativeBannerShimmer100")
                val showNativeBannerShimmer80 =
                    firebaseRemoteConfig?.getString("showNativeBannerShimmer80")
                val showNativeBannerShimmer60 =
                    firebaseRemoteConfig?.getString("showNativeBannerShimmer60")
                val showBannerShimmer =
                    firebaseRemoteConfig?.getString("showBannerShimmer")

                /** Global Variables **/
                val playStoreLink = firebaseRemoteConfig?.getString("playStoreLink")
                val maxAdContentRating = firebaseRemoteConfig?.getString("maxAdContentRating")
                val splashCloseTimer = firebaseRemoteConfig?.getString("splashCloseTimer")
                val splashAppOpenShow = firebaseRemoteConfig?.getString("splashAppOpenShow")
                val onlineSplashAppOpen = firebaseRemoteConfig?.getString("onlineSplashAppOpen")

                /** Language **/
                val showBigNativeLanguage = firebaseRemoteConfig?.getString("showBigNativeLanguage")
                val showLanguageNativeAd = firebaseRemoteConfig?.getString("showLanguageNativeAd")

                /** Google Interstitial Ad Variable **/
                val googleInterMaxInterAdsShow = firebaseRemoteConfig?.getString("googleInterMaxInterAdsShow")
                val googleInterGapBetweenTwoInter = firebaseRemoteConfig?.getString("googleInterGapBetweenTwoInter")
                val googleInterCountDownTimer = firebaseRemoteConfig?.getString("googleInterCountDownTimer")
                val firstTime = firebaseRemoteConfig?.getString("firstTime")

                /** More App Ads **/
                val showMoreAppLanguage = firebaseRemoteConfig?.getString("showMoreAppLanguage")
                val onlyShowMoreAppLanguage = firebaseRemoteConfig?.getString("onlyShowMoreAppLanguage")
                val showMoreAppNative = firebaseRemoteConfig?.getString("showMoreAppNative")
                val showMoreAppNativeBanner = firebaseRemoteConfig?.getString("showMoreAppNativeBanner")
                val showMoreAppBanner = firebaseRemoteConfig?.getString("showMoreAppBanner")
                val onlyShowMoreAppNative = firebaseRemoteConfig?.getString("onlyShowMoreAppNative")
                val onlyShowMoreAppNativeBanner = firebaseRemoteConfig?.getString("onlyShowMoreAppNativeBanner")
                val onlyShowMoreAppBanner = firebaseRemoteConfig?.getString("onlyShowMoreAppBanner")
                val moreAppUrl = firebaseRemoteConfig?.getString("moreAppUrl")
                val moreAppAccountName = firebaseRemoteConfig?.getString("moreAppAccountName")

                /** Exit App **/
                val showAdsExitDialog = firebaseRemoteConfig?.getString("showAdsExitDialog")

                /** OnBoarding Screen **/
                val onBoardingNative1 = firebaseRemoteConfig?.getString("onBoarding_1_BigNative")
                val onBoardingNative2 = firebaseRemoteConfig?.getString("onBoarding_2_BigNative")
                val onBoardingNativeFull = firebaseRemoteConfig?.getString("onBoarding_Full_BigNative")

                val isShowBoardingScreen = firebaseRemoteConfig?.getString("isShow_onBoardingScreen")
                val isShowBoarding1Ads = firebaseRemoteConfig?.getString("isShow_onBoarding_1Ads")
                val isShowBoarding2Ads = firebaseRemoteConfig?.getString("isShow_onBoarding_2Ads")
                val isShowBoardingFullAds = firebaseRemoteConfig?.getString("isShow_onBoarding_FullAds")
                val onBoardingFullTimer = firebaseRemoteConfig?.getString("onBoarding_FullTimer")

                /******************** Assignment of Values ********************************/

                if (!onBoardingNative1.isNullOrEmpty()) {
                    AdsConstant.onBoarding_1_BigNative = onBoardingNative1
                }

                if (!onBoardingNative2.isNullOrEmpty()) {
                    AdsConstant.onBoarding_2_BigNative = onBoardingNative2
                }

                if (!onBoardingNativeFull.isNullOrEmpty()) {
                    AdsConstant.onBoarding_Full_BigNative = onBoardingNativeFull
                }

                if (!isShowBoardingScreen.isNullOrEmpty()) {
                    AdsConstant.isShow_onBoardingScreen = isShowBoardingScreen
                }

                if (!isShowBoarding1Ads.isNullOrEmpty()) {
                    AdsConstant.isShow_onBoarding_1Ads = isShowBoarding1Ads
                }

                if (!isShowBoarding2Ads.isNullOrEmpty()) {
                    AdsConstant.isShow_onBoarding_2Ads = isShowBoarding2Ads
                }

                if (!isShowBoardingFullAds.isNullOrEmpty()) {
                    AdsConstant.isShow_onBoarding_FullAds = isShowBoardingFullAds
                }

                if (!onBoardingFullTimer.isNullOrEmpty()) {
                    AdsConstant.onBoarding_FullTimer = try {
                        onBoardingFullTimer.toLong()
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "onBoardingFullTimer: NumberFormatException : $e")
                        6000L
                    } catch (e1: Exception) {
                        Log.e(TAG, "onBoardingFullTimer: Exception : $e1")
                        6000L
                    }
                }



                if (!interstitialAds.isNullOrEmpty()) {
                    AdsConstant.interstitialAds = interstitialAds
                }

                if (!nativeAds.isNullOrEmpty()) {
                    AdsConstant.nativeAds = nativeAds
                }

                if (!nativeBannerAds.isNullOrEmpty()) {
                    AdsConstant.nativeBannerAds = nativeBannerAds
                }

                if (!bannerAds.isNullOrEmpty()) {
                    AdsConstant.bannerAds = bannerAds
                }

                if (!nativeLanguageAds.isNullOrEmpty()) {
                    AdsConstant.nativeLanguageAds = nativeLanguageAds
                }

                if (!nativeBannerLanguageAds.isNullOrEmpty()) {
                    AdsConstant.nativeBannerLanguageAds = nativeBannerLanguageAds
                }

                if (!nativeExitDialogAds.isNullOrEmpty()) {
                    AdsConstant.nativeExitDialogAds = nativeExitDialogAds
                }

                if (!showNativeShimmer.isNullOrEmpty()) {
                    AdsConstant.showNativeShimmer = showNativeShimmer
                }

                if (!showNativeBannerShimmer130.isNullOrEmpty()) {
                    AdsConstant.showNativeBannerShimmer130 = showNativeBannerShimmer130
                }

                if (!showNativeBannerShimmer100.isNullOrEmpty()) {
                    AdsConstant.showNativeBannerShimmer100 = showNativeBannerShimmer100
                }

                if (!showNativeBannerShimmer80.isNullOrEmpty()) {
                    AdsConstant.showNativeBannerShimmer80 = showNativeBannerShimmer80
                }

                if (!showNativeBannerShimmer60.isNullOrEmpty()) {
                    AdsConstant.showNativeBannerShimmer60 = showNativeBannerShimmer60
                }

                if (!showBannerShimmer.isNullOrEmpty()) {
                    AdsConstant.showBannerShimmer = showBannerShimmer
                }

                if (!playStoreLink.isNullOrEmpty()) {
                    AdsConstant.playStoreLink = playStoreLink
                }

                if (!maxAdContentRating.isNullOrEmpty()) {
                    AdsConstant.maxAdContentRating = maxAdContentRating
                }

                if (!splashCloseTimer.isNullOrEmpty()) {
                    AdsConstant.splashCloseTimer = splashCloseTimer
                }

                if (!splashAppOpenShow.isNullOrEmpty()) {
                    AdsConstant.splashAppOpenShow = splashAppOpenShow
                }

                if (!onlineSplashAppOpen.isNullOrEmpty()) {
                    AdsConstant.onlineSplashAppOpen = onlineSplashAppOpen
                }

                if (!showBigNativeLanguage.isNullOrEmpty()) {
                    AdsConstant.showBigNativeLanguage = showBigNativeLanguage
                }

                if (!showLanguageNativeAd.isNullOrEmpty()) {
                    AdsConstant.showLanguageNativeAd = showLanguageNativeAd
                }

                if (!googleInterMaxInterAdsShow.isNullOrEmpty()) {
                    AdsConstant.googleInterMaxInterAdsShow = googleInterMaxInterAdsShow.toInt()
                }

                if (!googleInterGapBetweenTwoInter.isNullOrEmpty()) {
                    AdsConstant.googleInterGapBetweenTwoInter =
                        googleInterGapBetweenTwoInter.toInt()
                }

                if (!googleInterCountDownTimer.isNullOrEmpty()) {
                    AdsConstant.googleInterCountDownTimer = try {
                        googleInterCountDownTimer.toLong()
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "googleInterCountDownTimer: NumberFormatException : $e")
                        10000L
                    } catch (e1: Exception) {
                        Log.e(TAG, "googleInterCountDownTimer: Exception : $e1")
                        10000L
                    }
                }

                if (!firstTime.isNullOrEmpty()) {
                    AdsConstant.firstTime = firstTime.toBoolean()
                }

                if (!showMoreAppLanguage.isNullOrEmpty()) {
                    AdsConstant.showMoreAppLanguage = showMoreAppLanguage
                }

                if (!onlyShowMoreAppLanguage.isNullOrEmpty()) {
                    AdsConstant.onlyShowMoreAppLanguage = onlyShowMoreAppLanguage
                }

                if (!showMoreAppNative.isNullOrEmpty()) {
                    AdsConstant.showMoreAppNative = showMoreAppNative
                }

                if (!showMoreAppNativeBanner.isNullOrEmpty()) {
                    AdsConstant.showMoreAppNativeBanner = showMoreAppNativeBanner
                }

                if (!showMoreAppBanner.isNullOrEmpty()) {
                    AdsConstant.showMoreAppBanner = showMoreAppBanner
                }

                if (!onlyShowMoreAppNative.isNullOrEmpty()) {
                    AdsConstant.onlyShowMoreAppNative = onlyShowMoreAppNative
                }

                if (!onlyShowMoreAppNativeBanner.isNullOrEmpty()) {
                    AdsConstant.onlyShowMoreAppNativeBanner = onlyShowMoreAppNativeBanner
                }

                if (!onlyShowMoreAppBanner.isNullOrEmpty()) {
                    AdsConstant.onlyShowMoreAppBanner = onlyShowMoreAppBanner
                }


                // splash 1 sec
                moreAppUrl?.takeIf { it != activity.prefsHelper.moreAppUrl }?.let {
                    activity.prefsHelper.moreAppUrl = it
                }

                AdsConstant.moreAppUrl = moreAppUrl ?: ""

                moreAppAccountName?.takeIf { it != activity.prefsHelper.mreAppAccountName }?.let {
                    activity.prefsHelper.mreAppAccountName = it
                }

                AdsConstant.moreAppAccountName = moreAppAccountName ?: ""

                if (!showAdsExitDialog.isNullOrEmpty()) {
                    AdsConstant.showAdsExitDialog = showAdsExitDialog
                }

                /*************************************************************************************************/
                stopTimer()

                /*** Load More App Data ***/
                loadMoreAppData()
                /****************************/

                /*************************************** Logging Start  *******************************************/
                Log.e(TAG, "GOOGLE_INTERSTITIAL: ${AdsConstant.interstitialAds}")
                Log.e(TAG, "GOOGLE_NATIVE: ${AdsConstant.nativeAds}")
                Log.e(TAG, "GOOGLE_NATIVE_BANNER: ${AdsConstant.nativeBannerAds}")
                Log.e(TAG, "GOOGLE_BANNER: ${AdsConstant.bannerAds}")
                Log.e(TAG, "LANGUAGE_GOOGLE_NATIVE: ${AdsConstant.nativeLanguageAds}")
                Log.e(
                    TAG,
                    "LANGUAGE_GOOGLE_NATIVE_BANNER: ${AdsConstant.nativeBannerLanguageAds}"
                )
                Log.e(TAG, "GOOGLE_NATIVE_EXIT_DIALOG: ${AdsConstant.nativeExitDialogAds}")
                Log.e(TAG, "showGoogleNativeShimmer: ${AdsConstant.showNativeShimmer}")
                Log.e(
                    TAG,
                    "showGoogleNativeBannerShimmer130: ${AdsConstant.showNativeBannerShimmer130}"
                )
                Log.e(
                    TAG,
                    "showGoogleNativeBannerShimmer100: ${AdsConstant.showNativeBannerShimmer100}"
                )
                Log.e(
                    TAG,
                    "showGoogleNativeBannerShimmer80: ${AdsConstant.showNativeBannerShimmer80}"
                )

                Log.e(TAG, "showGoogleNativeBannerShimmer60: ${AdsConstant.showNativeBannerShimmer60}")
                Log.e(TAG, "showGoogleBannerShimmer: ${AdsConstant.showBannerShimmer}")
                Log.e(TAG, "playStoreLink: ${AdsConstant.playStoreLink}")
                Log.e(TAG, "maxAdContentRating: ${AdsConstant.maxAdContentRating}")
                Log.e(TAG, "splashCloseTimer: ${AdsConstant.splashCloseTimer}")
                Log.e(TAG, "splashAppOpenShow: ${AdsConstant.splashAppOpenShow}")
                Log.e(TAG, "onlineSplashAppOpen: ${AdsConstant.onlineSplashAppOpen}")
                Log.e(TAG, "showBigNativeLanguage: ${AdsConstant.showBigNativeLanguage}")
                Log.e(TAG, "showLanguageNativeAd: ${AdsConstant.showLanguageNativeAd}")
                Log.e(TAG, "googleInterMaxInterAdsShow: ${AdsConstant.googleInterMaxInterAdsShow}")
                Log.e(TAG, "googleInterGapBetweenTwoInter: ${AdsConstant.googleInterGapBetweenTwoInter}")
                Log.e(TAG, "googleInterCountDownTimer: ${AdsConstant.googleInterCountDownTimer}")
                Log.e(TAG, "firstTime: ${AdsConstant.firstTime}")
                Log.e(TAG, "showMoreAppLanguage: ${AdsConstant.showMoreAppLanguage}")
                Log.e(TAG, "onlyShowMoreAppLanguage: ${AdsConstant.onlyShowMoreAppLanguage}")
                Log.e(TAG, "showMoreAppNative: ${AdsConstant.showMoreAppNative}")
                Log.e(TAG, "showMoreAppNativeBanner: ${AdsConstant.showMoreAppNativeBanner}")
                Log.e(TAG, "showMoreAppBanner: ${AdsConstant.showMoreAppBanner}")
                Log.e(TAG, "onlyShowMoreAppNative: ${AdsConstant.onlyShowMoreAppNative}")
                Log.e(
                    TAG,
                    "onlyShowMoreAppNativeBanner: ${AdsConstant.onlyShowMoreAppNativeBanner}"
                )
                Log.e(TAG, "onlyShowMoreAppBanner: ${AdsConstant.onlyShowMoreAppBanner}")
                Log.e(TAG, "moreAppUrl: ${AdsConstant.moreAppUrl}")
                Log.e(TAG, "moreAppAccountName: ${AdsConstant.moreAppAccountName}")
                Log.e(TAG, "showAdsExitDialog: ${AdsConstant.showAdsExitDialog}")
                /*************************************** Logging End  *******************************************/



            } else {
                stopTimer()

                Log.e(TAG, "remort_data_failed")
                MainApplication.firebaseAnalytics?.logEvent("remort_data_failed", Bundle())

            }
        }
    }

    fun loadMoreAppData() {

        if (!isMoreAdsDataLoadProgress) {
            startTimerMoreApp()

            Log.e(TAG, "more_App_Request")
            MainApplication.firebaseAnalytics?.logEvent("more_App_Request", Bundle())

            try {
                val moreDataApi = MoreDataApiClient.getMoreApiInterface(AdsConstant.moreAppUrl)

                AdsConstant.moreAppDataList = ArrayList()
                val response = moreDataApi.getMoreList(AdsConstant.moreAppAccountName)
                response.enqueue(object : Callback<MoreApp> {
                    override fun onResponse(call: Call<MoreApp?>, response: Response<MoreApp?>) {
                        Log.e(TAG, "loadMoreData_Success")
                        MainApplication.firebaseAnalytics?.logEvent(
                            "loadMoreData_Success",
                            Bundle()
                        )


                        stopTimeMoreApp()
                        if (response.isSuccessful && response.body() != null) {
                            val moreApp = response.body()
                            Log.e(TAG, "loadMoreData_Success")
                            if (moreApp != null) {
                                val moreAppList = moreApp.moreAppData
                                Log.e(TAG, "moreAppList: ::: $moreAppList")
                                if (!moreAppList.isNullOrEmpty()) {
                                    AdsConstant.moreAppDataList.addAll(moreAppList)
                                }
                            }
                        }

                    }

                    override fun onFailure(call: Call<MoreApp>, t: Throwable) {
                        Log.e(TAG, "loadMoreApp_onFailure")
                        MainApplication.firebaseAnalytics?.logEvent(
                            "loadMoreApp_onFailure",
                            Bundle()
                        )

                        stopTimeMoreApp()
                        call.cancel()
                    }
                })

            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }


    }

    private fun startTimer() {
        seconds = 0

        adsRunnable = Runnable {
            val sec = seconds % 60
            val time = String.format(Locale.getDefault(), "%02d", sec)
            Log.e(TAG, "remote_Time_$time")

            if (seconds == 120) {
                Log.e(TAG, "Remort_120_TIMEOUT")
                MainApplication.firebaseAnalytics!!.logEvent("Remort_120_TIMEOUT$seconds", Bundle())

                stopTimer()
                return@Runnable
            }

            if (isRunning) {
                seconds++
            }
            adsHandler.postDelayed(adsRunnable, 1000)
        }

        adsHandler.post(adsRunnable)
    }

    private fun stopTimer() {
        if (adsHandler != null && adsRunnable != null) {
            isRunning = false
            adsHandler.removeCallbacks(adsRunnable)
        }
    }

    fun startTimerMoreApp() {

        isMoreAdsDataLoadProgress = true
        secondsMoreAds = 0
        isRunning = true
        adsRunnable = Runnable {
            val sec = secondsMoreAds % 60
            val time = String.format(Locale.getDefault(), "%02d", sec)
            Log.e(TAG, "moreApp_Time_$time")

            if (secondsMoreAds == 120) {

                Log.e(TAG, "moreapp_DATA_120_TIMEOUT")

                stopTimeMoreApp()

                return@Runnable
            }

            if (isRunning) {
                secondsMoreAds++
            }
            adsHandler.postDelayed(adsRunnable, 1000)
        }

        adsHandler.post(adsRunnable)
    }

    fun stopTimeMoreApp() {
        isMoreAdsDataLoadProgress = false
        if (adsHandler != null && adsRunnable != null) {
            isRunning = false
            adsHandler.removeCallbacks(adsRunnable)
        }
    }

    companion object {
        private const val TAG = "firebase----"
    }

}