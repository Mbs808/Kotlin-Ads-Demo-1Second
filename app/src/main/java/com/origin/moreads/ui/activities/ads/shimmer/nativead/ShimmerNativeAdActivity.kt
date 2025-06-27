package com.origin.moreads.ui.activities.ads.shimmer.nativead

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
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
import com.origin.moreads.ui.activities.language.BaseActivity

class ShimmerNativeAdActivity : BaseActivity() {

    /***** AdView *****/
    private var clAdView: ConstraintLayout? = null

    /** NativeAd **/
    private var rlNative: RelativeLayout? = null
    private var shimmerLayoutAd: ShimmerFrameLayout? = null
    private var flNative: FrameLayout? = null

    /** Shimmer Included Layout **/
    private var shimmerAdMediaHolder: View? = null

    /** Others **/
    private var mHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shimmer_native_ad)

        initializeViews()
        setAdView()

        Log.e(LOG_TAG, "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())
    }

    private fun initializeViews() {
        /***** AdView *****/
        clAdView = findViewById(R.id.clAdView)
        /** NativeAd **/
        rlNative = findViewById(R.id.rlNative)
        shimmerLayoutAd = findViewById(R.id.shimmerLayoutAd)
        flNative = findViewById(R.id.flNative)
        /** Shimmer Included Layout **/
        shimmerAdMediaHolder = findViewById(R.id.shimmerAdMediaHolder)
    }

    private fun setAdView() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mHeight = displayMetrics.heightPixels

        val paramsShimmerMediaHolder = shimmerAdMediaHolder?.layoutParams
        if (mHeight / 5 > 300) {
            paramsShimmerMediaHolder?.height = mHeight / 5
        } else {
            paramsShimmerMediaHolder?.height = 300
        }
        paramsShimmerMediaHolder?.width = ViewGroup.LayoutParams.MATCH_PARENT
        shimmerAdMediaHolder?.layoutParams = paramsShimmerMediaHolder

        if (AdsConstant.showNativeShimmer == "yes") {
            if (AdsConstant.onlyShowMoreAppNative == "yes") {
                if (AdsConstant.moreAppDataList.size > 0) {
                    loadMoreAppNativeAd(
                        activity = this,
                        frameLayout = flNative!!,
                        shimmerLayout = shimmerLayoutAd!!
                    )
                } else {
                    shimmerLayoutAd?.stopShimmer()
                }
            } else {
                googleNativeAd(
                    activity = this,
                    adID = AdsConstant.nativeAds,
                    frameLayout = flNative!!,
                    shimmerLayout = shimmerLayoutAd!!
                )
            }
        } else {
            rlNative?.visibility = View.GONE
        }
    }

    private fun getAddRequest(): AdRequest {
        val extras = Bundle().apply {
            putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        }
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun googleNativeAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            Log.e(LOG_TAG, "${TAG}_Native_onAdLoaded")
            shimmerLayout.visibility = View.GONE
            showNativeBanner(activity, frameLayout, shimmerLayout, nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(LOG_TAG, "${TAG}_Native_onAdFailedToLoad_$loadAdError")
                if (AdsConstant.showMoreAppNative == "yes") {
                    if (AdsConstant.moreAppDataList.size > 0) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeAd(
                                activity,
                                frameLayout,
                                shimmerLayout,
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
                Log.e(LOG_TAG, "${TAG}_Native_onAdClicked")
                googleNativeAd(activity, adID, frameLayout, shimmerLayout)
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
            R.layout.google_native_ad_view,
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

        val mediaView = adView.findViewById<MediaView>(R.id.adMedia)
        val params = mediaView.layoutParams
        if (mHeight / 5 > 300) {
            params.height = mHeight / 5
        } else {
            params.height = 300
        }
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        mediaView.layoutParams = params
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

    private fun loadMoreAppNativeAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        shimmerLayout.visibility = View.GONE
        val view = activity.layoutInflater.inflate(
            R.layout.google_native_ad_view_clone,
            activity.findViewById(R.id.nativeAd),
            false
        )
        val adIconClone = view.findViewById<ImageView>(R.id.adIconClone)
        val adNameClone = view.findViewById<TextView>(R.id.adNameClone)
        val adBodyClone = view.findViewById<TextView>(R.id.adBodyClone)
        val adMediaClone = view.findViewById<ImageView>(R.id.adMediaClone)
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

        val adMediaCloneParam = adMediaClone.layoutParams
        if (mHeight / 5 > 300) {
            adMediaCloneParam.height = mHeight / 5
        } else {
            adMediaCloneParam.height = 300
        }
        adMediaCloneParam.width = ViewGroup.LayoutParams.MATCH_PARENT
        adMediaClone.layoutParams = adMediaCloneParam

        Glide.with(activity.applicationContext)
            .asBitmap()
            .load(AdsConstant.moreAppDataList[number].appBanner)
            .into(adMediaClone)

        adCallToActionClone.text = activity.getString(R.string.install)

        adMediaClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppNativeAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adCallToActionClone.setOnClickListener {
            Log.e(LOG_TAG, "MoreAppNativeAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }
        Log.e(TAG, "MoreAppNativeAd_load")
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
        private const val LOG_TAG = "ShimmerNativeAdActivity"
        private const val TAG = "ShimmerNTVAd"
    }

}