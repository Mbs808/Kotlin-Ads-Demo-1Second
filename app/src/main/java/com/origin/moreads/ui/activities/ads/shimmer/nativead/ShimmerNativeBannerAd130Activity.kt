package com.origin.moreads.ui.activities.ads.shimmer.nativead

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class ShimmerNativeBannerAd130Activity : BaseActivity() {

    /***** AdView *****/
    private var clAdView: ConstraintLayout? = null

    /** NativeBannerAd **/
    private var rlNativeBanner: RelativeLayout? = null
    private var shimmerLayoutAd: ShimmerFrameLayout? = null
    private var flNativeBanner: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shimmer_native_banner_ad130)

        initializeViews()
        setAdView()

        Log.e(LOG_TAG, "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())
    }

    private fun initializeViews() {
        /***** AdView *****/
        clAdView = findViewById(R.id.clAdView)
        /** NativeBannerAd **/
        rlNativeBanner = findViewById(R.id.rlNativeBanner)
        shimmerLayoutAd = findViewById(R.id.shimmerLayoutAd)
        flNativeBanner = findViewById(R.id.flNativeBanner)
    }

    private fun setAdView() {
        if (AdsConstant.showNativeBannerShimmer130 == "yes") {
            if (AdsConstant.onlyShowMoreAppNativeBanner == "yes") {
                if (AdsConstant.moreAppDataList.size > 0) {
                    loadMoreAppNativeBannerAd(
                        activity = this,
                        frameLayout = flNativeBanner!!,
                        shimmerLayout = shimmerLayoutAd!!
                    )
                } else {
                    shimmerLayoutAd?.stopShimmer()
                }
            } else {
                googleNativeBannerAd(
                    activity = this,
                    adID = AdsConstant.nativeBannerAds,
                    frameLayout = flNativeBanner!!,
                    shimmerLayout = shimmerLayoutAd!!
                )
            }
        } else {
            rlNativeBanner?.gone()
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
        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            Log.e(LOG_TAG, "${TAG}_googleNativeBannerAd_onAdLoaded")
            shimmerLayout.visibility = View.GONE
            showNativeBanner(activity, frameLayout, shimmerLayout, nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(LOG_TAG, "${TAG}_googleNativeBannerAd_onAdFailedToLoad$loadAdError")
                if (AdsConstant.showMoreAppNativeBanner == "yes") {
                    if (AdsConstant.moreAppDataList.size > 0) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeBannerAd(
                                activity,
                                frameLayout,
                                shimmerLayout
                            )
                        }
                    } else {
                        shimmerLayoutAd?.stopShimmer()
                    }
                } else {
                    shimmerLayoutAd?.stopShimmer()
                }
            }

            override fun onAdLoaded() {
                shimmerLayout.visibility = View.GONE
            }

            override fun onAdClicked() {
                Log.e(LOG_TAG, "${TAG}_googleNativeBannerAd_onAdClicked")
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
        shimmerLayout.visibility = View.GONE

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
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            adView.iconView?.visibility = View.VISIBLE
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        adView.setNativeAd(nativeAd)
    }

    private fun loadMoreAppNativeBannerAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        shimmerLayout.visibility = View.GONE
        val view = activity.layoutInflater.inflate(
            R.layout.google_native_banner_ad_view_130_clone,
            activity.findViewById(R.id.nativeAd),
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
            Log.e(LOG_TAG, "MoreAppNativeBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adNameClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppNativeBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adBodyClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppNativeBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adCallToActionClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppNativeBannerAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }
        Log.e(LOG_TAG, "MoreAppNativeBannerAd_load")
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e(LOG_TAG, "${TAG}_onBackPressed")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_onBackPressed", Bundle())
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val LOG_TAG = "ShimmerNativeBannerAd130Activity"
        private const val TAG = "ShimmerNTBNAd130"
    }

}