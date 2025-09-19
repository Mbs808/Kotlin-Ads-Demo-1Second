package com.origin.moreads.ui.activities.language

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.origin.moreads.ads.UpdateDialogManager
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.ads.utils.UpdateDialogAction
import com.origin.moreads.utils.PREF_NAME
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // set same name of preference and key of language code in shared preference
        val prefs = newBase.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val langCode = prefs.getString("LANGUAGE_CODE", "en")

        val localeUpdatedContext = updateLocaleContext(newBase,langCode)
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun updateLocaleContext(context: Context, langCode: String?): Context {
        if (langCode.isNullOrEmpty()) return context

        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == UpdateDialogAction.SHOW_UPDATE_DIALOG) {
                UpdateDialogManager.showUpdateDialog(this@BaseActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (AdsConstant.updateNow == "yes" && !isFinishing) {
            UpdateDialogManager.showUpdateDialog(this)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, IntentFilter(UpdateDialogAction.SHOW_UPDATE_DIALOG), RECEIVER_EXPORTED)
        } else {
            registerReceiver(updateReceiver, IntentFilter(UpdateDialogAction.SHOW_UPDATE_DIALOG))
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(updateReceiver)
    }

}