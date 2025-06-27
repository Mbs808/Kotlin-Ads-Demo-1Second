package com.origin.moreads.extensions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.origin.moreads.ads.utils.SharedPreferenceHelper

val Context.prefsHelper: SharedPreferenceHelper get() = SharedPreferenceHelper.newInstance(this)

fun Context.dimen(@DimenRes dimenRes: Int): Int = resources.getDimensionPixelSize(dimenRes)

@ColorInt
fun Context.color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

fun Context.string(@StringRes id: Int): String {
    return getString(id)
}

inline val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun Context.startIntent(destinationClass: Class<*>) {
    Intent(this, destinationClass).also { startActivity(it) }
}

fun Context.startIntentWithClearTask(destinationClass: Class<*>) {
    Intent(this, destinationClass).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) }.also { startActivity(it) }
}

fun Context.startIntent(valueName: String, value: String, destinationClass: Class<*>) {
    Intent(this, destinationClass).apply {
        putExtra(valueName, value)
    }.also { startActivity(it) }
}

fun Context.startIntent(valueName: String, value: Int, destinationClass: Class<*>) {
    Intent(this, destinationClass).apply {
        putExtra(valueName, value)
    }.also { startActivity(it) }
}

fun Context.startIntent(valueName: String, value: Boolean, destinationClass: Class<*>) {
    Intent(this, destinationClass).apply {
        putExtra(valueName, value)
    }.also { startActivity(it) }
}

fun Context.startIntent(valueName: String, value: Float, destinationClass: Class<*>) {
    Intent(this, destinationClass).apply {
        putExtra(valueName, value)
    }.also { startActivity(it) }
}

fun Context.startIntent(destinationClass: Class<*>, bundle: Bundle) {
    Intent(this, destinationClass).apply {
        putExtras(bundle)
    }.also { startActivity(it) }
}

fun Context.startIntent(activityClass: Class<*>, extrasBuilder: Bundle.() -> Unit = {}) {
    Intent(this, activityClass).apply {
        val extras = Bundle().apply(extrasBuilder)
        putExtras(extras)
    }.also { startActivity(it) }
}