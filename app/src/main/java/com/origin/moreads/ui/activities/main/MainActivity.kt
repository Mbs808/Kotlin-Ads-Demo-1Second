package com.origin.moreads.ui.activities.main

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
import com.origin.moreads.ads.adsload.AdsLoaded
import com.origin.moreads.ads.adsload.GoogleInterstitialAds
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.extensions.gone
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
import com.origin.moreads.utils.Global
import com.origin.moreads.utils.IS_FROM
import com.origin.moreads.utils.PERMISSION_REQUEST_NOTIFICATION_ARRAY
import com.origin.moreads.utils.PERMISSION_REQUEST_NOTIFICATION_LIST
import com.origin.moreads.utils.SETTING_ACTIVITY

class MainActivity : BaseActivity() {

    /***** Main View *****/
    private var btnShimmerAds: TextView? = null
    private var btnInterstitialAds: TextView? = null
    private var btnRate: TextView? = null
    private var btnChangeLanguage: TextView? = null

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


    private var main: ConstraintLayout? = null

    /***** AdView *****/
    private var clAdView: ConstraintLayout? = null

    /** NativeBannerAd **/
    private var rlNativeBanner: RelativeLayout? = null
    private var shimmerLayoutAdBanner: ShimmerFrameLayout? = null
    private var flNativeBanner: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Apply padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /***** Initial Load Google Interstitial Ads *****/
        if (!AdsConstant.isSplashInterCall) {
            GoogleInterstitialAds.googleInterstitial(this)
        }

