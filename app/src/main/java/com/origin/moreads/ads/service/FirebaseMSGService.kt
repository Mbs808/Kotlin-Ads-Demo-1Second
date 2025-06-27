package com.origin.moreads.ads.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.origin.moreads.R
import com.origin.moreads.ui.activities.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMSGService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.e(TAG, "onNewToken: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        if (message.notification != null) {
            val data = message.data
            var strKey: String? = null
            var strValue: String? = null
            for ((key, value) in data) {
                Log.e(TAG, " Key :: $key -- Value :: $value")
                strKey = key
                strValue = value
            }
            showNotification(
                message.notification!!.title,
                message.notification!!.imageUrl.toString(),
                message.notification!!.body,
                strKey,
                strValue
            )
        }

    }

    private fun showNotification(
        title: String?,
        image: String?,
        message: String?,
        strKey: String?,
        strValue: String?
    ) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        if (strKey != null && strValue != null) {
            intent.putExtra("key", strKey)
            intent.putExtra("value", strValue)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder: NotificationCompat.Builder?
        builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "FirebaseMSGService"

        private const val CHANNEL_ID = "HEADS_UP_NOTIFICATION"
        private const val CHANNEL_NAME = "HEADS_UP_NOTIFICATION"

        private const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL"
        private const val NOTIFICATION_CHANNEL_NAME = "ADS_NOTIFICATION"
    }

}