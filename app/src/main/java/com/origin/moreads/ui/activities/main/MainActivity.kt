package com.origin.moreads.ui.activities.main

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.ads.adsload.GoogleInterstitialAds
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.databinding.ActivityMainBinding
import com.origin.moreads.databinding.DialogExitWithAdBinding
import com.origin.moreads.databinding.DialogExitWithMoreAdBinding
import com.origin.moreads.databinding.GoogleNativeAdViewCloneBinding
import com.origin.moreads.databinding.GoogleNativeBannerAdView80CloneBinding
import com.origin.moreads.extensions.hasAllPermissions
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.showSnackBar
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.ads.interstitialad.InterstitialAdActivity
import com.origin.moreads.ui.activities.ads.shimmer.ShimmerAdsActivity
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.ui.activities.language.LanguageActivity
import com.origin.moreads.ui.dialogs.OpenSettingDialog
import com.origin.moreads.ui.dialogs.PermissionNeededDialog
import com.origin.moreads.utils.EventLog
import com.origin.moreads.utils.IS_FROM
import com.origin.moreads.utils.PERMISSION_REQUEST_NOTIFICATION_ARRAY
import com.origin.moreads.utils.PERMISSION_REQUEST_NOTIFICATION_LIST
import com.origin.moreads.utils.SETTING_ACTIVITY
import com.origin.moreads.utils.setGone
import com.origin.moreads.utils.setInvisible
import com.origin.moreads.utils.setVisible
import com.origin.moreads.utils.showAdClick

class MainActivity : BaseActivity() {

    /***** Dialogs *****/
    private var permissionNeededDialog: PermissionNeededDialog? = null
    private var openSettingDialog: OpenSettingDialog? = null

    /***** Permission Result Contract *****/
    private var permissionLauncherForNotification: ActivityResultLauncher<Array<String>>? = null

    /***** Activity Result Contract *****/
    private var resultLauncherForNotification: ActivityResultLauncher<Intent>? = null

    /***** App Update Manager *****/
    private var appUpdateManager: AppUpdateManager? = null
    private var reviewManager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /***** Initial Load Google Interstitial Ads *****/

        if (AdsConstant.isConnected(this) && !prefsHelper.isInterShow) {
            if (!AdsConstant.isSplashInterCall) {
                GoogleInterstitialAds.loadInterstitial(this)
            }
        }

        /** Rate us dialog show at app open counter of 2*/
        if (prefsHelper.rateUsDialogCounter == 2) {
            prefsHelper.rateUsDialogCounter += 1

            requestReviewInfo()
            //No need to translate the language.
        }

        initializeAppUpdateManager()
        setUpPermissionDialog()
        subscribePermissionContract()
        subscribeActivityResultsContract()

        setOnClickListener()

        if (!prefsHelper.isInterShow) {
            exitAdsDialogInit()
        }