        /***** Initial Load App Open Ads *****/
        /*if (AppOpenManager.appOpenAd == null) {
            AppOpenManager(application as MainApplication)
        }*/

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
        initializeViews()
        setOnClickListener()
        loadAdForExitDialog()
        setHomeAdView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasAllPermissions(PERMISSION_REQUEST_NOTIFICATION_LIST)) {
                requestPermissionForNotification()
            }
        }

        editAdsDialog()

        Log.e(LOG_TAG, "${TAG}_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("${TAG}_onCreate", Bundle())

    }

    private fun subscribeActivityResultsContract() {
        resultLauncherForNotification =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (hasAllPermissions(PERMISSION_REQUEST_NOTIFICATION_LIST)) {
                    prefsHelper.rateUsDialogCounter += 1
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
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

    private fun initializeViews() {
        /***** MainView *****/
        btnShimmerAds = findViewById(R.id.tvShimmerAds)
        btnInterstitialAds = findViewById(R.id.tvInterstitialAds)
        btnRate = findViewById(R.id.tvRate)
        btnChangeLanguage = findViewById(R.id.tvChangeLanguage)


        /***** AdView *****/
        clAdView = findViewById(R.id.clAdView)
        /** NativeBannerAd **/
        rlNativeBanner = findViewById(R.id.rlNativeBanner)
        shimmerLayoutAdBanner = findViewById(R.id.shimmerLayoutAd)
        flNativeBanner = findViewById(R.id.flNativeBanner)
    }

    private fun setOnClickListener() {
        btnShimmerAds?.setOnClickListener {
            startIntent(ShimmerAdsActivity::class.java)
        }

        btnInterstitialAds?.setOnClickListener {
            startIntent(InterstitialAdActivity::class.java)
        }

        btnRate?.setOnClickListener {
            requestReviewInfo()
            //No need to translate the language.
            Toast.makeText(this, "Rate dialog is shown here.", Toast.LENGTH_SHORT).show()
        }


        btnChangeLanguage?.setOnClickListener {
            val bundle = Bundle().apply {
                putString(IS_FROM, SETTING_ACTIVITY)
            }
            startIntent(LanguageActivity::class.java, bundle)
        }

    }

    private fun loadAdForExitDialog() {
        if (AdsConstant.showAdsExitDialog == "yes") {
            AdsLoaded.loadGoogleNativeAd(
                context = this,
                adId = AdsConstant.nativeExitDialogAds
            ) { nativeAd ->
                if (AdsLoaded.exitDialogUnifiedNativeAds != null) {
                    AdsLoaded.exitDialogUnifiedNativeAds?.destroy()
                }
                AdsLoaded.exitDialogUnifiedNativeAds = nativeAd
            }
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                Log.e(LOG_TAG, "${TAG}_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("${TAG}_onBackPressed", Bundle())

                if (AdsConstant.showAdsExitDialog == "yes") {
                    /*exitDialogWithAd = ExitDialogWithAd(
                        this,
                        onDialogDismiss = {},
                        onDialogShow = {}
                    )
                    exitDialogWithAd?.show(supportFragmentManager, exitDialogWithAd?.tag)*/
                    showExitAdsDialog()
//                    editAdsDialog()
                } else {
                    finishAffinity()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onResume() {
        super.onResume()

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

        if (Global.isSharing || Global.isGoingOutside) {
            Global.isSharing = false
            Global.isGoingOutside = false
        }

    }

    override fun onStop() {
        super.onStop()
        appUpdateManager?.unregisterListener(statusUpdateListener)
    }

    companion object {
        private const val LOG_TAG = "MainActivity"
        private const val TAG = "MainAct"

        private const val APP_UPDATE_REQUEST_CODE = 101
    }

    //exit dialog
    private var exitAdsDialog: Dialog? = null
    private lateinit var rlNative: RelativeLayout
    private lateinit var textSpaceFrame: FrameLayout
    private lateinit var space: Space
    private lateinit var flNative: FrameLayout

    // Shimmer Included Layout
    private var shimmerAdMediaHolder: View? = null
    private var shimmerLayoutAd: ShimmerFrameLayout? = null

    private lateinit var btnTapToExit: TextView

    private var mHeight = 0

    private fun editAdsDialog() {
        exitAdsDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_exit_with_ad)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setGravity(Gravity.BOTTOM)
            }
            setCancelable(true)
            setCanceledOnTouchOutside(true)
//            show()

            // Initialize Views
            rlNative = findViewById(R.id.rlNative)
            textSpaceFrame = findViewById(R.id.textSpaceFrame)
            space = findViewById(R.id.space)
            flNative = findViewById(R.id.flNative)

            shimmerAdMediaHolder = findViewById(R.id.shimmerAdMediaHolder)
            shimmerLayoutAd = findViewById(R.id.shimmerLayoutAd)

            btnTapToExit = findViewById(R.id.btnTapToExit)

            // Load Ad View
            setAdView()

            // Exit button click
            btnTapToExit.setOnClickListener {
//                SplashOpenAds.appOpenAd = null
                dismiss()
                finishAffinity()
            }
        }
    }

    private fun showExitAdsDialog() {
        if (exitAdsDialog != null && !exitAdsDialog!!.isShowing) {
            exitAdsDialog?.show()
        } else {
            exitAdsDialog?.dismiss()
            exitAdsDialog?.show()
        }
    }


    private fun setAdView() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mHeight = displayMetrics.heightPixels
        val params = space.layoutParams
        params.height = mHeight / 5
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        space.layoutParams = params

        val paramsShimmerMediaHolder = shimmerAdMediaHolder?.layoutParams
        if (mHeight / 5 > 300) {
            paramsShimmerMediaHolder?.height = mHeight / 5
        } else {
            paramsShimmerMediaHolder?.height = 300
        }
        paramsShimmerMediaHolder?.width = ViewGroup.LayoutParams.MATCH_PARENT
        shimmerAdMediaHolder?.layoutParams = paramsShimmerMediaHolder

        if (AdsConstant.onlyShowMoreAppNative == "yes") {
            if (AdsConstant.moreAppDataList.size > 0) {
                shimmerLayoutAd?.let {
                    loadMoreAppNativeAd(
                        this,
                        flNative,
                        it
                    )
                }
            } else {

                shimmerLayoutAd?.stopShimmer()

//                tvAdText.visible()
            }
        } else {
            if (AdsLoaded.exitDialogUnifiedNativeAds != null) {
                showNativeBanner(
                    activity = this,
                    frameLayout = flNative,
                    nativeAd = AdsLoaded.exitDialogUnifiedNativeAds!!
                )
            } else {
                googleNativeAd(
                    activity = this,
                    adID = AdsConstant.nativeExitDialogAds,
                    frameLayout = flNative
                )
            }
        }
    }

    private fun getAddRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("maxAdContentRating", AdsConstant.maxAdContentRating)
        return AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun googleNativeAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout
    ) {
        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->
            AdsLoaded.exitDialogUnifiedNativeAds = nativeAd
            Log.e(TAG, "${TAG}_onNativeAdLoaded")
            shimmerLayoutAd?.visibility = View.GONE
            showNativeBanner(activity, frameLayout, nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "${TAG}_onAdFailedToLoad_$loadAdError")
                Log.e("TAG", "onAdFailedToLoad: 000 loadAdError ::   $loadAdError")

                if (AdsConstant.showMoreAppNative == "yes") {
                    if (AdsConstant.moreAppDataList.isNotEmpty()) {
                        shimmerLayoutAd?.let { loadMoreAppNativeAd(activity, frameLayout, it) }
                    } else {
                        Log.e("TAG", "onAdFailedToLoad: 000")
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (AdsConstant.moreAppDataList.isNotEmpty()) {
                                if (!activity.isFinishing) {
                                    shimmerLayoutAd?.let {
                                        loadMoreAppNativeAd(
                                            activity,
                                            frameLayout,
                                            it
                                        )
                                    }
                                }
                            } else {
                                shimmerLayoutAd?.stopShimmer()
                            }
                        }, 3000)

                    }
                } else {
                    Log.e("TAG", "onAdFailedToLoad: 000 loadAdError ::   $loadAdError")
                    shimmerLayoutAd?.stopShimmer()
                }

                AdsLoaded.exitDialogUnifiedNativeAds = null
            }

            override fun onAdLoaded() {
                shimmerLayoutAd?.visibility = View.GONE
//                tvAdText.visibility = View.GONE
            }

            override fun onAdClicked() {
                AdsLoaded.exitDialogUnifiedNativeAds = null
                Log.e(TAG, "${TAG}_onAdClicked")
                googleNativeAd(activity, adID, frameLayout)
            }
        }).build()

        val request = getAddRequest()
        adLoader.loadAd(request)
    }

    private fun showNativeBanner(
        activity: Activity,
        frameLayout: FrameLayout,
        nativeAd: NativeAd
    ) {
        shimmerLayoutAd?.visibility = View.GONE
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

    fun loadMoreAppNativeAd(
        activity: Activity,
        frameLayout: FrameLayout,
        shimmerLayoutAd: ShimmerFrameLayout
    ) {
        shimmerLayoutAd.visibility = View.GONE

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
            Log.e(TAG, "loadMoreAppNativeAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }

        adCallToActionClone.setOnClickListener {
            Log.e(TAG, "loadMoreAppNativeAd_click")
            showAdClick(activity, AdsConstant.moreAppDataList[number].appLink.toString())
        }
        Log.e(TAG, "loadMoreAppNativeAd_show")
    }

    private fun showAdClick(activity: Activity, link: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        }
    }

    /**
    Home Native Banner Ads
     **/

    private fun setHomeAdView() {
        Log.e(
            TAG,
            "setHomeAdView:::-----showNativeBannerShimmer80--------------${AdsConstant.showNativeBannerShimmer80}",
        )
        if (AdsConstant.showNativeBannerShimmer80 == "yes") {
            if (AdsConstant.onlyShowMoreAppNativeBanner == "yes") {
                if (AdsConstant.moreAppDataList.size > 0) {
                    loadMoreAppNativeBannerAd(
                        activity = this,
                        frameLayout = flNativeBanner!!,
                        shimmerLayout = shimmerLayoutAdBanner!!
                    )
                } else {
                    shimmerLayoutAdBanner?.stopShimmer()
                }
            } else {
                googleNativeBannerAd(
                    activity = this,
                    adID = AdsConstant.nativeBannerAds,
                    frameLayout = flNativeBanner!!,
                    shimmerLayout = shimmerLayoutAdBanner!!
                )
            }
        } else {
            rlNativeBanner?.gone()
        }
    }


    private fun googleNativeBannerAd(
        activity: Activity,
        adID: String,
        frameLayout: FrameLayout,
        shimmerLayout: ShimmerFrameLayout
    ) {
        val builder = AdLoader.Builder(activity, adID).forNativeAd { nativeAd ->

            shimmerLayout.visibility = View.GONE
            showNativeBanner(activity, frameLayout, shimmerLayout, nativeAd)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {

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
                shimmerLayout.visibility = View.GONE
            }

            override fun onAdClicked() {

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
            R.layout.google_native_banner_ad_view_80_clone,
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


}