package com.origin.moreads.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.net.toUri

// Variable Route
const val IS_FROM = "IS_FROM"

const val EventLog = "Ads_Demo"

const val PREF_NAME = "com.origin.adsdemo"


// Activity Route
const val SETTING_ACTIVITY = "SETTING_ACTIVITY"

// PERMISSION CONSTANTS
const val PERMISSION_POST_NOTIFICATIONS = 1

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
val PERMISSION_REQUEST_NOTIFICATION_ARRAY = arrayOf(
    Manifest.permission.POST_NOTIFICATIONS
)

val PERMISSION_REQUEST_NOTIFICATION_LIST = setOf(
    PERMISSION_POST_NOTIFICATIONS
)


fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun View.setVisible() {
    visibility = View.VISIBLE
}

fun View.setGone() {
    visibility = View.GONE
}

fun showAdClick(activity: Activity, link: String?) {
    if (link.isNullOrBlank()) {
        Log.e(EventLog, "Ad link is null or empty")
        return
    }

    try {
        val uri = link.toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e(EventLog, "No activity found for link: $link", e)
    } catch (e: Exception) {
        Log.e(EventLog, "Error opening ad link: $link", e)
    }
}
