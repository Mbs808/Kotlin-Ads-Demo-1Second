package com.origin.moreads.ui.activities.language

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.origin.moreads.ads.adsload.PreviewLangAdsLoad
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.databinding.ActivityLanguageBinding
import com.origin.moreads.databinding.GoogleNativeAdViewCloneBinding
import com.origin.moreads.databinding.GoogleNativeBannerAdView130CloneBinding
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.main.MainActivity
import com.origin.moreads.ui.adapters.LanguageAdapter
import com.origin.moreads.utils.EventLog
import com.origin.moreads.utils.IS_FROM
import com.origin.moreads.utils.SETTING_ACTIVITY
import com.origin.moreads.utils.setGone
import com.origin.moreads.utils.setInvisible
import com.origin.moreads.utils.setVisible
import com.origin.moreads.utils.showAdClick
import java.util.Locale

class LanguageActivity : BaseActivity() {

    /***** Adapter *****/
    private var languageAdapter: LanguageAdapter? = null

    /***** Others *****/
    private var isFrom = ""
    private var mHeight = 0

    private var languageCode = ""
    private var isLanguageSelected = false

    private var upDownAnimator: ObjectAnimator? = null

    private lateinit var binding: ActivityLanguageBinding

    private val TAG = "lang_native--"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** get value from intent **/
        isFrom = intent.getStringExtra(IS_FROM).toString()

        /** get value from shared preference **/
        languageCode = prefsHelper.languageCode
        AdsConstant.isAdsClick = false

        initializeViews()
        prepareRVForLanguages()
        setOnClickListener()

        setAdView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "LanguageAct_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("LanguageAct_onBackPressed", Bundle())

