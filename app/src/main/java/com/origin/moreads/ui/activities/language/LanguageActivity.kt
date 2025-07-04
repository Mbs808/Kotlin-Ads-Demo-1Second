package com.origin.moreads.ui.activities.language

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
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
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.origin.moreads.ads.adsload.AdsLoaded
import com.origin.moreads.ads.adsload.OnBoardingFirstAd
import com.origin.moreads.ads.adsload.OnBoardingFullAd
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.extensions.gone
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.extensions.visible
import com.origin.moreads.ui.activities.main.MainActivity
import com.origin.moreads.ui.activities.onboard.OnBoardingActivity
import com.origin.moreads.ui.adapters.LanguageAdapter
import com.origin.moreads.utils.IS_FROM
import com.origin.moreads.utils.SETTING_ACTIVITY
import com.origin.moreads.utils.Utils
import java.util.Locale

class LanguageActivity : BaseActivity() {

    /***** Toolbar *****/
    private var ivBack: ImageView? = null
    private var tvTitle: TextView? = null
    private var ivNext: ImageView? = null

    private var llContainer: LinearLayout? = null

    /***** MainView *****/
    private var rvLanguages: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    /***** AdView *****/
    private var clAdView: ConstraintLayout? = null

    /** Small Native Banner **/
    private var rlSmallNativeBanner: RelativeLayout? = null
    private var flSmallNativeBanner: FrameLayout? = null
    private var shimmerLayoutAd: ShimmerFrameLayout? = null

    /** Big Native **/
    private var rlBigNative: RelativeLayout? = null
    private var flBigTextSpace: FrameLayout? = null
    private var space: Space? = null
    private var flBigNative: FrameLayout? = null
    private var shimmerLayoutBigAd: ShimmerFrameLayout? = null

    /** Shimmer Included Layout **/
    private var shimmerAdMediaHolder: View? = null

    /***** Adapter *****/
    private var languageAdapter: LanguageAdapter? = null

    /***** Others *****/
    private var isFrom = ""
    private var mHeight = 0

    private var languageCode = ""
    private var isLanguageSelected = false


    private var upDownAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        /** get value from intent **/
        isFrom = intent.getStringExtra(IS_FROM).toString()

        /** get value from shared preference **/
        languageCode = prefsHelper.languageCode
        AdsConstant.isAdsClick = false

        initializeViews()
        prepareRVForLanguages()
        setOnClickListener()

        setAdView()

        if (AdsConstant.isShow_onBoardingScreen == "yes") {
            loadOnBoardingAds()
        }

        Log.e("Ads_Demo", "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())
    }

    private fun startAnimation() {
        // If an animation is already running, cancel it before starting a new one
        if (upDownAnimator != null && upDownAnimator!!.isRunning) {
            upDownAnimator!!.cancel()
        }

        val translateY = PropertyValuesHolder.ofFloat("translationY", 0f, -20f)
        upDownAnimator = ObjectAnimator.ofPropertyValuesHolder(llContainer, translateY)
        upDownAnimator?.setDuration(1000)
        upDownAnimator?.repeatCount = ObjectAnimator.INFINITE
        upDownAnimator?.repeatMode = ObjectAnimator.REVERSE
        upDownAnimator?.interpolator = LinearInterpolator()

        // Start the animation
        upDownAnimator?.start()
    }

    private fun stopAnimation() {
        if (llContainer != null) {
            llContainer?.visibility = View.GONE

            ivNext?.setImageResource(R.drawable.ic_next)
            // Stop the animation when the view is hidden
            if (upDownAnimator != null && upDownAnimator!!.isRunning) {
                upDownAnimator!!.cancel()
            }
        }
    }

