package com.origin.moreads.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.origin.moreads.R
import com.origin.moreads.ads.utils.AdsConstant

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
        Log.e("TAG", "Ad link is null or empty")
        return
    }

    try {
        val uri = link.toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(activity, activity.getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
        Log.e("TAG", "No activity found for link: $link", e)
    } catch (e: Exception) {
        Log.e("TAG", "Error opening ad link: $link", e)
    }
}

fun gotoPlayStore(activity: Activity) {
    try {
        val intent = Intent("android.intent.action.VIEW")
        intent.setData(AdsConstant.playStoreLink.toUri())
        activity.startActivity(intent)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        val linkString: String = AdsConstant.playStoreLink
        val defaultBrowser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
        defaultBrowser.setData(linkString.toUri())
        activity.startActivity(defaultBrowser)
    }
}
