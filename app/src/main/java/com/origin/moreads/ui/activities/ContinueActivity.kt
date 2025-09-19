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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.origin.moreads.MainApplication
import com.origin.moreads.R
import com.origin.moreads.databinding.ActivityContinueBinding
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.language.BaseActivity
import com.origin.moreads.ui.activities.language.LanguageActivity
import com.origin.moreads.utils.EventLog

class ContinueActivity : BaseActivity() {

    var policyText = ""

    private lateinit var binding: ActivityContinueBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContinueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // preload language native ads
//        if (AdsConstant.isConnected(this@ContinueActivity) && !prefsHelper.isLanguageSelected && AdsConstant.showLanguageNativeAd == "yes") {
//            PreviewLangAdsLoad.loadLanguageNativeAds(this)
//        }


        setPolicyText()

        binding.continueLottie.setOnClickListener {
            startIntent(LanguageActivity::class.java)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(EventLog, "ContinueAct_onBackPressed")
                MainApplication.firebaseAnalytics?.logEvent("ContinueAct_onBackPressed", Bundle())

                startIntent(LanguageActivity::class.java)
                finish()
            }
        })

        Log.e(EventLog, "ContinueAct_onCreate")
        MainApplication.firebaseAnalytics?.logEvent("ContinueAct_onCreate", Bundle())
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
                    } catch (_: ActivityNotFoundException) {
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
            Log.e("TAG", "Privacy Policy not found in text.")
        }

        binding.tvPolicy.text = spannableString
        binding.tvPolicy.movementMethod = LinkMovementMethod.getInstance()

    }

}