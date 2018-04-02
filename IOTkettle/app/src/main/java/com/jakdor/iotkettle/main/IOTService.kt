package com.jakdor.iotkettle.main

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.jakdor.iotkettle.R
import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * Foreground service for keeping up connection - due to restrictions in API26+
 */
class IOTService: Service() {

    @Inject
    lateinit var iotClient: IOTClient

    @Inject
    lateinit var iotHelper: IOTHelper

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == ACTION.START_ACTION) {
            Log.i(CLASS_TAG, "Received Start Foreground Intent ")

            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.action = ACTION.MAIN_ACTION
            //notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)

            val icon = BitmapFactory.decodeResource(resources, R.drawable.kettler)

            val notification = NotificationCompat.Builder(
                    this, getString(R.string.service_chanel_id))
                    .setContentTitle(getString(R.string.service_chanel_name))
                    .setTicker(getString(R.string.service_chanel_name))
                    .setContentText(getString(R.string.service_chanel_desc))
                    .setSmallIcon(R.drawable.kettler)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build()

            startForeground(ACTION.FOREGROUND_SERVICE, notification)

        } else if (intent != null && intent.action == ACTION.STOP_ACTION) {
            Log.i(CLASS_TAG, "Received Stop Foreground Intent")
            stopForeground(true)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    interface ACTION {
        companion object {
            const val FOREGROUND_SERVICE = 2137
            const val MAIN_ACTION = "com.jakdor.iotkettle.main.IOTService.action.main"
            const val START_ACTION = "com.jakdor.iotkettle.main.IOTService.action.start"
            const val STOP_ACTION = "com.jakdor.iotkettle.main.IOTService.action.stop"
        }
    }

    companion object {
        const val CLASS_TAG: String = "IOTService"
    }
}