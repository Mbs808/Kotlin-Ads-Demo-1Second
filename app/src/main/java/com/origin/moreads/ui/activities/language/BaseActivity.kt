package com.origin.moreads.ui.activities.language

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.utils.PREF_NAME
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // set same name of preference and key of language code in shared preference
        val prefs = newBase.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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

}