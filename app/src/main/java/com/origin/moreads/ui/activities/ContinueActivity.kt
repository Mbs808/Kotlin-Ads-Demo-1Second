package com.origin.moreads.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.origin.moreads.R
import com.origin.moreads.ads.adsload.AdsLoaded
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.ads.utils.SharedPreferenceHelper
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.ads.shimmer.bannerad.ShimmerBannerAdActivity
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.ui.activities.language.LanguageActivity

class ContinueActivity : BaseActivity() {

    var tvPolicy: TextView? = null
    var continueLottie: LottieAnimationView? = null
    var policyText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_continue)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvPolicy = findViewById(R.id.tvPolicy)
        continueLottie = findViewById(R.id.continueLottie)

        loadLanguageScreenAds()
        setPolicyText()

        continueLottie?.setOnClickListener {
            startIntent(LanguageActivity::class.java)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startIntent(LanguageActivity::class.java)
                finish()
            }
        })
    }

    private fun loadLanguageScreenAds() {
        Log.e("TAG", "loadLanguageScreenAds::::-----------isLoadingInLanguage---------${AdsLoaded.isLoadingInLanguage} ", )
        Log.e("TAG", "loadLanguageScreenAds::::-----------showLanguageNativeAd---------${AdsConstant.showLanguageNativeAd} ", )
        Log.e("TAG", "loadLanguageScreenAds::::-----------isLanguageSelected---------${prefsHelper.isLanguageSelected} ", )
        Log.e("TAG", "loadLanguageScreenAds::::-----------onlyShowMoreAppLanguage---------${AdsConstant.onlyShowMoreAppLanguage} ", )

        if (!AdsLoaded.isLoadingInLanguage) {
            if (AdsConstant.showLanguageNativeAd == "yes") {
                if (!prefsHelper.isLanguageSelected) {
                    if (AdsConstant.onlyShowMoreAppLanguage != "yes") {
                        Log.d("lanugage---", "splash---" + "bbbb")
                        if (AdsConstant.showBigNativeLanguage == "yes") {
                            Log.d("lanugage---", "splash---" + "ccc")
                            AdsLoaded.isLanguageLoadingInSplash = true
                            AdsLoaded.loadGoogleNativeAd(
                                this,
                                AdsConstant.nativeLanguageAds
                            ) { nativeAd ->
                                if (AdsLoaded.languageUnifiedNativeAds != null) {
                                    AdsLoaded.languageUnifiedNativeAds?.destroy()
                                }
                                AdsLoaded.languageUnifiedNativeAds = nativeAd
                                AdsLoaded.isLanguageAdLoadingMutableLiveData.value =
                                    nativeAd != null
                            }
                        } else {
                            Log.d("lanugage---", "splash---" + "dddd")
                            AdsLoaded.isLanguageLoadingInSplash = true
                            AdsLoaded.loadGoogleNativeAd(
                                this,
                                AdsConstant.nativeBannerLanguageAds
                            ) { nativeAd ->
                                if (AdsLoaded.languageUnifiedNativeAds != null) {
                                    AdsLoaded.languageUnifiedNativeAds?.destroy()
                                }
                                AdsLoaded.languageUnifiedNativeAds = nativeAd
                                AdsLoaded.isLanguageAdLoadingMutableLiveData.value =
                                    nativeAd != null
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setPolicyText() {
        policyText = "By tapping Get Started, you indicate that you have read our Privacy Policy"

        val spannableString = SpannableString(policyText)

// Set default text color
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            policyText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickableWord = "Privacy Policy"
        val start = policyText.indexOf(clickableWord)

        if (start >= 0) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, "https://www.google.com/".toUri())
                    try {
                        widget.context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            widget.context,
                            widget.context.getString(R.string.no_browser_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = "#266af1".toColorInt()
                    ds.isFakeBoldText = true
                }
            }

            spannableString.setSpan(
                clickableSpan,
                start,
                start + clickableWord.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            Log.e("PrivacyText", "Privacy Policy not found in text.")
        }

        tvPolicy?.text = spannableString
        tvPolicy?.movementMethod = LinkMovementMethod.getInstance()

    }
}