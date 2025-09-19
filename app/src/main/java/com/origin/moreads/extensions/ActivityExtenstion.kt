package com.origin.moreads.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.origin.moreads.R
import com.origin.moreads.utils.PERMISSION_POST_NOTIFICATIONS


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
    val snackBarTV =
        snackBarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    snackBarTV?.setTextColor(color(textColor))

    actionText?.let {
        val snackBarActionTv =
            snackBarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
        snackBarActionTv?.setTextColor(color(actionTextColor))
        snackBar?.setAction(actionText) {
            actionListener?.onClick(it)
        }
    }
    snackBar?.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackBar?.show()
}


fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(
    this,
    getPermissionString(permId)
) == PackageManager.PERMISSION_GRANTED

fun Context.hasAllPermissions(permIds: Collection<Int>) = permIds.all(this::hasPermission)

fun getPermissionString(id: Int) = when (id) {
    PERMISSION_POST_NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else ""
    else -> ""
}