                backPressedEvent()
            }
        })

        Log.e(EventLog, "LanguageAct_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("LanguageAct_onCreate", Bundle())

    }

    private fun startAnimation() {
        // If an animation is already running, cancel it before starting a new one
        if (upDownAnimator != null && upDownAnimator!!.isRunning) {
            upDownAnimator!!.cancel()
        }

        val translateY = PropertyValuesHolder.ofFloat("translationY", 0f, -20f)
        upDownAnimator = ObjectAnimator.ofPropertyValuesHolder(binding.llContainer, translateY)
        upDownAnimator?.setDuration(1000)
        upDownAnimator?.repeatCount = ObjectAnimator.INFINITE
        upDownAnimator?.repeatMode = ObjectAnimator.REVERSE
        upDownAnimator?.interpolator = LinearInterpolator()

        // Start the animation
        upDownAnimator?.start()
    }

    private fun stopAnimation() {
        binding.llContainer.setGone()

        binding.ivNext.setImageResource(R.drawable.ic_next)
        // Stop the animation when the view is hidden
        if (upDownAnimator != null && upDownAnimator!!.isRunning) {
            upDownAnimator!!.cancel()
        }
    }

    private fun initializeViews() {
        /***** Toolbar View Setup *****/
        when (isFrom) {
            SETTING_ACTIVITY -> {
                binding.clAdView.setGone()
                binding.ivBack.setVisible()
                val params = binding.tvTitle.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(15, 0, 15, 0)
                binding.tvTitle.layoutParams = params
                binding.rvLanguages.clipToPadding = false
                binding.rvLanguages.setPadding(0, 0, 0, 0)

            }

            else -> {
                binding.clAdView.setVisible()
                binding.ivBack.setGone()
                val params = binding.tvTitle.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, 0, 0, 0)
                binding.tvTitle.layoutParams = params
                binding.rvLanguages.clipToPadding = false
                binding.rvLanguages.setPadding(0, 0, 0, 150)
            }
        }
    }

    private fun prepareRVForLanguages() {
        languageAdapter = LanguageAdapter(
            onClick = {
                isLanguageSelected = true
                languageCode = it.languageCode
                stopAnimation()
            }
        )

        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
            adapter = languageAdapter
            setHasFixedSize(true)
        }
        languageAdapter?.languageCode = languageCode
        isLanguageSelected = true
        languageAdapter?.submitList(AdsConstant.getLanguageList())
    }

    private fun setOnClickListener() {
        binding.ivNext.setOnClickListener {
            if (isLanguageSelected) {

                prefsHelper.languageCode = languageCode
                prefsHelper.isLanguageSelected = true
                updateLocalePreference(languageCode)
                intentToNextActivity()
            } else {
                Toast.makeText(this, R.string.select_language_first, Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivBack.setOnClickListener { finish() }

        binding.llContainer.setOnClickListener {
            stopAnimation()
        }

    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "LanguageAct_onResume")

        if (AdsConstant.isAdsClick) {
            AdsConstant.isAdsClick = false
            binding.llContainer.setVisible()
            binding.ivNext.setImageResource(R.drawable.ic_next2)
            startAnimation()
        }
    }

    override fun onDestroy() {
        if (upDownAnimator != null && upDownAnimator!!.isRunning) {
            upDownAnimator!!.cancel()
        }
        super.onDestroy()

        Log.e(TAG, "LanguageAct_onDestroy")

    }

    private fun intentToNextActivity() {
        val nextActivity = MainActivity::class.java
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

    fun updateLocalePreference(commCode: String?) {
        if (commCode.isNullOrBlank()) return

        val locale = Locale(commCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        // Apply the locale update to the application context
        createConfigurationContext(config)
    }

    private fun setAdView() {
        mHeight = resources.displayMetrics.heightPixels

        if (isFrom != SETTING_ACTIVITY) {
            Log.e(TAG, "LanguageAct_setAdView")

            if (AdsConstant.isConnected(this@LanguageActivity) && AdsConstant.showLanguageNativeAd == "yes") {
                if (AdsConstant.onlyShowMoreAppLanguage == "yes") {
                    Log.e(TAG, "LanguageAct:-----onlyShowMoreApp-----")

                    if (AdsConstant.showBigNativeLanguage == "yes") {

                        binding.rlBigNative.setVisible()
                        binding.shimmerLayoutBigAd.setVisible()

                        binding.languageShimmer.shimmerAdMediaHolder.let { view ->
                            val params = view.layoutParams
                            val targetHeight = maxOf(mHeight / 5, 300)

                            params?.apply {
                                height = targetHeight
                                width = ViewGroup.LayoutParams.MATCH_PARENT
                                view.layoutParams = this
                            }
                        }
                        if (AdsConstant.moreAppDataList.isNotEmpty()) {
                            loadMoreAppNativeAd(
                                activity = this,
                                frameLayout = binding.flBigNative,
                                shimmerLayout = binding.shimmerLayoutBigAd
                            )
                        } else {
                            binding.shimmerLayoutBigAd.post {
                                binding.shimmerLayoutBigAd.stopShimmer()
                            }
                        }
                    } else {
                        binding.rlSmallNativeBanner.setVisible()
                        binding.shimmerLayoutAd.setVisible()

                        if (AdsConstant.moreAppDataList.isNotEmpty()) {
                            loadMoreAppNativeBannerAd(
                                activity = this,
                                frameLayout = binding.flSmallNativeBanner,
                                shimmerLayout = binding.shimmerLayoutAd
                            )
                        } else {
                            binding.shimmerLayoutAd.post {
                                binding.shimmerLayoutAd.stopShimmer()
                            }
                        }
                    }
                } else {
                    PreviewLangAdsLoad.isLanguageAdLoadingMutableLiveData.observe(this) { loadedFromSplash ->
                        if (AdsConstant.showBigNativeLanguage == "yes") {
                            Log.e(TAG, "LanguageAct:--showBigAd--------")

                            showBigAd(loadedFromSplash)
                        } else {
                            Log.e(TAG, "LanguageAct:--showSmallAd--------")
                            showSmallAd(loadedFromSplash)
                        }
                    }

                }
            } else {
                binding.rlSmallNativeBanner.setGone()
                binding.rlBigNative.setGone()
            }
        }
    }

    private fun showBigAd(loaded: Boolean?) {
        binding.rlBigNative.setVisible()
        binding.shimmerLayoutBigAd.setVisible()


        val view = binding.languageShimmer.shimmerAdMediaHolder
        val params = view.layoutParams
        val targetHeight = maxOf(mHeight / 5, 300)

        params?.apply {
            height = targetHeight
            width = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = this
        }

        loaded?.let {
            if (it) {
                PreviewLangAdsLoad.languageUnifiedNativeAds?.let { ad ->
                    Log.e(TAG, "LanguageAct:--BigAd show--------")

                    displayNativeAd(this, binding.flBigNative, binding.shimmerLayoutBigAd)
                }
            } else {
                Log.e(TAG, "LanguageAct:--BigAd fail--------")

                if (AdsConstant.moreAppDataList.isNotEmpty() && AdsConstant.showMoreAppLanguage == "yes") {
                    Log.e(TAG, "LanguageAct:--BigAd show more app--------")

                    loadMoreAppNativeAd(
                        activity = this,
                        frameLayout = binding.flBigNative,
                        shimmerLayout = binding.shimmerLayoutBigAd
                    )
                } else {
                    Log.e(TAG, "LanguageAct:--BigAd more data empty--------")

                    binding.shimmerLayoutBigAd.post {
                        binding.shimmerLayoutBigAd.stopShimmer()
                    }
                }
            }
        } ?: run {
            Log.e(TAG, "LanguageAct:--BigAd not loaded--------")

            if (!PreviewLangAdsLoad.isLoadingInLanguage && !PreviewLangAdsLoad.isLoadingInPreview) {
                PreviewLangAdsLoad.isLoadingInLanguage = true
                Log.e(TAG, "LanguageAct:--BigAd not loaded-111-------")
                googleNativeAd(
                    this,
                    AdsConstant.nativeLanguageAds,
                    binding.shimmerLayoutBigAd,
                    binding.flBigNative
                )
            }
        }
    }

    private fun showSmallAd(loaded: Boolean?) {
        binding.rlSmallNativeBanner.setVisible()
        binding.shimmerLayoutAd.setVisible()


        loaded?.let {
            if (it) {
                PreviewLangAdsLoad.languageUnifiedNativeAds?.let { ad ->
                    Log.e(TAG, "LanguageAct:--SmallAd show-------")

                    displayNativeAd(this, binding.flSmallNativeBanner, binding.shimmerLayoutAd)
                }
            } else {
                Log.e(TAG, "LanguageAct:--SmallAd fail-------")

                if (AdsConstant.moreAppDataList.isNotEmpty() && AdsConstant.showMoreAppLanguage == "yes") {
                    Log.e(TAG, "LanguageAct:--SmallAd show more app-------")

                    loadMoreAppNativeBannerAd(
                        activity = this,
                        frameLayout = binding.flSmallNativeBanner,
                        shimmerLayout = binding.shimmerLayoutAd
                    )
                } else {
                    Log.e(TAG, "LanguageAct:--SmallAd more app data empty-------")

                    binding.shimmerLayoutAd.post {
                        binding.shimmerLayoutAd.stopShimmer()
                    }
                }

            }
        } ?: run {
            Log.e(TAG, "LanguageAct:--SmallAd not loaded-------")

            if (!PreviewLangAdsLoad.isLoadingInLanguage && !PreviewLangAdsLoad.isLoadingInPreview) {
                PreviewLangAdsLoad.isLoadingInLanguage = true

                Log.e(TAG, "LanguageAct:--SmallAd not loaded111-------")
                googleNativeBannerAd(
                    this,
                    AdsConstant.nativeBannerLanguageAds,
                    binding.shimmerLayoutAd,
                    binding.flSmallNativeBanner
                )
            }
        }
    }

    private fun googleNativeAd(
        activity: Activity,
        adID: String,
        shimmerLayoutAd: ShimmerFrameLayout,
        frameLayout: FrameLayout
    ) {
        Log.e(TAG, "LanguageAct_start_load_NativeAd")

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            PreviewLangAdsLoad.languageUnifiedNativeAds = nativeAd
            shimmerLayoutAd.stopShimmer()
            shimmerLayoutAd.setGone()
            displayNativeAd(activity, frameLayout, shimmerLayoutAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                Log.e(TAG, "LanguageAct_NativeAd_fail$loadAdError")

                PreviewLangAdsLoad.languageUnifiedNativeAds = null

                if (AdsConstant.showMoreAppLanguage == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeAd(activity, frameLayout, shimmerLayoutAd)
                        }
                    } else {
                        shimmerLayoutAd.setVisible()
                        shimmerLayoutAd.post {
                            shimmerLayoutAd.stopShimmer()
                        }
                    }
                } else {
                    shimmerLayoutAd.setVisible()

                    shimmerLayoutAd.post {
                        shimmerLayoutAd.stopShimmer()
                    }
                }
            }

            override fun onAdLoaded() {
                Log.e(TAG, "LanguageAct_NativeAd_Loaded")

                shimmerLayoutAd.setGone()
            }

            override fun onAdClicked() {
                Log.e(TAG, "LanguageAct_NativeAd_Clicked")
                AdsConstant.isAdsClick = true
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

    private fun googleNativeBannerAd(
        activity: Activity,
        adID: String,
        shimmerLayoutAd: ShimmerFrameLayout,
        frameLayout: FrameLayout
    ) {
        Log.e(TAG, "LanguageAct_NativeBanner_LoadStart")

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            PreviewLangAdsLoad.languageUnifiedNativeAds = nativeAd
            shimmerLayoutAd.setGone()
            displayNativeAd(activity, frameLayout, shimmerLayoutAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "LanguageAct_NativeBanner_Fail$loadAdError")

                PreviewLangAdsLoad.languageUnifiedNativeAds = null

                if (AdsConstant.showMoreAppLanguage == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeBannerAd(activity, frameLayout, shimmerLayoutAd)
                        }
                    } else {
                        shimmerLayoutAd.setVisible()
                        shimmerLayoutAd.post {
                            shimmerLayoutAd.stopShimmer()
                        }
                    }
                } else {
                    shimmerLayoutAd.setVisible()
                    shimmerLayoutAd.post {
                        shimmerLayoutAd.stopShimmer()
                    }
                }
            }

            override fun onAdLoaded() {
                Log.e(TAG, "LanguageAct_NativeBanner_Loaded")

                shimmerLayoutAd.setGone()
            }

            override fun onAdClicked() {
                Log.e(TAG, "LanguageAct_NativeBanner_Clicked")
                AdsConstant.isAdsClick = true
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

    private fun displayNativeAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayoutAd: ShimmerFrameLayout
    ) {
        Log.e(TAG, "LanguageAct_displayNativeAd")
        shimmerLayoutAd.setGone()

        val adView = if (AdsConstant.showBigNativeLanguage == "yes") {

            binding.rlSmallNativeBanner.setGone()

            activity.layoutInflater.inflate(
                R.layout.google_native_ad_view,
                activity.findViewById(R.id.nativeAd),
                false
            ) as NativeAdView
        } else {

            binding.rlBigNative.setGone()

            activity.layoutInflater.inflate(
                R.layout.google_native_banner_ad_view_130,
                activity.findViewById(R.id.nativeAd),
                false
            ) as NativeAdView
        }

        PreviewLangAdsLoad.languageUnifiedNativeAds?.let {
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

        // Defensive checks
        if (activity.isFinishing || activity.isDestroyed) return
        if (AdsConstant.moreAppDataList.isEmpty()) return

        shimmerLayout.setGone()

        Log.e(EventLog, "LanguageAct_MoreNativeAd_Load")
        MainApplication.firebaseAnalytics?.logEvent("LanguageAct_MoreNativeAd_Load", Bundle())


        // Inflate using ViewBinding
        val binding = GoogleNativeAdViewCloneBinding.inflate(activity.layoutInflater)
        frameLayout.removeAllViews()
        frameLayout.addView(binding.root)


        AdsConstant.adCounter += 1
        if (AdsConstant.moreAppDataList.size == AdsConstant.adCounter) {
            AdsConstant.adCounter = 0
        }

        val number = AdsConstant.adCounter
        val adData = AdsConstant.moreAppDataList[number]

        // Load icon
        Glide.with(activity.applicationContext)
            .load(adData.appIcon)
            .into(binding.adIconClone)

        binding.adNameClone.text = adData.appName
        binding.adBodyClone.text = adData.appDescription

        // Adjust adMediaClone size
        val mediaParams = binding.adMediaClone.layoutParams
        mediaParams.height = if (mHeight / 5 > 300) mHeight / 5 else 300
        mediaParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        binding.adMediaClone.layoutParams = mediaParams

        // Load banner
        Glide.with(activity.applicationContext)
            .load(adData.appBanner)
            .into(binding.adMediaClone)

        binding.adCallToActionClone.text = activity.getString(R.string.install)

        val onClickListener = View.OnClickListener {
            Log.e(EventLog, "LanguageAct_MoreNativeAd_Click")
            MainApplication.firebaseAnalytics?.logEvent("LanguageAct_MoreNativeAd_Click", Bundle())
            showAdClick(activity, adData.appLink.toString())
        }

        binding.adMediaClone.setOnClickListener(onClickListener)
        binding.adCallToActionClone.setOnClickListener(onClickListener)

        Log.e(EventLog, "LanguageAct_MoreNativeAd_show")
        MainApplication.firebaseAnalytics?.logEvent("LanguageAct_MoreNativeAd_show", Bundle())

    }

    private fun loadMoreAppNativeBannerAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {

        Log.e(EventLog, "LanguageAct_MoreNBanner_Load")
        MainApplication.firebaseAnalytics?.logEvent("LanguageAct_MoreNBanner_Load", Bundle())

        // Defensive checks
        if (activity.isFinishing || activity.isDestroyed) return
        if (AdsConstant.moreAppDataList.isEmpty()) return

        shimmerLayout.setGone()

        val binding = GoogleNativeBannerAdView130CloneBinding.inflate(activity.layoutInflater)
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

        val adClickListener = View.OnClickListener {
            Log.e(EventLog, "LanguageAct_MoreNBanner_Click")
            MainApplication.firebaseAnalytics?.logEvent("LanguageAct_MoreNBanner_Click", Bundle())
            showAdClick(activity, adData.appLink ?: "")
        }

        binding.adIconClone.setOnClickListener(adClickListener)
        binding.adNameClone.setOnClickListener(adClickListener)
        binding.adBodyClone.setOnClickListener(adClickListener)
        binding.adCallToActionClone.setOnClickListener(adClickListener)

        Log.e(EventLog, "LanguageAct_MoreNBanner_Show")
        MainApplication.firebaseAnalytics?.logEvent("LanguageAct_MoreNBanner_Show", Bundle())
    }
}