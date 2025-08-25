package com.origin.moreads.ui.activities.ads.shimmer.nativead

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.databinding.ActivityShimmerNativeBannerAd130Binding
import com.origin.moreads.databinding.GoogleNativeBannerAdView130CloneBinding
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.utils.EventLog
import com.origin.moreads.utils.setGone
import com.origin.moreads.utils.setInvisible
import com.origin.moreads.utils.setVisible
import com.origin.moreads.utils.showAdClick

class ShimmerNativeBannerAd130Activity : BaseActivity() {

    private lateinit var binding: ActivityShimmerNativeBannerAd130Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShimmerNativeBannerAd130Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setAdView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "ShimmerNTBNAd130_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent(
                    "ShimmerNTBNAd130_onBackPressed",
                    Bundle()
                )

                finish()
            }
        })

        Log.e(EventLog, "ShimmerNTBNAd130_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_onCreate", Bundle())
    }

    /**
    You call stopShimmer() before super.onDestroy(),
    which is correct because you’re cleaning up UI components (shimmer animation) before the activity is marked as destroyed.
    Once super.onDestroy() is called, the activity lifecycle is essentially over,
    and accessing views after that could cause issues in some cases (e.g., null references or crashes if the view is no longer attached).
     **/

    override fun onDestroy() {
        binding.shimmerLayoutAd.stopShimmer()
        super.onDestroy()

        Log.e(EventLog, "ShimmerNTBNAd130_onDestroy")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_onDestroy", Bundle())
    }

    private fun setAdView() {
        if (AdsConstant.isConnected(this) && AdsConstant.showNativeBannerShimmer130 == "yes" ) {
            if (AdsConstant.onlyShowMoreAppNativeBanner == "yes") {
                if (AdsConstant.moreAppDataList.isNotEmpty()) {
                    loadMoreAppNativeBannerAd(
                        activity = this,
                        frameLayout = binding.flNativeBanner,
                        shimmerLayout = binding.shimmerLayoutAd
                    )
                } else {
                    binding.shimmerLayoutAd.stopShimmer()
                }
            } else {
                googleNativeBannerAd(
                    activity = this,
                    adID = AdsConstant.nativeBannerAds,
                    frameLayout = binding.flNativeBanner,
                    shimmerLayout = binding.shimmerLayoutAd
                )
            }
        } else {
            binding.rlNativeBanner.setGone()
        }
    }

    private fun getAddRequest(): AdRequest {
        val extras = Bundle().apply {
            putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        }
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    private fun googleNativeBannerAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {

        Log.e(EventLog, "ShimmerNTBNAd130_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_LoadStart", Bundle())

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            shimmerLayout.setGone()
            showNativeBanner(activity, frameLayout, shimmerLayout, nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(EventLog, "ShimmerNTBNAd130_Fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_Fail", Bundle())

                if (AdsConstant.showMoreAppNativeBanner == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeBannerAd(
                                activity,
                                frameLayout,
                                shimmerLayout
                            )
                        }
                    } else {
                        shimmerLayout.stopShimmer()
                    }
                } else {
                    shimmerLayout.stopShimmer()
                }
            }

            override fun onAdLoaded() {
                Log.e(EventLog, "ShimmerNTBNAd130_Loaded")
                MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_Loaded", Bundle())

                shimmerLayout.setGone()
            }

            override fun onAdClicked() {
                Log.e(EventLog, "ShimmerNTBNAd130_Clicked")
                MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_Clicked", Bundle())

                googleNativeBannerAd(activity, adID, frameLayout, shimmerLayout)
            }
        }).build()

        val request = getAddRequest()
        adLoader.loadAd(request)
    }

    private fun showNativeBanner(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout,
        nativeAd: NativeAd
    ) {

        Log.e(EventLog, "ShimmerNTBNAd130_Show")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_Show", Bundle())

        shimmerLayout.setGone()

        val adView = activity.layoutInflater.inflate(
            R.layout.google_native_banner_ad_view_130,
            activity.findViewById(R.id.nativeAd),
            false
        ) as NativeAdView

        populateAppInstallAdView(nativeAd, adView)
        frameLayout.removeAllViews()
        frameLayout.addView(adView)
    }

    private fun populateAppInstallAdView(
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {
        adView.iconView = adView.findViewById(R.id.adIcon)
        adView.headlineView = adView.findViewById(R.id.adName)
        adView.bodyView = adView.findViewById(R.id.adBody)

        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
        (adView.headlineView as TextView).text = nativeAd.headline

        if (nativeAd.body == null) {
            adView.bodyView?.setInvisible()
        } else {
            adView.bodyView?.setVisible()
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.icon == null) {
            adView.iconView?.setGone()
        } else {
            adView.iconView?.setVisible()
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.setInvisible()
        } else {
            adView.callToActionView?.setVisible()
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        adView.setNativeAd(nativeAd)
    }

    private fun loadMoreAppNativeBannerAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        Log.e(EventLog, "ShimmerNTBNAd130_More_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_More_LoadStart", Bundle())

        // Hide shimmer layout safely
        shimmerLayout.setGone()

        // Defensive checks
        if (activity.isFinishing || activity.isDestroyed) return
        if (AdsConstant.moreAppDataList.isEmpty()) return


        val bindingAd = GoogleNativeBannerAdView130CloneBinding.inflate(activity.layoutInflater)
        frameLayout.removeAllViews()
        frameLayout.addView(bindingAd.root)


        AdsConstant.adCounter += 1

        if (AdsConstant.moreAppDataList.size == AdsConstant.adCounter) {
            AdsConstant.adCounter = 0
        }

        val number = AdsConstant.adCounter
        val adData = AdsConstant.moreAppDataList[number]


        Glide.with(activity.applicationContext)
            .asBitmap()
            .load(adData.appIcon)
            .into(bindingAd.adIconClone)

        bindingAd.adNameClone.text = adData.appName
        bindingAd.adBodyClone.text = adData.appDescription

        bindingAd.adCallToActionClone.text = activity.getString(R.string.install)

        val clickListener = View.OnClickListener {
            Log.e(EventLog, "ShimmerNTBNAd130_More_Click")
            MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_More_Click", Bundle())
            showAdClick(activity, adData.appLink.toString())
        }

        bindingAd.adIconClone.setOnClickListener(clickListener)
        bindingAd.adNameClone.setOnClickListener(clickListener)
        bindingAd.adBodyClone.setOnClickListener(clickListener)
        bindingAd.adCallToActionClone.setOnClickListener(clickListener)

        Log.e(EventLog, "ShimmerNTBNAd130_More_Show")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerNTBNAd130_More_Show", Bundle())
    }

}