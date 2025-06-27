package com.origin.moreads.extensions

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.origin.moreads.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackBar(
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
    actionListener: View.OnClickListener? = null,
) {
    context?.let { context ->
        val snackBar = message?.let { message ->
            Snackbar.make(
                view,
                message,
                duration
            )
        } ?: messageString?.let { messageString ->
            Snackbar.make(
                view,
                messageString,
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
        snackBarView?.setBackgroundColor(context.color(backgroundColor))
        val snackBarTV =
            snackBarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackBarTV?.setTextColor(context.color(textColor))
        actionText?.let {
            val snackBarActionTv =
                snackBarView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
            snackBarActionTv?.setTextColor(context.color(actionTextColor))
            snackBar?.setAction(actionText) {
                actionListener?.onClick(it)
            }
        }
        snackBar?.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBar?.show()
    }
}

fun Fragment.startIntent(destinationClass: Class<*>, bundle: Bundle) {
    context?.let { context ->
        Intent(context, destinationClass).apply {
            putExtras(bundle)
        }.also { startActivity(it) }
    }
}