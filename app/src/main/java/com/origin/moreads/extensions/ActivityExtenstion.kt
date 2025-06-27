package com.origin.moreads.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.origin.moreads.R
import com.origin.moreads.utils.PERMISSION_POST_NOTIFICATIONS
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import java.util.TimeZone

fun Activity.setStatusAndNavigationBarColor(
    statusBarColor: Int? = null,
    navigationBarColor: Int? = null
) {
    val window: Window = window

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        statusBarColor?.let { color ->
            window.statusBarColor = color
            val isLightColor = isColorLight(color)
            window.decorView.systemUiVisibility = if (isLightColor) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }

        navigationBarColor?.let { color ->
            window.navigationBarColor = color
            val isLightColor = isColorLight(color)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = if (isLightColor) {
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
        }
    } else {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor?.let { window.statusBarColor = it }
        navigationBarColor?.let { window.navigationBarColor = it }
    }
}


fun isColorLight(color: Int): Boolean {
    val darkness =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
    return darkness < 0.5
}

fun Activity.showSnackBar(
    view: View,
    duration: Int = Snackbar.LENGTH_LONG,
    messageString: String? = null,
    @ColorRes backgroundColor: Int = R.color.black,
    @ColorRes textColor: Int = R.color.white,
    @StringRes message: Int? = null,
    @StringRes actionText: Int? = null,
    @ColorRes actionTextColor: Int = R.color.white,
    marginBottom: Int? = null,
    marginTop: Int? = null,
    actionListener: View.OnClickListener? = null
) {
    val snackBar = message?.let {
        Snackbar.make(
            view,
            it,
            duration
        )
    } ?: messageString?.let {
        Snackbar.make(
            view,
            it,
            duration
        )
    }
    val snackBarView = snackBar?.view
    marginBottom?.let {
        val params = snackBarView?.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, it)
        snackBarView.layoutParams = params
    }
    marginTop?.let {
        val params = snackBarView?.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(params.leftMargin, it, params.rightMargin, params.bottomMargin)
        snackBarView.layoutParams = params
    }
    snackBarView?.setBackgroundColor(color(backgroundColor))
    val snackBarTV = snackBarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    snackBarTV?.setTextColor(color(textColor))

    actionText?.let {
        val snackBarActionTv = snackBarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
        snackBarActionTv?.setTextColor(color(actionTextColor))
        snackBar?.setAction(actionText) {
            actionListener?.onClick(it)
        }
    }
    snackBar?.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackBar?.show()
}




fun Activity.getAppVersion(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return "v${packageInfo.versionName}"
}

fun Activity.getAppVersionSetting(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return "version ${packageInfo.versionName}"
}

fun Activity.getDeviceModel(): String {
    return Build.MODEL
}

fun Activity.getOsVersion(): String {
    return Build.VERSION.RELEASE
}

fun Activity.getScreenResolution(): String {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
}

fun Activity.getDeviceLanguage(): String {
    return Locale.getDefault().language
}

fun Activity.getTimeZone(): String {
    val timeZone = TimeZone.getDefault()
    return "GMT${getTimeZoneOffset(timeZone)}"
}

fun Activity.getTimeZoneOffset(timeZone: TimeZone): String {
    val offset = timeZone.rawOffset / 1000 / 60 / 60
    return if (offset >= 0) "+$offset:00" else "$offset:00"
}

fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(
    this,
    getPermissionString(permId)
) == PackageManager.PERMISSION_GRANTED

fun Context.hasAllPermissions(permIds: Collection<Int>) = permIds.all(this::hasPermission)

fun Context.getPermissionString(id: Int) = when (id) {
    PERMISSION_POST_NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else ""
    else -> ""
}