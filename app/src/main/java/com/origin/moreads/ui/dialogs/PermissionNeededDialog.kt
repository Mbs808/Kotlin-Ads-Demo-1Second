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

class PermissionNeededDialog(
    activity: Activity,
    private val onConfirmClick: () -> Unit
) : Dialog(activity, R.style.CustomDialog) {

    private var ivClose: ImageView? = null
    private var btnRequestPermission: TextView? = null

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
        setContentView(R.layout.dialog_permission_needed)

        initView()
        clickEvents()

    }

    private fun initView() {
        ivClose = findViewById(R.id.ivClose)
        btnRequestPermission = findViewById(R.id.btnRequestPermission)
    }

    private fun clickEvents() {
        ivClose?.setOnClickListener {
            dismiss()
        }
        btnRequestPermission?.setOnClickListener {
            onConfirmClick.invoke()
            dismiss()
        }
    }

}