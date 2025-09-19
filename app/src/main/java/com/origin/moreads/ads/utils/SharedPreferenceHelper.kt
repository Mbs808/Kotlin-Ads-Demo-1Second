package com.origin.moreads.ads.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.origin.moreads.utils.PREF_NAME

class SharedPreferenceHelper(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var languageCode: String
        get() = prefs.getString(LANGUAGE_CODE, "en").toString()
        set(languageCode) = prefs.edit { putString(LANGUAGE_CODE, languageCode) }

    var moreAppAccountName: String
        get() = prefs.getString(MORE_APP_ACCOUNT_NAME, AdsConstant.moreAccountName).toString()
        set(accountName) = prefs.edit { putString(MORE_APP_ACCOUNT_NAME, accountName) }

    var moreAppUrl: String
        get() = prefs.getString(MORE_APP_URL, AdsConstant.moreAppUrl).toString()
        set(moreAppUrl) = prefs.edit { putString(MORE_APP_URL, moreAppUrl) }

    var isLanguageSelected: Boolean
        get() = prefs.getBoolean(IS_LANGUAGE_SELECTED, false)
        set(isLanguageSelected) = prefs.edit {
            putBoolean(IS_LANGUAGE_SELECTED, isLanguageSelected)
        }

    var rateUsDialogCounter: Int
        get() = prefs.getInt(RATE_US_DIALOG_COUNTER, 0)
        set(rateUsDialogCounter) = prefs.edit {
            putInt(RATE_US_DIALOG_COUNTER, rateUsDialogCounter)
        }

    var isPermissionNeededDialogShowed: Boolean
        get() = prefs.getBoolean(IS_PERMISSION_NEEDED_DIALOG_SHOWED, false)
        set(isPermissionNeededDialogShowed) = prefs.edit {
            putBoolean(IS_PERMISSION_NEEDED_DIALOG_SHOWED, isPermissionNeededDialogShowed)
        }


    companion object {
        fun newInstance(context: Context) = SharedPreferenceHelper(context)
        // Preference Keys
        const val LANGUAGE_CODE = "LANGUAGE_CODE"
        const val IS_LANGUAGE_SELECTED = "IS_LANGUAGE_SELECTED"
        const val RATE_US_DIALOG_COUNTER = "RATE_US_DIALOG_COUNTER"
        const val IS_PERMISSION_NEEDED_DIALOG_SHOWED = "IS_PERMISSION_NEEDED_DIALOG_SHOWED"
        const val MORE_APP_URL = "MORE_APP_URL"
        const val MORE_APP_ACCOUNT_NAME = "MORE_APP_ACCOUNT_NAME"

    }


}