    private fun loadOnBoardingAds() {
        //preload native onboarding
        if (AdsConstant.isShow_onBoarding_1Ads == "yes") {
            if (!OnBoardingFirstAd.isLoadingInOnBoarding) {
                OnBoardingFirstAd.isLoadingInLang = true
                OnBoardingFirstAd.loadGoogleNativeAd(
                    this,
                    AdsConstant.onBoarding_1_BigNative
                ) { nativeAd ->
                    if (OnBoardingFirstAd.onB1NativeAds != null) {
                        OnBoardingFirstAd.onB1NativeAds?.destroy()
                    }
                    OnBoardingFirstAd.onB1NativeAds = nativeAd
                    OnBoardingFirstAd.isOnB1LoadingMutableLiveData.value = nativeAd != null
                }
            }
        }

        if (AdsConstant.isShow_onBoarding_FullAds == "yes") {
            if (!OnBoardingFullAd.isLoadingInOnBoarding) {
                OnBoardingFullAd.isLoadingInLang = true
                OnBoardingFullAd.loadGoogleNativeAd(
                    this,
                    AdsConstant.onBoarding_Full_BigNative
                ) { nativeAd ->
                    if (OnBoardingFullAd.onBFullNativeAds != null) {
                        OnBoardingFullAd.onBFullNativeAds?.destroy()
                    }
                    OnBoardingFullAd.onBFullNativeAds = nativeAd
                    OnBoardingFullAd.isOnBFullLoadingMutableLiveData.value = nativeAd != null
                }
            }
        }
    }

