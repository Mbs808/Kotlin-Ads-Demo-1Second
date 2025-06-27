package com.origin.moreads.ui.activities.onboard

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
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
import com.origin.moreads.R
import com.origin.moreads.ads.adsload.OnBoardingSecondAd
import com.origin.moreads.ads.utils.AdsConstant

class Fragment4 : Fragment() {

    /** Big Native **/
    private var rlBigNative: RelativeLayout? = null
    private var flBigNative: FrameLayout? = null
    private var shimmerLayoutBigAd: ShimmerFrameLayout? = null

    /** Shimmer Included Layout **/
    private var shimmerAdMediaHolder: View? = null

    private var tvNext: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_4, container, false)

        /** Big Native **/
        rlBigNative = view.findViewById(R.id.rlNative)
        flBigNative = view.findViewById(R.id.flNative)
        shimmerLayoutBigAd = view.findViewById(R.id.shimmerLayoutAd)
        shimmerAdMediaHolder = view.findViewById(R.id.shimmerAdMediaHolder)
        tvNext = view.findViewById(R.id.tvNext)

        tvNext?.setOnClickListener {
            (activity as? OnBoardingActivity)?.moveToNextPage()
        }

        if (AdsConstant.isShow_onBoarding_2Ads == "yes") {
            loadAds()
        } else {
            rlBigNative?.visibility = View.GONE
        }

        Log.e("Tag", "onCreateView::::----111------ ", )

        return view
    }

    override fun onResume() {
        super.onResume()

        Log.e("Tag", "onCreateView::::----222------ ", )

    }
    private fun loadAds() {

        if (AdsConstant.onlyShowMoreAppNative == "yes") {
            if (AdsConstant.moreAppDataList.size > 0) {
                loadMoreAppNativeAd(
                    activity = requireActivity(),
                    frameLayout = flBigNative!!
                )
            }
        } else {
            OnBoardingSecondAd.isOnB2LoadingMutableLiveData.observe(requireActivity()) { loadedFromSplash ->
                loadedFromSplash?.let {
                    if (it) {
                        OnBoardingSecondAd.onB2NativeAds?.let {
                            showNativeBanner(
                                activity = requireActivity(),
                                frameLayout = flBigNative!!,
                                shimmerLayoutAd = shimmerLayoutBigAd!!
                            )
                        }
                    } else {
                        googleNativeAdFirst(
                            activity = requireActivity(),
                            adID = AdsConstant.onBoarding_2_BigNative,
                            shimmerLayoutAd = shimmerLayoutBigAd!!,
                            frameLayout = flBigNative!!
                        )
                    }
                } ?: run {
                    if (!OnBoardingSecondAd.isLoadingInOnBoarding && !OnBoardingSecondAd.isLoadingInCurrentAct) {
                        OnBoardingSecondAd.isLoadingInOnBoarding = true
                        googleNativeAdFirst(
                            activity = requireActivity(),
                            adID = AdsConstant.onBoarding_2_BigNative,
                            shimmerLayoutAd = shimmerLayoutBigAd!!,
                            frameLayout = flBigNative!!
                        )
                    }
                }
            }
        }
    }


    private fun googleNativeAdFirst(
        activity: Activity,
        adID: String,
        shimmerLayoutAd: ShimmerFrameLayout,
        frameLayout: FrameLayout
    ) {

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            OnBoardingSecondAd.onB2NativeAds = nativeAd
            shimmerLayoutAd.visibility = View.GONE
            showNativeBanner(activity, frameLayout, shimmerLayoutAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                OnBoardingSecondAd.onB2NativeAds = null

                if (AdsConstant.showMoreAppNative == "yes") {
                    if (AdsConstant.moreAppDataList.size > 0) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeAd(activity, frameLayout)
                        }
                    } else {
                        shimmerLayoutAd.visibility = View.VISIBLE
                        shimmerLayoutAd.stopShimmer()
                    }
                } else {
                    shimmerLayoutAd.visibility = View.VISIBLE
                    shimmerLayoutAd.stopShimmer()
                }
            }

            override fun onAdLoaded() {

                shimmerLayoutAd.visibility = View.GONE
            }

            override fun onAdClicked() {
                OnBoardingSecondAd.onB2NativeAds = null
            }
        }).build()

        val request = getAddRequest()
        adLoader.loadAd(request)
    }

    private fun getAddRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    private fun showNativeBanner(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayoutAd: ShimmerFrameLayout
    ) {

        shimmerLayoutAd.visibility = View.GONE

        val adView = activity.layoutInflater.inflate(
            R.layout.google_native_ad_view_onb,
            activity.findViewById(R.id.nativeAd),
            false
        ) as NativeAdView

        OnBoardingSecondAd.onB2NativeAds?.let {
            populateAppInstallAdView(it, adView)
            frameLayout.removeAllViews()
            frameLayout.addView(adView)
        }
    }

    private fun populateAppInstallAdView(
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {

        adView.iconView = adView.findViewById(R.id.adIcon)
        adView.headlineView = adView.findViewById(R.id.adName)
        adView.bodyView = adView.findViewById(R.id.adBody)

        val mediaView = adView.findViewById<MediaView>(R.id.adMedia)

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
        frameLayout: FrameLayout
    ) {
        val view = activity.layoutInflater.inflate(
            R.layout.google_native_ad_view_clone_onb,
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


        Glide.with(activity.applicationContext)
            .asBitmap()
            .load(AdsConstant.moreAppDataList[number].appBanner)
            .into(adMediaClone)

        adCallToActionClone.text = activity.getString(R.string.install)

        adMediaClone.setOnClickListener {
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adCallToActionClone.setOnClickListener {
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

    }

    private fun showAdClick(activity: Activity, link: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        }
    }


    companion object {

    }
}