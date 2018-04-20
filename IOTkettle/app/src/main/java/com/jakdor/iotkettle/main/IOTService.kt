package com.jakdor.iotkettle.main

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.jakdor.iotkettle.R
import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper
import dagger.android.AndroidInjection
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Foreground service for keeping up connection - due to restrictions in API26+
 */
class IOTService: Service() {

    @Inject
    lateinit var iotClient: IOTClient

    @Inject
    lateinit var iotHelper: IOTHelper

    private lateinit var connectionString: String

    private lateinit var helperThread: Thread

    private var retryCounter = 0

    private lateinit var notifyIcon: Bitmap
    private lateinit var notifyIcon2: Bitmap

    private var notificationId = 2137
    private var notificationCounter = 0
    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

        notifyIcon = BitmapFactory.decodeResource(resources, R.drawable.kettler)
        notifyIcon2 = BitmapFactory.decodeResource(resources, R.drawable.kettler2)
        notificationBuilder =
                NotificationCompat.Builder(this, getString(R.string.service_chanel_id))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == ACTION.START_ACTION) {
            Log.i(CLASS_TAG, "Received Start Foreground Intent")

            connectionString = intent.extras.getString("ip")

            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.action = ACTION.MAIN_ACTION
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)

            val icon = BitmapFactory.decodeResource(resources, R.drawable.kettler)

            notificationBuilder.setContentTitle(getString(R.string.service_chanel_desc))
                    .setTicker(getString(R.string.service_chanel_desc))
                    .setContentText(getString(R.string.service_idle_info))
                    .setSmallIcon(R.drawable.kettler)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)

            startForeground(notificationId, notificationBuilder.build())

            connect()

            iotHelper.changeIotClient(iotClient)
            helperThread = Thread(iotHelper)
            helperThread.start()

            timerHandler = Handler()
            timerHandler.postDelayed(timerRunnable, 0)

        } else if (intent != null && intent.action == ACTION.IP_CHANGE_ACTION) {
            Log.i(CLASS_TAG, "Received change ip Intent")
            connectionString = intent.extras.getString("ip")

            helperThread = Thread(iotHelper)
            helperThread.start()
            timerHandler = Handler()
            timerHandler.postDelayed(timerRunnable, 0)

            onIpChanged()
        } else if (intent != null && intent.action == ACTION.STOP_ACTION) {
            Log.i(CLASS_TAG, "Received Stop Foreground Intent")
            timerHandler.removeCallbacks(timerRunnable)
            iotClient.kill()
            helperThread.interrupt()
            notificationCounter = 0
            stopForeground(true)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * Send status update to [MainActivity]
     */
    fun sendStatusBroadcast(appState: AppState){
        val intent = Intent("IoTKettleAppState")
        intent.putExtra("state", appState)
        sendBroadcast(intent)
    }

    /**
     * Main loop, change check every 1000ms
     */
    private lateinit var timerHandler: Handler
    private var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            checkConnection()
            receive()

            timerHandler.postDelayed(this, 1000)
        }
    }

    /**
     * Defines behaviour after ipChangedAction
     */
    fun onIpChanged(){
        iotClient.kill()
        connect()

        notificationCounter = 0
        iotHelper.changeIotClient(iotClient)
    }

    /**
     * Start connection threat
     */
    fun connect() {
        sendStatusBroadcast(AppState.CONNECTING)

        iotClient.connectIP = connectionString
        Thread(iotClient).start()
    }

    /**
     * Process received data
     */
    fun receive() {
        if (iotHelper.isNotifyFlag) {
            iotHelper.notifyHandled()

            val received = iotHelper.received

            if (received == "start") {
                sendNotification("Czajnik uruchomiony", time, false)
                sendStatusBroadcast(AppState.START)
            } else if (received == "stop1") {
                sendNotification("Czajnik wyłączony", time, true)
                sendStatusBroadcast(AppState.STOP)
            }
        }
    }

    /**
     * Checks connection, restarts if needed
     */
    fun checkConnection() {
        if (!iotClient.isConnectionOK) {
            ++retryCounter

            sendStatusBroadcast(AppState.DISCONNECTED)

            if (retryCounter > 5) {
                iotClient.kill()
                connect()
                iotHelper.changeIotClient(iotClient)
                retryCounter = 0
            }
        } else if (notificationCounter == 0) {
            sendStatusBroadcast(AppState.CONNECTED)
        }
    }

    /**
     * Builds and displays Android notification
     */
    fun sendNotification(title: String, text: String, type: Boolean) {
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(text)

        if (type) {
            val pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
            notificationBuilder.setVibrate(pattern)
            notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_alert)
            notificationBuilder.setLights(Color.RED, 500, 500)
            notificationBuilder.setLargeIcon(notifyIcon2)
            notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        } else {
            val pattern = longArrayOf(250, 250, 250)
            notificationBuilder.setVibrate(pattern)
            notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
            notificationBuilder.setLights(Color.BLUE, 500, 500)
            notificationBuilder.setLargeIcon(notifyIcon)
            notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        notificationBuilder.setSound(alarmSound)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
        ++notificationCounter
    }


    /**
     * Format current time for notifications
     */
    private val time: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }

    interface ACTION {
        companion object {
            const val MAIN_ACTION = "com.jakdor.iotkettle.main.IOTService.action.main"
            const val START_ACTION = "com.jakdor.iotkettle.main.IOTService.action.start"
            const val STOP_ACTION = "com.jakdor.iotkettle.main.IOTService.action.stop"
            const val IP_CHANGE_ACTION = "com.jakdor.iotkettle.main.IOTService.action.ip_change"
        }
    }

    companion object {
        const val CLASS_TAG: String = "IOTService"
    }
}