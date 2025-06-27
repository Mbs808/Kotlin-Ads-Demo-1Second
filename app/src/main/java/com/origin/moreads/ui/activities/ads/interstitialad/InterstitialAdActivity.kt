package com.origin.moreads.ui.activities.ads.interstitialad

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.adsload.GoogleInterstitialAds
import com.origin.moreads.ui.activities.language.BaseActivity

class InterstitialAdActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial_ad)

        /***** Show Initialize Ads *****/
        GoogleInterstitialAds.googleInterstitialShow(this, TAG)

        Log.e(LOG_TAG, "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e(LOG_TAG, "${TAG}_onBackPressed")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_onBackPressed", Bundle())

            GoogleInterstitialAds.googleInterstitialShow(this, "${TAG}_BACK")
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val LOG_TAG = "InterstitialAd"
        private const val TAG = "InterstitialAd"
    }

}