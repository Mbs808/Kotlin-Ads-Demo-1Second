package com.origin.moreads.ads

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.origin.moreads.R
import com.origin.moreads.ads.utils.AdsConstant
import com.origin.moreads.utils.gotoPlayStore

object UpdateDialogManager {
    var currentDialog: Dialog? = null

    fun showUpdateDialog(activity: Activity) {
        if (activity.isFinishing) return
        if (currentDialog?.isShowing == true) return
        if (AdsConstant.updateNow != "yes") return

        currentDialog = Dialog(activity).apply {
            setContentView(R.layout.update_dialog)
            window?.apply {
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setGravity(Gravity.CENTER)
            }
            setCancelable(false)

            findViewById<TextView>(R.id.btnUpdateApp)?.setOnClickListener {
                dismiss()
                gotoPlayStore(activity)
            }

            setOnDismissListener {
                currentDialog = null
            }
        }

        if (!activity.isFinishing) {
            currentDialog?.show()
        }
    }

}
