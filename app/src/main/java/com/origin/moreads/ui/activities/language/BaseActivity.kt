package com.origin.moreads.ui.activities.language

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.origin.moreads.extensions.prefsHelper
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref(this, prefsHelper.languageCode)
    }

    fun pref(activity: Activity, commCode: String?) {
        val locale = Locale(commCode!!)
        Locale.setDefault(locale)
        val resources = activity.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}