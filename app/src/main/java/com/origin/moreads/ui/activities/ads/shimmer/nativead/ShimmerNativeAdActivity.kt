package com.origin.moreads.ui.activities.ads.shimmer.nativead

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.databinding.ActivityShimmerNativeAdBinding
import com.origin.moreads.databinding.GoogleNativeAdViewCloneBinding
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.utils.EventLog
import com.origin.moreads.utils.setGone
import com.origin.moreads.utils.setInvisible
import com.origin.moreads.utils.setVisible
import com.origin.moreads.utils.showAdClick

class ShimmerNativeAdActivity : BaseActivity() {

    private lateinit var binding: ActivityShimmerNativeAdBinding
    private val TAG = "ShimmerNativeAdAct"

    /** Others **/
    private var mHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShimmerNativeAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAdView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "ShimmerNTVAd_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("ShimmerNTVAd_onBackPressed", Bundle())
                finish()
            }
        })

        Log.e(EventLog, "ShimmerNTVAd_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerNTVAd_onCreate", Bundle())
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

        Log.e(TAG, "ShimmerNTVAd_onDestroy")
    }


    private fun setAdView() {
        mHeight = resources.displayMetrics.heightPixels

        val params = binding.nativeShimmer.shimmerAdMediaHolder.layoutParams
        params.height = maxOf(mHeight / 5, 300)
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        binding.nativeShimmer.shimmerAdMediaHolder.layoutParams = params

        if (AdsConstant.isConnected(this) && AdsConstant.showNativeShimmer == "yes") {
            if (AdsConstant.onlyShowMoreAppNative == "yes") {
                if (AdsConstant.moreAppDataList.isNotEmpty()) {
                    loadMoreAppNativeAd(
                        activity = this,
                        frameLayout = binding.flNative,
                        shimmerLayout = binding.shimmerLayoutAd
                    )
                } else {
                    binding.shimmerLayoutAd.post {
                        binding.shimmerLayoutAd.stopShimmer()
                    }
                }
            } else {
                googleNativeAd(
                    activity = this,
                    adID = AdsConstant.nativeAds,
                    frameLayout = binding.flNative,
                    shimmerLayout = binding.shimmerLayoutAd
                )
            }
        } else {
            binding.rlNative.setGone()
        }
    }


    fun googleNativeAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        Log.e(TAG, "ShimmerNTVAd_NativeAd_LoadStart")

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            shimmerLayout.setGone()
            showNativeBanner(activity, frameLayout, shimmerLayout, nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "ShimmerNTVAd_NativeAd_Fail$loadAdError")

                if (AdsConstant.showMoreAppNative == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeAd(
                                activity,
                                frameLayout,
                                shimmerLayout,
                            )
                        }
                    } else {
                        binding.shimmerLayoutAd.post {
                            binding.shimmerLayoutAd.stopShimmer()
                        }
                    }
                } else {
                    binding.shimmerLayoutAd.post {
                        binding.shimmerLayoutAd.stopShimmer()
                    }
                }
            }

            override fun onAdLoaded() {
                Log.e(TAG, "ShimmerNTVAd_NativeAd_Loaded")

                shimmerLayout.setGone()
            }

            override fun onAdClicked() {
                Log.e(TAG, "ShimmerNTVAd_NativeAd_Clicked")

                googleNativeAd(activity, adID, frameLayout, shimmerLayout)
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun showNativeBanner(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout,
        nativeAd: NativeAd
    ) {

        Log.e(TAG, "ShimmerNTVAd_NativeAd_Show")

        shimmerLayout.setGone()

        val adView = activity.layoutInflater.inflate(
            R.layout.google_native_ad_view,
            activity.findViewById(R.id.nativeAd),
            false
        ) as NativeAdView

        populateAppInstallAdView(nativeAd, adView)
        frameLayout.removeAllViews()
        frameLayout.addView(adView)
    }

    private fun populateAppInstallAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.iconView = adView.findViewById(R.id.adIcon)
        adView.headlineView = adView.findViewById(R.id.adName)
        adView.bodyView = adView.findViewById(R.id.adBody)

        val mediaView = adView.findViewById<MediaView>(R.id.adMedia)

        // Adjust mediaView height
        mediaView?.layoutParams?.apply {
            height = maxOf(mHeight / 5, 300)
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                if (child is ImageView) {
                    child.adjustViewBounds = true
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {}
        })

        adView.mediaView = mediaView

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

    private fun loadMoreAppNativeAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        try {
            Log.d(EventLog, "ShimmerNTVAd_MoreNative_Load")
            MainApplication.firebaseAnalytics?.logEvent("ShimmerNTVAd_MoreNative_Load", Bundle())

            // Hide shimmer layout safely
            shimmerLayout.setGone()

            // Defensive checks
            if (activity.isFinishing || activity.isDestroyed) return
            if (AdsConstant.moreAppDataList.isEmpty()) return


            val binding = GoogleNativeAdViewCloneBinding.inflate(activity.layoutInflater)
            frameLayout.removeAllViews()
            frameLayout.addView(binding.root)

            AdsConstant.adCounter += 1
            if (AdsConstant.moreAppDataList.size == AdsConstant.adCounter) {
                AdsConstant.adCounter = 0
            }

            val number = AdsConstant.adCounter
            val adData = AdsConstant.moreAppDataList[number]

            Glide.with(activity.applicationContext)
                .asBitmap()
                .load(adData.appIcon)
                .into(binding.adIconClone)

            binding.adNameClone.text = adData.appName
            binding.adBodyClone.text = adData.appDescription

            val mediaParams = binding.adMediaClone.layoutParams
            mediaParams.height = maxOf(mHeight / 5, 300)
            mediaParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.adMediaClone.layoutParams = mediaParams

            Glide.with(activity.applicationContext)
                .asBitmap()
                .load(adData.appBanner)
                .into(binding.adMediaClone)

            binding.adCallToActionClone.text = activity.getString(R.string.install)

            // Set click listeners for the views
            val clickListener = View.OnClickListener {
                Log.d(EventLog, "ShimmerNTVAd_MoreNativeAd_Click")
                MainApplication.firebaseAnalytics?.logEvent(
                    "ShimmerNTVAd_MoreNativeAd_Click",
                    Bundle()
                )
                showAdClick(activity, adData.appLink.toString())
            }

            binding.adMediaClone.setOnClickListener(clickListener)
            binding.adCallToActionClone.setOnClickListener(clickListener)

            Log.d(EventLog, "ShimmerNTVAd_MoreNativeAd_Show")
            MainApplication.firebaseAnalytics?.logEvent("ShimmerNTVAd_MoreNativeAd_Show", Bundle())
        } catch (e: Exception) {
            Log.e(TAG, "loadMoreAppNativeAd Exception: ${e.message}", e)
        }
    }
}