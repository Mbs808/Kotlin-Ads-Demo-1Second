package com.origin.moreads.ui.activities.ads.shimmer

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.origin.moreads.MainApplication
import com.origin.moreads.databinding.ActivityShimmerAdsBinding
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.ads.shimmer.bannerad.ShimmerBannerAdActivity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeAdActivity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd100Activity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd130Activity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd60Activity
import com.origin.moreads.ui.activities.ads.shimmer.nativead.ShimmerNativeBannerAd80Activity
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.utils.EventLog

class ShimmerAdsActivity : BaseActivity() {

    private lateinit var binding: ActivityShimmerAdsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShimmerAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickListener()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "ShimmerAdsAct_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("ShimmerAdsAct_onBackPressed", Bundle())

                finish()
            }
        })

        Log.e(EventLog, "ShimmerAdsAct_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerAdsAct_onCreate", Bundle())
    }

    private fun setOnClickListener() {
        /***** MainView *****/
        binding.tvBannerAd.setOnClickListener {
            startIntent(ShimmerBannerAdActivity::class.java)
        }

        binding.tvNativeAd.setOnClickListener {
            startIntent(ShimmerNativeAdActivity::class.java)
        }

        binding.tvNativeBannerAd130.setOnClickListener {
            startIntent(ShimmerNativeBannerAd130Activity::class.java)
        }

        binding.tvNativeBannerAd100.setOnClickListener {
            startIntent(ShimmerNativeBannerAd100Activity::class.java)
        }

        binding.tvNativeBannerAd80.setOnClickListener {
            startIntent(ShimmerNativeBannerAd80Activity::class.java)
        }

        binding.tvNativeBannerAd60.setOnClickListener {
            startIntent(ShimmerNativeBannerAd60Activity::class.java)
        }

    }

}