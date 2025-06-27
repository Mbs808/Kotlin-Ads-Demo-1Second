package com.origin.moreads.ui.activities.onboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.origin.moreads.R
import com.origin.moreads.ads.adsload.OnBoardingSecondAd
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.ui.activities.main.MainActivity

class OnBoardingActivity : BaseActivity() {

    var viewPager: ViewPager2? = null
    var adapter: ViewPagerAdapter? = null

    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefsHelper.isOnBoardingDone = true

        loadAds()

        viewPager = findViewById(R.id.viewpager)

        val shouldShow= AdsConstant.isShow_onBoarding_FullAds == "yes"

        adapter = ViewPagerAdapter(this,shouldShow)

        viewPager?.adapter = adapter

        viewPager?.offscreenPageLimit = if (shouldShow) 5 else 4

        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (shouldShow && position == 2) {
                    startAutoNavigateTimer(AdsConstant.onBoarding_FullTimer)
                } else {
                    stopAutoNavigateTimer()
                }
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveToNextPage()
            }
        })
    }

    fun moveToNextPage() {
        val nextItem = viewPager!!.currentItem + 1
        if (nextItem < (viewPager!!.adapter?.itemCount ?: 0)) {
            viewPager?.currentItem = nextItem
        } else {
            startIntent(MainActivity::class.java)
        }
    }

    private fun loadAds() {
        //preload native onboarding
        if (AdsConstant.isShow_onBoarding_2Ads == "yes") {
            if (!OnBoardingSecondAd.isLoadingInOnBoarding) {
                OnBoardingSecondAd.isLoadingInCurrentAct = true
                OnBoardingSecondAd.loadGoogleNativeAd(
                    this,
                    AdsConstant.onBoarding_2_BigNative
                ) { nativeAd ->
                    if (OnBoardingSecondAd.onB2NativeAds != null) {
                        OnBoardingSecondAd.onB2NativeAds?.destroy()
                    }
                    OnBoardingSecondAd.onB2NativeAds = nativeAd
                    OnBoardingSecondAd.isOnB2LoadingMutableLiveData.value = nativeAd != null
                }
            }
        }
    }

    private fun startAutoNavigateTimer(delayMillis: Long) {
        stopAutoNavigateTimer()

        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = Runnable {
            moveToNextPage()
        }
        timerHandler?.postDelayed(timerRunnable!!, delayMillis)
    }

    private fun stopAutoNavigateTimer() {
        timerHandler?.removeCallbacks(timerRunnable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoNavigateTimer()
    }
}