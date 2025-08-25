package com.origin.moreads.extensions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.origin.moreads.ads.utils.SharedPreferenceHelper

val Context.prefsHelper: SharedPreferenceHelper get() = SharedPreferenceHelper.newInstance(this)


@ColorInt
fun Context.color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

fun Context.string(@StringRes id: Int): String {
    return getString(id)
}

fun Context.startIntent(destinationClass: Class<*>) {
    Intent(this, destinationClass).also { startActivity(it) }
}


fun Context.startIntent(destinationClass: Class<*>, bundle: Bundle) {
    Intent(this, destinationClass).apply {
        putExtras(bundle)
    }.also { startActivity(it) }
}

