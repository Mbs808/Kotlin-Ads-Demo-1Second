package com.origin.moreads.ads.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.origin.moreads.ads.adsload.AppOpenManager

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action != null) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    AppOpenManager.isShowingOpenAds = true
                }

                Intent.ACTION_SCREEN_ON -> {
                    AppOpenManager.isShowingOpenAds = true
                }
            }
        }
    }
}