        setHomeAdView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasAllPermissions(PERMISSION_REQUEST_NOTIFICATION_LIST)) {
                requestPermissionForNotification()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "MainAct_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("MainAct_onBackPressed", Bundle())

                if (AdsConstant.isConnected(this@MainActivity)) {
                    if (AdsConstant.showAdsExitDialog == "yes" && !prefsHelper.isInterShow) {
                        showExitAdsDialog()
                    } else {
                        finishAffinity()
                    }
                } else {
                    finishAffinity()
                }
            }
        })

        Log.e(EventLog, "MainAct_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_onCreate", Bundle())
    }

    private fun subscribeActivityResultsContract() {
        resultLauncherForNotification =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (hasAllPermissions(PERMISSION_REQUEST_NOTIFICATION_LIST)) {
                    prefsHelper.rateUsDialogCounter += 1
                } else {
                    openSettingDialog?.show()
                }
            }
    }

    private fun subscribePermissionContract() {
        permissionLauncherForNotification =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.entries.all { it.value }
                if (granted) {
                    prefsHelper.rateUsDialogCounter += 1
                } else {
                    if (!prefsHelper.isPermissionNeededDialogShowed) {
                        prefsHelper.isPermissionNeededDialogShowed = true
                        permissionNeededDialog?.show()
                    } else {
                        openSettingDialog?.show()
                    }
                }
            }
    }

    private fun setUpPermissionDialog() {
        permissionNeededDialog = PermissionNeededDialog(
            activity = this,
            onConfirmClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionForNotification()
                }
            }
        )

        openSettingDialog = OpenSettingDialog(
            activity = this,
            onConfirmClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestActivityResultsForNotification()
                }
            },
            onDismiss = {
                finishAffinity()
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionForNotification() {
        permissionLauncherForNotification?.launch(PERMISSION_REQUEST_NOTIFICATION_ARRAY)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestActivityResultsForNotification() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra("app_package", packageName)
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        resultLauncherForNotification?.launch(intent)
    }

    private fun setOnClickListener() {
        binding.tvShimmerAds.setOnClickListener {
            startIntent(ShimmerAdsActivity::class.java)
        }

        binding.tvInterstitialAds.setOnClickListener {
            startIntent(InterstitialAdActivity::class.java)
        }

        binding.tvRate.setOnClickListener {
            requestReviewInfo()
            Toast.makeText(this, "Rate dialog is shown here.", Toast.LENGTH_SHORT).show()
        }

        binding.tvChangeLanguage.setOnClickListener {
            val bundle = Bundle().apply {
                putString(IS_FROM, SETTING_ACTIVITY)
            }
            startIntent(LanguageActivity::class.java, bundle)
        }

    }

    private fun initializeAppUpdateManager() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) || appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE
                ))
            ) {
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                            .setAllowAssetPackDeletion(true)
                            .build(),
                        APP_UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
        appUpdateManager?.registerListener(statusUpdateListener)
    }

    private fun requestReviewInfo() {
        reviewManager = ReviewManagerFactory.create(this)
        val request = reviewManager!!.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
                showReviewFlow()
            } else {
                Toast.makeText(this, "ReviewInfo not received.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showReviewFlow() {
        reviewInfo?.let {
            val flow = reviewManager?.launchReviewFlow(this, it)
            flow?.addOnCompleteListener { _ ->
                Toast.makeText(this, "Review successful", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val statusUpdateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                showSnackBar(
                    view = findViewById(android.R.id.content),
                    duration = Snackbar.LENGTH_INDEFINITE,
                    messageString = "Downloading... $bytesDownloaded/$totalBytesToDownload",
                    actionTextColor = R.color.app_color
                )
            }

            InstallStatus.DOWNLOADED -> {
                showSnackBar(
                    view = findViewById(android.R.id.content),
                    messageString = "Download Completed",
                    actionTextColor = R.color.app_color,
                    actionText = R.string.restart,
                    actionListener = {
                        appUpdateManager?.completeUpdate()
                    }
                )
            }

            InstallStatus.FAILED -> {
                showSnackBar(
                    view = findViewById(android.R.id.content),
                    backgroundColor = android.R.color.holo_red_dark,
                    textColor = R.color.white,
                    messageString = "Download Failed"
                )
            }

            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()

        Log.e(EventLog, "MainAct_onResume")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_onResume", Bundle())

        appUpdateManager?.let { updateManager ->
            updateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager?.startUpdateFlowForResult(
                            appUpdateInfo,
                            this,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                                .setAllowAssetPackDeletion(true)
                                .build(),
                            APP_UPDATE_REQUEST_CODE
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                }

                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    showSnackBar(
                        view = findViewById(android.R.id.content),
                        messageString = "Download Completed",
                        actionTextColor = R.color.app_color,
                        actionText = R.string.restart,
                        actionListener = {
                            appUpdateManager?.completeUpdate()
                        }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        Log.e(EventLog, "MainAct_onStop")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_onStop", Bundle())

        appUpdateManager?.unregisterListener(statusUpdateListener)
    }

    companion object {
        private const val APP_UPDATE_REQUEST_CODE = 101
    }

    fun loadMoreAppNativeAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayoutAd: ShimmerFrameLayout
    ) {

        Log.e(EventLog, "MainAct_More_Native_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_More_Native_LoadStart", Bundle())

        // Defensive checks
        if (activity.isFinishing || activity.isDestroyed) return
        if (AdsConstant.moreAppDataList.isEmpty()) return

        val mHeight = resources.displayMetrics.heightPixels

        // Hide shimmer layout safely
        shimmerLayoutAd.setGone()

        val binding = GoogleNativeAdViewCloneBinding.inflate(activity.layoutInflater)
        frameLayout.removeAllViews()
        frameLayout.addView(binding.root)

        // Increment adCounter and reset if needed
        AdsConstant.adCounter += 1
        if (AdsConstant.moreAppDataList.size == AdsConstant.adCounter) {
            AdsConstant.adCounter = 0
        }

        val number = AdsConstant.adCounter
        val adData = AdsConstant.moreAppDataList[number]

        // Load app icon
        Glide.with(activity.applicationContext)
            .asBitmap()
            .load(adData.appIcon)
            .into(binding.adIconClone)

        binding.adNameClone.text = adData.appName
        binding.adBodyClone.text = adData.appDescription
        binding.adCallToActionClone.text = activity.getString(R.string.install)

        // Adjust media image height
        binding.adMediaClone.layoutParams = binding.adMediaClone.layoutParams.apply {
            height = if (mHeight / 5 > 300) mHeight / 5 else 300
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        // Load banner
        Glide.with(activity.applicationContext)
            .asBitmap()
            .load(adData.appBanner)
            .into(binding.adMediaClone)

        // Click listeners
        val onClickListener = View.OnClickListener {
            Log.e(EventLog, "MainAct_More_Native_Click")
            MainApplication.firebaseAnalytics?.logEvent("MainAct_More_Native_Click", Bundle())
            showAdClick(activity, adData.appLink ?: "")
        }

        binding.adMediaClone.setOnClickListener(onClickListener)
        binding.adCallToActionClone.setOnClickListener(onClickListener)

        Log.e(EventLog, "MainAct_More_Native_Show")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_More_Native_Show", Bundle())
    }

    /**
    Home Native Banner Ads
     **/
    private fun setHomeAdView() {
        if (AdsConstant.isConnected(this) && AdsConstant.showNativeBannerShimmer80 == "yes") {
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

    private fun googleNativeBannerAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {

        Log.e(EventLog, "MainAct_NativeBanner_LoadStart")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_NativeBanner_LoadStart", Bundle())

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            shimmerLayout.setGone()
            showNativeBanner(activity, frameLayout, shimmerLayout, nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(EventLog, "MainAct_NativeBanner_Fail$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("MainAct_NativeBanner_Fail", Bundle())

                if (AdsConstant.showMoreAppNativeBanner == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        if (!activity.isFinishing) {
                            loadMoreAppNativeBannerAd(activity, frameLayout, shimmerLayout)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (AdsConstant.moreAppDataList.isNotEmpty()) {
                                if (!activity.isFinishing) {
                                    loadMoreAppNativeBannerAd(activity, frameLayout, shimmerLayout)
                                }
                            } else {
                                shimmerLayout.stopShimmer()
                            }
                        }, 3000)
                    }
                } else {
                    shimmerLayout.stopShimmer()
                }
            }

            override fun onAdLoaded() {
                Log.e(EventLog, "MainAct_NativeBanner_Loaded")
                MainApplication.firebaseAnalytics?.logEvent("MainAct_NativeBanner_Loaded", Bundle())

                shimmerLayout.setGone()
            }

            override fun onAdClicked() {
                Log.e(EventLog, "MainAct_NativeBanner_Clicked")
                MainApplication.firebaseAnalytics?.logEvent(
                    "MainAct_NativeBanner_Clicked",
                    Bundle()
                )

                googleNativeBannerAd(activity, adID, frameLayout, shimmerLayout)
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
        shimmerLayout: ShimmerFrameLayout,
        nativeAd: NativeAd
    ) {

        Log.e(EventLog, "MainAct_NativeBanner_Show")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_NativeBanner_Show", Bundle())

        shimmerLayout.setGone()

        val adView = activity.layoutInflater.inflate(
            R.layout.google_native_banner_ad_view_80,
            activity.findViewById(R.id.nativeAd),
            false
        ) as NativeAdView

        populateAppInstallAdViewNativeBanner(nativeAd, adView)
        frameLayout.removeAllViews()
        frameLayout.addView(adView)
    }

    private fun populateAppInstallAdViewNativeBanner(
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
        Log.e(EventLog, "MainAct_MoreNativeBanner_Load")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_MoreNativeBanner_Load", Bundle())

        // Hide shimmer layout safely
        shimmerLayout.setGone()

        // Defensive checks
        if (activity.isFinishing || activity.isDestroyed) return
        if (AdsConstant.moreAppDataList.isEmpty()) return

        // Inflate binding instead of view
        val binding = GoogleNativeBannerAdView80CloneBinding.inflate(activity.layoutInflater)

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
            Log.e(EventLog, "MainAct_MoreNativeBanner_Click")
            MainApplication.firebaseAnalytics?.logEvent("MainAct_MoreNativeBanner_Click", Bundle())
            showAdClick(activity, adData.appLink.toString())
        }

        binding.adIconClone.setOnClickListener(clickListener)
        binding.adNameClone.setOnClickListener(clickListener)
        binding.adBodyClone.setOnClickListener(clickListener)
        binding.adCallToActionClone.setOnClickListener(clickListener)

        Log.e(EventLog, "MainAct_MoreNativeBanner_Show")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_MoreNativeBanner_Show", Bundle())

    }

/////////////////////////////////////////////////////////////////////////////

    //exit dialog with ads
    private var mHeight = 0
    private lateinit var exitAdsDialog: BottomSheetDialog

    private fun exitAdsDialogInit() {
        Log.e("TAG", "exitAdsDialogInit:::-------000----- ")

        mHeight = resources.displayMetrics.heightPixels

        exitAdsDialog = BottomSheetDialog(this, R.style.ExitBottomSheetTheme)

        val backBinding = DialogExitWithAdBinding.inflate(layoutInflater)

        exitAdsDialog.apply {
            setContentView(backBinding.root)
            setCanceledOnTouchOutside(true)

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setGravity(Gravity.BOTTOM)

            val shimmerHolder = backBinding.exitShimmer.shimmerAdMediaHolder

            val params = shimmerHolder.layoutParams
            params.height = maxOf(mHeight / 5, 300)
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            shimmerHolder.layoutParams = params

            if (AdsConstant.showAdsExitDialog == "yes") {
                if (AdsConstant.onlyShowMoreAppNative == "yes") {
                    if (AdsConstant.moreAppDataList.size > 0) {
                        backBinding.shimmerLayoutAd.let {
                            loadMoreAppNativeAd(
                                this@MainActivity,
                                backBinding.flNative,
                                it
                            )
                        }
                    } else {
                        backBinding.shimmerLayoutAd.stopShimmer()
                    }
                } else {
                    backBinding.apply {
                        loadGoogleNativeExitAd(
                            activity = this@MainActivity,
                            adID = AdsConstant.nativeLanguageAds,
                            frameLayout = backBinding.flNative,
                            shimmerLayout = backBinding.shimmerLayoutAd
                        )
                    }
                }
            }

            backBinding.btnTapToExit.setOnClickListener {
                dismiss()
                finishAffinity()
            }
        }

    }

    fun loadGoogleNativeExitAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        Log.e("Video_Maker", "Exit_adLoadStart")
        MainApplication.firebaseAnalytics?.logEvent("Exit_adLoadStart", Bundle())

        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            shimmerLayout.visibility = View.GONE
            showNativeExit(activity, frameLayout, shimmerLayout, nativeAd)

        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("Video_Maker", "Exit_onAdFailed:$loadAdError")
                MainApplication.firebaseAnalytics?.logEvent("Exit_onAdFailed", Bundle())

                if (AdsConstant.showMoreAppNative == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        if (!activity.isFinishing) {
                            shimmerLayout.let { loadMoreAppNativeAd(activity, frameLayout, it) }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (AdsConstant.moreAppDataList.isNotEmpty()) {
                                if (!activity.isFinishing) {
                                    shimmerLayout.let {
                                        loadMoreAppNativeAd(
                                            activity,
                                            frameLayout,
                                            it
                                        )
                                    }
                                }
                            } else {
                                shimmerLayout.stopShimmer()
                            }
                        }, 3000)

                    }
                } else {
                    shimmerLayout.stopShimmer()
                }
            }

            override fun onAdLoaded() {
                Log.e("Video_Maker", "Exit_onAdLoaded")
                MainApplication.firebaseAnalytics?.logEvent("Exit_onAdLoaded", Bundle())
                shimmerLayout.visibility = View.GONE
            }

            override fun onAdClicked() {
                Log.e("Video_Maker", "Exit_onAdClicked")
                MainApplication.firebaseAnalytics?.logEvent("Exit_onAdClicked", Bundle())
                loadGoogleNativeExitAd(activity, adID, frameLayout, shimmerLayout)
            }
        }).build()

        val request = getAddRequest()
        adLoader.loadAd(request)
    }

    private fun showNativeExit(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayoutAd: ShimmerFrameLayout,
        nativeAd: NativeAd
    ) {
        Log.e(EventLog, "MainAct_NativeExit_Show")
        MainApplication.firebaseAnalytics?.logEvent("MainAct_NativeExit_Show", Bundle())

        shimmerLayoutAd.setGone()

        val adView = activity.layoutInflater.inflate(
            R.layout.google_native_ad_view,
            activity.findViewById(R.id.nativeAd),
            false
        ) as NativeAdView

        nativeAd.let {
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

    private fun showExitAdsDialog() {
        if (!exitAdsDialog.isShowing) {
            if (!isFinishing) {
                exitAdsDialog.show()
            }
        }
    }

}