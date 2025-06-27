package com.origin.moreads.ads.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceHelper(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var languageCode: String
        get() = prefs.getString(LANGUAGE_CODE, "en").toString()
        set(languageCode) = prefs.edit().putString(LANGUAGE_CODE, languageCode).apply()

    var mreAppAccountName: String
        get() = prefs.getString(MORE_APP_ACCOUNT_NAME, AdsConstant.moreAppAccountName).toString()
        set(accountName) = prefs.edit().putString(MORE_APP_ACCOUNT_NAME, accountName).apply()

    var moreAppUrl: String
        get() = prefs.getString(MORE_APP_URL, AdsConstant.moreAppUrl).toString()
        set(moreAppUrl) = prefs.edit().putString(MORE_APP_URL, moreAppUrl).apply()

    var isLanguageSelected: Boolean
        get() = prefs.getBoolean(IS_LANGUAGE_SELECTED, false)
        set(isLanguageSelected) = prefs.edit().putBoolean(IS_LANGUAGE_SELECTED, isLanguageSelected)
            .apply()

    var isOnBoardingDone: Boolean
        get() = prefs.getBoolean(IS_ONBOARDING_DONE, false)
        set(isDone) = prefs.edit().putBoolean(IS_ONBOARDING_DONE, isDone)
            .apply()


    var rateUsDialogCounter: Int
        get() = prefs.getInt(RATE_US_DIALOG_COUNTER, 0)
        set(rateUsDialogCounter) = prefs.edit().putInt(RATE_US_DIALOG_COUNTER, rateUsDialogCounter)
            .apply()

    var isPermissionNeededDialogShowed: Boolean
        get() = prefs.getBoolean(IS_PERMISSION_NEEDED_DIALOG_SHOWED, false)
        set(isPermissionNeededDialogShowed) = prefs.edit()
            .putBoolean(IS_PERMISSION_NEEDED_DIALOG_SHOWED, isPermissionNeededDialogShowed).apply()

    companion object {

        fun newInstance(context: Context) = SharedPreferenceHelper(context)

        private const val PREF_NAME = "com.origin.adsdemo"
        private const val DEFAULT_INT_VALUE = -1
        private const val DEFAULT_STRING_VALUE = ""
        private const val DEFAULT_BOOLEAN_VALUE = false

        // Preference Keys
        const val LANGUAGE_CODE = "LANGUAGE_CODE"
        const val IS_LANGUAGE_SELECTED = "IS_LANGUAGE_SELECTED"
        const val IS_ONBOARDING_DONE = "IS_ONBOARDING_DONE"
        const val EXIT_COUNTER = "EXIT_COUNTER"
        const val RATE_US_DIALOG_COUNTER = "RATE_US_DIALOG_COUNTER"
        const val IS_PERMISSION_NEEDED_DIALOG_SHOWED = "IS_PERMISSION_NEEDED_DIALOG_SHOWED"
        const val MORE_APP_URL = "MORE_APP_URL"
        const val MORE_APP_ACCOUNT_NAME = "MORE_APP_ACCOUNT_NAME"

        // Getters and setters for different types of preferences
        fun getIntPref(context: Context, key: String, defaultValue: Int = DEFAULT_INT_VALUE): Int {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getInt(key, defaultValue)
        }

        fun setIntPref(context: Context, key: String, value: Int) {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            pref.edit().putInt(key, value).apply()
        }

        fun getStringPref(
            context: Context,
            key: String,
            defaultValue: String = DEFAULT_STRING_VALUE
        ): String {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getString(key, defaultValue) ?: defaultValue
        }

        fun setStringPref(context: Context, key: String, value: String) {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            pref.edit().putString(key, value).apply()
        }

        fun getBooleanPref(
            context: Context,
            key: String,
            defaultValue: Boolean = DEFAULT_BOOLEAN_VALUE
        ): Boolean {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getBoolean(key, defaultValue)
        }

        fun setBooleanPref(context: Context, key: String, value: Boolean) {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            pref.edit().putBoolean(key, value).apply()
        }

    }


}