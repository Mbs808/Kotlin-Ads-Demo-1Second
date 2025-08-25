package com.origin.moreads.ui.activities.ads.shimmer.bannerad

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.databinding.ActivityShimmerBannerAdBinding
import com.origin.moreads.databinding.GoogleBannerAdViewCloneBinding
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.utils.EventLog
import com.origin.moreads.utils.setGone
import com.origin.moreads.utils.setVisible
import com.origin.moreads.utils.showAdClick

class ShimmerBannerAdActivity : BaseActivity() {

    private lateinit var binding: ActivityShimmerBannerAdBinding

    var isAdsClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShimmerBannerAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!isGestureNavigationEnabled(this)) {
            hideNavigationBar(window)
        }

        setAdView()

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e("EventLog", "ShimmerBNRAd_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_onBackPressed", Bundle())
                finish()
            }
        })

        Log.e("EventLog", "ShimmerBNRAd_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_onCreate", Bundle())
    }

    private fun isGestureNavigationEnabled(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val value = Settings.Secure.getInt(
                    context.contentResolver,
                    "navigation_mode"
                )
                value == 2
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun hideNavigationBar(window: Window?) {
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)

            val windowInsetsController = WindowCompat.getInsetsController(it, it.decorView)
            windowInsetsController.let { controller ->
                //  Hide the navigation bar
                controller.hide(WindowInsetsCompat.Type.navigationBars())

                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                it.statusBarColor = Color.TRANSPARENT

                // true for light status bar (dark icons), false for dark status bar (light icons)
                controller.isAppearanceLightStatusBars = true
            }
        }
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

        Log.e(EventLog, "ShimmerBNRAd_onDestroy")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_onDestroy", Bundle())
    }


    override fun onResume() {
        super.onResume()

        Log.e(EventLog, "ShimmerBNRAd_onResume")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_onResume", Bundle())

        if (isAdsClicked) {
            isAdsClicked = false
            if (!isGestureNavigationEnabled(this@ShimmerBannerAdActivity)) {
                hideNavigationBar(window)
            }
        }
    }

    private fun setAdView() {
        if (AdsConstant.isConnected(this) && AdsConstant.showBannerShimmer == "yes" ) {
            if (AdsConstant.onlyShowMoreAppBanner == "yes") {
                if (AdsConstant.moreAppDataList.isNotEmpty()) {
                    loadMoreAppBannerAd(
                        activity = this,
                        frameLayout = binding.flBanner,
                        shimmerLayout = binding.shimmerLayoutAd
                    )
                } else {
                    binding.shimmerLayoutAd.stopShimmer()
                }
            } else {
                googleBannerAd(
                    activity = this,
                    adID = AdsConstant.bannerAds,
                    frameLayout = binding.flBanner,
                    shimmerLayout = binding.shimmerLayoutAd
                )
            }
        } else {
            binding.rlBanner.setGone()
        }
    }

    private fun getAddRequest(): AdRequest {
        val extras = Bundle().apply {
            putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        }
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun googleBannerAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        Log.e(EventLog, "ShimmerBNRAd_BannerLoadStart")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_BannerLoadStart", Bundle())

        val adViewGoogle = AdView(activity)
        adViewGoogle.adUnitId = adID
        frameLayout.addView(adViewGoogle)
        val adSize = getAdSize(activity, frameLayout)

        adViewGoogle.setAdSize(adSize)

        val request = getAddRequest()
        adViewGoogle.loadAd(request)
        adViewGoogle.adListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(EventLog, "ShimmerBNRAd_Banner_fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_Banner_fail", Bundle())

                if (AdsConstant.showMoreAppBanner == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        if (!activity.isFinishing) {
                            loadMoreAppBannerAd(
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
                Log.e(EventLog, "ShimmerBNRAd_Banner_Loaded")
                MainApplication.firebaseAnalytics?.logEvent(
                    "ShimmerBNRAd_Banner_Loaded",
                    Bundle()
                )

                frameLayout.setVisible()
                shimmerLayout.stopShimmer()
                shimmerLayout.setGone()
            }

            override fun onAdClicked() {
                Log.e(EventLog, "ShimmerBNRAd_Banner_Clicked")
                MainApplication.firebaseAnalytics?.logEvent(
                    "ShimmerBNRAd_Banner_Clicked",
                    Bundle()
                )

                isAdsClicked = true
                googleBannerAd(activity, adID, frameLayout, shimmerLayout)

            }
        }
    }

    private fun getAdSize(
        activity: Activity,
        frameLayout: FrameLayout
    ): AdSize {

        val display: Display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = frameLayout.width

        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels
        }

        val adWidth = (adWidthPixels / density).toInt()

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    private fun loadMoreAppBannerAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        Log.e(EventLog, "ShimmerBNRAd_MoreBanner_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_MoreBanner_LoadStart", Bundle())

        // Hide shimmer layout safely
        shimmerLayout.setGone()

        // Defensive checks
        if (activity.isFinishing || activity.isDestroyed) return
        if (AdsConstant.moreAppDataList.isEmpty()) return

        val binding = GoogleBannerAdViewCloneBinding.inflate(activity.layoutInflater)
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
        binding.adCallToActionClone.text = activity.getString(R.string.install)

        val clickListener = View.OnClickListener {
            Log.e(EventLog, "ShimmerBNRAd_MoreBanner_Click")
            MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_MoreBanner_Click", Bundle())
            showAdClick(activity, adData.appLink ?: "")
        }

        binding.adIconClone.setOnClickListener(clickListener)
        binding.adNameClone.setOnClickListener(clickListener)
        binding.adBodyClone.setOnClickListener(clickListener)
        binding.adCallToActionClone.setOnClickListener(clickListener)

        Log.e(EventLog, "ShimmerBNRAd_MoreBanner_Show")
        MainApplication.firebaseAnalytics?.logEvent("ShimmerBNRAd_MoreBanner_Show", Bundle())

    }

}