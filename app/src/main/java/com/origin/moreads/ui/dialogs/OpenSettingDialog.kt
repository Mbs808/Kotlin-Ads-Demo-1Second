package com.origin.moreads.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.origin.moreads.R

class OpenSettingDialog(
    activity: Activity,
    private val onConfirmClick: () -> Unit,
    private val onDismiss: () -> Unit
): Dialog(activity, R.style.CustomDialog) {

    private var ivClose: ImageView? = null
    private var btnOpenSetting: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_open_setting)

        initView()
        clickEvents()

    }

    private fun initView() {
        ivClose = findViewById(R.id.ivClose)
        btnOpenSetting = findViewById(R.id.btnOpenSetting)
    }

    private fun clickEvents() {
        ivClose?.setOnClickListener {
            onDismiss.invoke()
            dismiss()
        }
        btnOpenSetting?.setOnClickListener {
            onConfirmClick.invoke()
            dismiss()
        }
    }

}