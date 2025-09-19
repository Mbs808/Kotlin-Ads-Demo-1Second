package com.origin.moreads.ui.activities.ads.interstitialad

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.adsload.GoogleInterstitialAds
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.utils.EventLog

class InterstitialAdActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial_ad)

        /***** Show Inter Ads *****/
        GoogleInterstitialAds.showInterstitial(this, "InterAct_onCreate")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "InterAct_onBack")
                MainApplication.firebaseAnalytics?.logEvent("InterAct_onBack", Bundle())

                GoogleInterstitialAds.showInterstitial(
                    this@InterstitialAdActivity,
                    "InterAct_onBack"
                )

                finish()
            }
        })

        Log.e(EventLog, "InterAct_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("InterAct_onCreate", Bundle())
    }
}