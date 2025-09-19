package com.origin.moreads.ads.adsload

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.os.Bundle
import com.origin.moreads.MainApplication
import com.origin.moreads.ads.api.MoreDataApiClient
import com.origin.moreads.ads.model.MoreApp
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.utils.EventLog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

object MoreAppDataLoader {
    private var isMoreAdsDataLoadProgress = false
    private var secondsMoreAds = 0
    private var isRunning = false
    private val adsHandler = Handler(Looper.getMainLooper())
    private lateinit var adsRunnable: Runnable
    private var lastLoadFailed = false

    // check loading state
    val isLoading: Boolean
        get() = isMoreAdsDataLoadProgress

    // check if last load failed
    val didLastLoadFail: Boolean
        get() = lastLoadFailed

    fun loadMoreAppData(url: String, accountName: String) {
        Log.e(EventLog, "MoreAppData_load_start")
        MainApplication.firebaseAnalytics?.logEvent("MoreAppData_load_start", Bundle())

        if (!isMoreAdsDataLoadProgress) {
            lastLoadFailed = false
            startTimerMoreApp()

            Log.e(EventLog, "MoreAppData_request")
            MainApplication.firebaseAnalytics?.logEvent("MoreAppData_request", Bundle())

            try {
                val moreDataApi = MoreDataApiClient.getMoreApiInterface(url)
                AdsConstant.moreAppDataList = ArrayList()
                val response = moreDataApi.getMoreList(accountName)
                response.enqueue(object : Callback<MoreApp> {
                    override fun onResponse(call: Call<MoreApp?>, response: Response<MoreApp?>) {
                        stopTimeMoreApp()

                        if (response.isSuccessful && response.body() != null) {
                            lastLoadFailed = false

                            Log.e(EventLog, "MoreAppData_load_success")
                            MainApplication.firebaseAnalytics?.logEvent(
                                "MoreAppData_load_success",
                                Bundle()
                            )
                            val moreApp = response.body()
                            if (moreApp != null) {
                                val moreAppList = moreApp.moreAppData

                                if (!moreAppList.isNullOrEmpty()) {
                                    AdsConstant.moreAppDataList.addAll(moreAppList)
                                }
                            }
                        } else {
                            Log.e(EventLog, "MoreAppData_else")
                            lastLoadFailed = true
                        }
                    }

                    override fun onFailure(call: Call<MoreApp>, t: Throwable) {
                        Log.e(EventLog, "MoreAppData_onFailure")
                        MainApplication.firebaseAnalytics?.logEvent(
                            "MoreAppData_onFailure",
                            Bundle()
                        )
                        stopTimeMoreApp()
                        lastLoadFailed = true
                        call.cancel()
                    }
                })
            } catch (e: IllegalArgumentException) {
                Log.e(EventLog, "MoreAppData_catch")
                MainApplication.firebaseAnalytics?.logEvent("MoreAppData_catch", Bundle())
                e.printStackTrace()

                stopTimeMoreApp()
                lastLoadFailed = true
            }
        }
    }

    private fun startTimerMoreApp() {
        isMoreAdsDataLoadProgress = true
        secondsMoreAds = 0
        isRunning = true
        adsRunnable = Runnable {
            val sec = secondsMoreAds % 60
            val time = String.format(Locale.getDefault(), "%02d", sec)

            Log.e(EventLog, "MoreAppData_time_$time")

            if (secondsMoreAds == 120) {
                Log.e(EventLog, "MoreAppData_Timeout$secondsMoreAds")
                MainApplication.firebaseAnalytics!!.logEvent("MoreAppData_Timeout$secondsMoreAds", Bundle())

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

    private fun stopTimeMoreApp() {
        isMoreAdsDataLoadProgress = false
        if (::adsRunnable.isInitialized) {
            isRunning = false
            adsHandler.removeCallbacks(adsRunnable)
        }
    }
}