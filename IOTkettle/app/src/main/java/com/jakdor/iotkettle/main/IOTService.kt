package com.jakdor.iotkettle.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
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

    private var retryCounter = 0

    private lateinit var notifyIcon: Bitmap
    private lateinit var notifyIcon2: Bitmap

    private var notificationCounter = 0
    private var channel: NotificationChannel? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == ACTION.START_ACTION) {
            Log.i(CLASS_TAG, "Received Start Foreground Intent")

            connectionString = intent.extras.getString("ip")

            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.action = ACTION.MAIN_ACTION
            //notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)

            val icon = BitmapFactory.decodeResource(resources, R.drawable.kettler)

            notifyIcon = BitmapFactory.decodeResource(resources, R.drawable.kettler)
            notifyIcon2 = BitmapFactory.decodeResource(resources, R.drawable.kettler2)

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

            connect()

            iotHelper.changeIotClient(iotClient)
            Thread(iotHelper).start()

            timerHandler.postDelayed(timerRunnable, 0)

        } else if (intent != null && intent.action == ACTION.IP_CHANGE_ACTION) {
            Log.i(CLASS_TAG, "Received change ip Intent")
            connectionString = intent.extras.getString("ip")
            onIpChanged()
        } else if (intent != null && intent.action == ACTION.STOP_ACTION) {
            Log.i(CLASS_TAG, "Received Stop Foreground Intent")
            timerHandler.removeCallbacks(null)
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
        val intent = Intent("AppState")
        intent.putExtra("state", appState)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Main loop, change check every 1000ms
     */
    internal var timerHandler = Handler()
    private var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            checkConnection()
            receive()
            //timeCounter()
            //todo presenter start timer

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
        //view.setStatusTextView(R.string.status_connecting)
        //todo presenter notify connecting
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
                //view.setStatusTextView(R.string.status_working)
                //timerFlag = true
                //todo presenter notify start
                sendStatusBroadcast(AppState.START)
            } else if (received == "stop1") {
                sendNotification("Czajnik wyłączony", time, true)
                //view.setStatusTextView(R.string.status_ended)
                //timerFlag = false
                //todo presenter notify stop
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
            //view.setStatusTextView(R.string.status_no_connection)
            //todo presenter notify no connection
            sendStatusBroadcast(AppState.DISCONNECTED)
            if (retryCounter > 5) {
                iotClient.kill()
                connect()
                iotHelper.changeIotClient(iotClient)
                retryCounter = 0
            }
        } else if (notificationCounter == 0) {
            //view.setStatusTextView(R.string.status_text)
            //todo presenter notify connected
            sendStatusBroadcast(AppState.CONNECTED)
        }
    }

    /**
     * Builds and displays Android notification
     */
    fun sendNotification(title: String, text: String, type: Boolean) {
        val notifBuilder = NotificationCompat.Builder(
                this, getString(R.string.notification_chanel_id))
        notifBuilder.setContentTitle(title)
        notifBuilder.setContentText(text)

        if (type) {
            val pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
            notifBuilder.setVibrate(pattern)
            notifBuilder.setSmallIcon(android.R.drawable.ic_dialog_alert)
            notifBuilder.setLights(Color.RED, 500, 500)
            notifBuilder.setLargeIcon(notifyIcon2)
            notifBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        } else {
            notifBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
            notifBuilder.setLights(Color.BLUE, 500, 500)
            notifBuilder.setLargeIcon(notifyIcon)
            notifBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        notifBuilder.setSound(alarmSound)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(channel == null){
                setupNotificationChanel()
                notificationManager.createNotificationChannel(channel)
            }
        }

        notificationManager.notify(notificationCounter++, notifBuilder.build())
    }

    /**
     * Setup Notification Chanel for API>=26
     */
    private fun setupNotificationChanel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = getString(R.string.notification_chanel_id)
            val name = getString(R.string.notification_chanel_name)
            val description = getString(R.string.notification_chanel_desc)
            channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel?.description = description
        }
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
            const val FOREGROUND_SERVICE = 2137
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