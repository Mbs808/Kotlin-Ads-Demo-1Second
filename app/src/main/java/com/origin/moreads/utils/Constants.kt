package com.origin.moreads.utils

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

// Variable Route
const val IS_FROM = "IS_FROM"
const val RATTING_STAR = "RATTING_STAR"

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
