package com.origin.moreads.ui.activities.ads.shimmer.bannerad

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.extensions.gone
import com.origin.moreads.ui.activities.language.BaseActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class ShimmerBannerAdActivity : BaseActivity() {

    /***** AdView *****/
    private var clAdView: ConstraintLayout? = null
    /** Banner **/
    private var rlBanner: RelativeLayout? = null
    private var shimmerLayoutAd: ShimmerFrameLayout? = null
    private var flBanner: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shimmer_banner_ad)

        initializeViews()
        setAdView()

        Log.e(LOG_TAG, "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())
    }

    private fun initializeViews() {
        /***** AdView *****/
        clAdView = findViewById(R.id.clAdView)
        /** Banner **/
        rlBanner = findViewById(R.id.rlBanner)
        shimmerLayoutAd = findViewById(R.id.shimmerLayoutAd)
        flBanner = findViewById(R.id.flBanner)
    }

    private fun setAdView() {
        if (AdsConstant.showBannerShimmer == "yes") {
            if (AdsConstant.onlyShowMoreAppBanner == "yes") {
                if (AdsConstant.moreAppDataList.size > 0) {
                    Log.e(LOG_TAG, "Loaded_More_App_Banner")
                    loadMoreAppBannerAd(
                        activity = this,
                        frameLayout = flBanner!!,
                        shimmerLayout = shimmerLayoutAd!!
                    )
                } else {
                    shimmerLayoutAd?.stopShimmer()
                }
            } else {
                Log.e(LOG_TAG, "Loaded_Banner")
                googleBannerAd(
                    activity = this,
                    adID = AdsConstant.bannerAds,
                    frameLayout = flBanner!!,
                    shimmerLayout = shimmerLayoutAd!!
                )
            }
        }  else {
            rlBanner?.gone()
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
        val adViewGoogle = AdView(activity)
        adViewGoogle.adUnitId = adID
        frameLayout.addView(adViewGoogle)
        val adSize = getAdSize(activity, frameLayout)

        adViewGoogle.setAdSize(adSize)

        val request = getAddRequest()
        adViewGoogle.loadAd(request)
        adViewGoogle.adListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(LOG_TAG, "${TAG}_Banner_onAdFailedToLoad$loadAdError")
                if (AdsConstant.showMoreAppBanner == "yes") {
                    if (AdsConstant.moreAppDataList.size > 0) {
                        if (!activity.isFinishing) {
                            loadMoreAppBannerAd(
                                activity,
                                frameLayout,
                                shimmerLayout
                            )
                        }
                    }else {
                        shimmerLayoutAd?.stopShimmer()
                    }
                }else{
                    shimmerLayoutAd?.stopShimmer()
                }

            }

            override fun onAdLoaded() {
                Log.e(LOG_TAG, "${TAG}_Banner_onAdLoaded")
                frameLayout.visibility = View.VISIBLE
                shimmerLayout.visibility = View.GONE
            }

            override fun onAdClicked() {
                Log.e(LOG_TAG, "${TAG}_Banner_onAdClicked")
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
        shimmerLayout.visibility = View.GONE
        val view = activity.layoutInflater.inflate(
            R.layout.google_banner_ad_view_clone,
            activity.findViewById(R.id.bannerAd),
            false
        )
        val adIconClone = view.findViewById<ImageView>(R.id.adIconClone)
        val adNameClone = view.findViewById<TextView>(R.id.adNameClone)
        val adBodyClone = view.findViewById<TextView>(R.id.adBodyClone)
        val adCallToActionClone = view.findViewById<TextView>(R.id.adCallToActionClone)
        frameLayout.removeAllViews()
        frameLayout.addView(view)

        AdsConstant.adCounter += 1

        if (AdsConstant.moreAppDataList.size == AdsConstant.adCounter) {
            AdsConstant.adCounter = 0
        }

        val number = AdsConstant.adCounter

        Glide.with(activity.applicationContext)
            .asBitmap()
            .load(AdsConstant.moreAppDataList[number].appIcon)
            .into(adIconClone)
        adNameClone.text = AdsConstant.moreAppDataList[number].appName
        adBodyClone.text = AdsConstant.moreAppDataList[number].appDescription

        adCallToActionClone.text = activity.getString(R.string.install)

        adIconClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adNameClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adBodyClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adCallToActionClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }
        Log.e(LOG_TAG, "MoreAppBannerAd_show")
    }

    private fun showAdClick(activity: Activity, link: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
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
        private const val LOG_TAG = "ShimmerBannerAdActivity"
        private const val TAG = "ShimmerBNRAd"
    }

}