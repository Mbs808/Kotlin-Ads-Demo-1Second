package com.origin.moreads.ui.activities.ads.shimmer

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.extensions.color
import com.origin.moreads.extensions.setStatusAndNavigationBarColor
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.ads.shimmer.bannerad.ShimmerBannerAdActivity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeAdActivity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd100Activity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd130Activity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd60Activity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd80Activity
import com.origin.moreads.ui.activities.language.BaseActivity

class ShimmerAdsActivity : BaseActivity() {

    /***** Main View *****/
    private var btnBannerAd: TextView? = null
    private var btnNativeAd: TextView? = null
    private var btnNativeBannerAd130: TextView? = null
    private var btnNativeBannerAd100: TextView? = null
    private var btnNativeBannerAd80: TextView? = null
    private var btnNativeBannerAd60: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shimmer_ads)

        initializeViews()
        setOnClickListener()

        Log.e(LOG_TAG, "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())

    }

    private fun initializeViews() {
        /***** MainView *****/
        btnBannerAd = findViewById(R.id.tvBannerAd)
        btnNativeAd = findViewById(R.id.tvNativeAd)
        btnNativeBannerAd130 = findViewById(R.id.tvNativeBannerAd130)
        btnNativeBannerAd100 = findViewById(R.id.tvNativeBannerAd100)
        btnNativeBannerAd80 = findViewById(R.id.tvNativeBannerAd80)
        btnNativeBannerAd60 = findViewById(R.id.tvNativeBannerAd60)
    }

    private fun setOnClickListener() {
        /***** MainView *****/
        btnBannerAd?.setOnClickListener {
            startIntent(ShimmerBannerAdActivity::class.java)
        }

        btnNativeAd?.setOnClickListener {
            startIntent(ShimmerNativeAdActivity::class.java)
        }

        btnNativeBannerAd130?.setOnClickListener {
            startIntent(ShimmerNativeBannerAd130Activity::class.java)
        }

        btnNativeBannerAd100?.setOnClickListener {
            startIntent(ShimmerNativeBannerAd100Activity::class.java)
        }

        btnNativeBannerAd80?.setOnClickListener {
            startIntent(ShimmerNativeBannerAd80Activity::class.java)
        }

        btnNativeBannerAd60?.setOnClickListener {
            startIntent(ShimmerNativeBannerAd60Activity::class.java)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                Log.e(LOG_TAG, "${TAG}_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_onBackPressed", Bundle())
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val LOG_TAG = "ShimmerAdsActivity"
        private const val TAG = "ShimmerAds"
    }

}