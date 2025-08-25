package com.origin.moreads.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

val EditText.value: String get() = text.toString().trim()