    private fun initializeViews() {
        /***** Toolbar *****/
        ivBack = findViewById(R.id.ivBack)
        tvTitle = findViewById(R.id.tvTitle)
        ivNext = findViewById(R.id.ivNext)

        llContainer = findViewById(R.id.ll_container)

        /***** MainView *****/
        rvLanguages = findViewById(R.id.rvLanguages)
        progressBar = findViewById(R.id.progressBar)

        /***** AdView *****/
        clAdView = findViewById(R.id.clAdView)
        /** Small Native Banner **/
        rlSmallNativeBanner = findViewById(R.id.rlSmallNativeBanner)
        flSmallNativeBanner = findViewById(R.id.flSmallNativeBanner)
        shimmerLayoutAd = findViewById(R.id.shimmerLayoutAd)
        /** Big Native **/
        rlBigNative = findViewById(R.id.rlBigNative)
        flBigTextSpace = findViewById(R.id.flBigTextSpace)
        space = findViewById(R.id.space)
        flBigNative = findViewById(R.id.flBigNative)
        shimmerLayoutBigAd = findViewById(R.id.shimmerLayoutBigAd)
        shimmerAdMediaHolder = findViewById(R.id.shimmerAdMediaHolder)
        /***** Toolbar View Setup *****/
        when (isFrom) {
            SETTING_ACTIVITY -> {
                clAdView?.gone()
                ivBack?.visible()
                val params = tvTitle?.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(15, 0, 15, 0)
                tvTitle?.layoutParams = params
                rvLanguages?.clipToPadding = false
                rvLanguages?.setPadding(0, 0, 0, 0)

            }

            else -> {
                clAdView?.visible()
                ivBack?.gone()
                val params = tvTitle?.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, 0, 0, 0)
                tvTitle?.layoutParams = params
                rvLanguages?.clipToPadding = false
                rvLanguages?.setPadding(0, 0, 0, 150)
            }
        }
    }

    private fun prepareRVForLanguages() {
        languageAdapter = LanguageAdapter(
            context = this,
            onClick = {
                isLanguageSelected = true
                languageCode = it.languageCode
                stopAnimation()
            }
        )
        rvLanguages?.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
            adapter = languageAdapter
            setHasFixedSize(true)
        }
        languageAdapter?.languageCode = languageCode
        isLanguageSelected = true
        languageAdapter?.submitList(Utils.getLanguageList())
    }

    private fun setOnClickListener() {
        ivNext?.setOnClickListener {
            if (isLanguageSelected) {

                prefsHelper.languageCode = languageCode
                prefsHelper.isLanguageSelected = true
                pref(languageCode)
                intentToNextActivity()
            } else {
                Toast.makeText(this, R.string.select_language_first, Toast.LENGTH_SHORT).show()
            }
        }
        ivBack?.setOnClickListener { finish() }

        llContainer?.setOnClickListener {
            stopAnimation()
        }

    }

    override fun onResume() {
        super.onResume()
        Log.e("Ads_Demo", "${TAG}_onResume")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onResume", Bundle())

        if (AdsConstant.isAdsClick) {
            AdsConstant.isAdsClick = false
            if (llContainer != null) {
                llContainer?.visibility = View.VISIBLE
                ivNext?.setImageResource(R.drawable.ic_next2)
                startAnimation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e("Ads_Demo", "${TAG}_onDestroy")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onDestroy", Bundle())

        if (upDownAnimator != null && upDownAnimator!!.isRunning) {
            upDownAnimator!!.cancel()
        }

    }

    private fun intentToNextActivity() {
        val nextActivity =
            if (!prefsHelper.isOnBoardingDone && AdsConstant.isShow_onBoardingScreen == "yes") {
                OnBoardingActivity::class.java
            } else {
                MainActivity::class.java
            }
        startIntent(nextActivity)
        finish()
    }

    private fun backPressedEvent() {
        if (isFrom == SETTING_ACTIVITY) {
            finish()
        } else {
            intentToNextActivity()
        }
    }

    fun pref(commCode: String?) {
        val locale = Locale(commCode!!)
        Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                Log.e("Ads_Demo", "${TAG}_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_onBackPressed", Bundle())
                backPressedEvent()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setAdView() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mHeight = displayMetrics.heightPixels
        val params = space?.layoutParams
        params?.height = mHeight / 5
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        space?.layoutParams = params


        if (isFrom != SETTING_ACTIVITY) {
            Log.e("Ads_Demo", "${TAG}_startAdsLoad")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_startAdsLoad", Bundle())

            if (AdsConstant.showLanguageNativeAd == "yes") {
                if (AdsConstant.onlyShowMoreAppLanguage == "yes") {
                    if (AdsConstant.showBigNativeLanguage == "yes") {
                        rlBigNative?.visibility = View.VISIBLE
                        shimmerLayoutBigAd?.visibility = View.VISIBLE

                        val paramsShimmerMediaHolder = shimmerAdMediaHolder?.layoutParams
                        if (mHeight / 5 > 300) {
                            paramsShimmerMediaHolder?.height = mHeight / 5
                        } else {
                            paramsShimmerMediaHolder?.height = 300
                        }
                        paramsShimmerMediaHolder?.width = ViewGroup.LayoutParams.MATCH_PARENT
                        shimmerAdMediaHolder?.layoutParams = paramsShimmerMediaHolder


                        val params1 = space?.layoutParams
                        if (mHeight / 5 > 300) {
                            params1?.height = mHeight / 5
                        } else {
                            params1?.height = 300
                        }
                        params1?.width = ViewGroup.LayoutParams.MATCH_PARENT
                        space?.layoutParams = params1

                        if (AdsConstant.moreAppDataList.size > 0) {
                            loadMoreAppNativeAd(
                                activity = this,
                                frameLayout = flBigNative!!
                            )
                        }
                    } else {
                        rlSmallNativeBanner?.visibility = View.VISIBLE
                        shimmerLayoutAd?.visibility = View.VISIBLE
                        if (AdsConstant.moreAppDataList.size > 0) {
                            loadMoreAppNativeBannerAd(
                                activity = this,
                                frameLayout = flSmallNativeBanner!!,
                                shimmerLayout = shimmerLayoutAd!!
                            )
                        } else {
                            shimmerLayoutAd?.stopShimmer()
                        }
                    }
                } else {
                    AdsLoaded.isLanguageAdLoadingMutableLiveData.observe(this) { loadedFromSplash ->
                        if (AdsConstant.showBigNativeLanguage == "yes") {
                            rlBigNative?.visibility = View.VISIBLE
                            shimmerLayoutBigAd?.visibility = View.VISIBLE

                            val paramsShimmerMediaHolder = shimmerAdMediaHolder?.layoutParams
                            if (mHeight / 5 > 300) {
                                paramsShimmerMediaHolder?.height = mHeight / 5
                            } else {
                                paramsShimmerMediaHolder?.height = 300
                            }
                            paramsShimmerMediaHolder?.width = ViewGroup.LayoutParams.MATCH_PARENT
                            shimmerAdMediaHolder?.layoutParams = paramsShimmerMediaHolder

                            loadedFromSplash?.let {
                                if (it) {
                                    AdsLoaded.languageUnifiedNativeAds?.let {
                                        displayNativeAd(
                                            activity = this,
                                            frameLayout = flBigNative!!,
                                            shimmerLayoutAd = shimmerLayoutBigAd!!
                                        )
                                    }
                                } else {
                                    googleNativeAd(
                                        activity = this,
                                        adID = AdsConstant.nativeLanguageAds,
                                        shimmerLayoutAd = shimmerLayoutBigAd!!,
                                        frameLayout = flBigNative!!
                                    )
                                }
                            } ?: run {
                                if (!AdsLoaded.isLoadingInLanguage && !AdsLoaded.isLanguageLoadingInSplash) {
                                    AdsLoaded.isLoadingInLanguage = true
                                    googleNativeAd(
                                        activity = this,
                                        adID = AdsConstant.nativeLanguageAds,
                                        shimmerLayoutAd = shimmerLayoutBigAd!!,
                                        frameLayout = flBigNative!!
                                    )
                                }
                            }
                        } else {
                            rlSmallNativeBanner?.visibility = View.VISIBLE
                            shimmerLayoutAd?.visibility = View.VISIBLE
                            loadedFromSplash?.let {
                                if (it) {
                                    AdsLoaded.languageUnifiedNativeAds?.let {
                                        displayNativeAd(
                                            activity = this,
                                            frameLayout = flSmallNativeBanner!!,
                                            shimmerLayoutAd = shimmerLayoutAd!!
                                        )
                                    }
                                } else {
                                    googleNativeBannerAd(
                                        activity = this,
                                        adID = AdsConstant.nativeBannerLanguageAds,
                                        shimmerLayoutAd = shimmerLayoutAd!!,
                                        frameLayout = flSmallNativeBanner!!
                                    )
                                }
                            } ?: run {
                                if (!AdsLoaded.isLoadingInLanguage && !AdsLoaded.isLanguageLoadingInSplash) {
                                    AdsLoaded.isLoadingInLanguage = true
                                    googleNativeBannerAd(
                                        activity = this,
                                        adID = AdsConstant.nativeBannerLanguageAds,
                                        shimmerLayoutAd = shimmerLayoutAd!!,
                                        frameLayout = flSmallNativeBanner!!
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun displayNativeAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayoutAd: ShimmerFrameLayout
    ) {
        Log.e("Ads_Demo", "${TAG}_displayNativeAd")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_displayNativeAd", Bundle())

        shimmerLayoutAd.visibility = View.GONE

        val adView = if (AdsConstant.showBigNativeLanguage == "yes") {
            activity.layoutInflater.inflate(
                R.layout.google_native_ad_view,
                activity.findViewById(R.id.nativeAd),
                false
            ) as NativeAdView
        } else {
            activity.layoutInflater.inflate(
                R.layout.google_native_banner_ad_view_130,
                activity.findViewById(R.id.nativeAd),
                false
            ) as NativeAdView
        }

        AdsLoaded.languageUnifiedNativeAds?.let {
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

        if (AdsConstant.showBigNativeLanguage == "yes") {
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
        }

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

    private fun getAddRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    private fun googleNativeAd(
        activity: Activity,
        adID: String,
        shimmerLayoutAd: ShimmerFrameLayout,
        frameLayout: FrameLayout
    ) {
        Log.e("Ads_Demo", "${TAG}_start_load_NativeAd")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_start_load_NativeAd", Bundle())

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            AdsLoaded.languageUnifiedNativeAds = nativeAd
            shimmerLayoutAd.visibility = View.GONE
            displayNativeAd(activity, frameLayout, shimmerLayoutAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("Ads_Demo", "${TAG}_NativeAd_fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_NativeAd_fail", Bundle())

                AdsLoaded.languageUnifiedNativeAds = null

                if (AdsConstant.showMoreAppLanguage == "yes") {
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
                Log.e("Ads_Demo", "${TAG}_NativeAd_Loaded")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_NativeAd_Loaded", Bundle())

                shimmerLayoutAd.visibility = View.GONE
            }

            override fun onAdClicked() {
                Log.e("Ads_Demo", "${TAG}_NativeAd_Clicked")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_NativeAd_Clicked", Bundle())

                AdsConstant.isAdsClick = true
                AdsLoaded.languageUnifiedNativeAds = null
            }
        }).build()

        val request = getAddRequest()
        adLoader.loadAd(request)
    }

    private fun googleNativeBannerAd(
        activity: Activity,
        adID: String,
        shimmerLayoutAd: ShimmerFrameLayout,
        frameLayout: FrameLayout
    ) {
        Log.e("Ads_Demo", "${TAG}_NativeBanner_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_NativeBanner_LoadStart", Bundle())

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            AdsLoaded.languageUnifiedNativeAds = nativeAd
            shimmerLayoutAd.visibility = View.GONE
            displayNativeAd(activity, frameLayout, shimmerLayoutAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("Ads_Demo", "${TAG}_NativeBanner_Fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_NativeBanner_Fail", Bundle())

                AdsLoaded.languageUnifiedNativeAds = null
                if (AdsConstant.showMoreAppLanguage == "yes") {
                    if (AdsConstant.moreAppDataList.size > 0) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeBannerAd(activity, frameLayout, shimmerLayoutAd)
                        }
                    } else {
                        shimmerLayoutAd.stopShimmer()
                        shimmerLayoutAd.visibility = View.VISIBLE
                    }
                } else {
                    shimmerLayoutAd.stopShimmer()
                    shimmerLayoutAd.visibility = View.VISIBLE
                }
            }

            override fun onAdLoaded() {
                Log.e("Ads_Demo", "${TAG}_NativeBanner_Loaded")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_NativeBanner_Loaded", Bundle())
                shimmerLayoutAd.visibility = View.GONE
            }

            override fun onAdClicked() {
                Log.e("Ads_Demo", "${TAG}_NativeBanner_Clicked")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_NativeBanner_Clicked", Bundle())

                AdsConstant.isAdsClick = true
                AdsLoaded.languageUnifiedNativeAds = null
            }
        }).build()

        val request = getAddRequest()
        adLoader.loadAd(request)
    }

    private fun loadMoreAppNativeAd(
        activity: Activity,
        frameLayout: FrameLayout
    ) {

        Log.e("Ads_Demo", "${TAG}_MoreNativeAd_Load")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeAd_Load", Bundle())

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
            Log.e("Ads_Demo", "${TAG}_MoreNativeAd_Click")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeAd_Click", Bundle())

            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adCallToActionClone.setOnClickListener {
            Log.e("Ads_Demo", "${TAG}_MoreNativeAd_Click")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeAd_Click", Bundle())

            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        Log.e("Ads_Demo", "${TAG}_MoreNativeAd_show")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeAd_show", Bundle())

    }

    private fun loadMoreAppNativeBannerAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        Log.e("Ads_Demo", "${TAG}_MoreNativeBanner_Load")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeBanner_Load", Bundle())

        shimmerLayout.visibility = View.GONE
        val view = activity.layoutInflater.inflate(
            R.layout.google_native_banner_ad_view_130_clone,
            activity.findViewById(R.id.nativeAd), false
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
            Log.e("Ads_Demo", "${TAG}_MoreNativeBanner_Click")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeBanner_Click", Bundle())

            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adNameClone.setOnClickListener {
            Log.e("Ads_Demo", "${TAG}_MoreNativeBanner_Click")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeBanner_Click", Bundle())

            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adBodyClone.setOnClickListener {
            Log.e("Ads_Demo", "${TAG}_MoreNativeBanner_Click")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeBanner_Click", Bundle())

            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adCallToActionClone.setOnClickListener {
            Log.e("Ads_Demo", "${TAG}_MoreNativeBanner_Click")
            MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeBanner_Click", Bundle())

            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        Log.e("Ads_Demo", "${TAG}_MoreNativeBanner_Show")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_MoreNativeBanner_Show", Bundle())
    }

    private fun showAdClick(activity: Activity, link: String) {
        AdsConstant.isAdsClick = true
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        }
    }

    companion object {
        private const val TAG = "LanguageAct"
    }

}