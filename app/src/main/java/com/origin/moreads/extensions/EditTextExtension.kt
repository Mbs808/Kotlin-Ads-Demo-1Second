package com.origin.moreads.extensions

import android.widget.EditText

val EditText.value: String get() = text.toString().trim()

