package com.jakdor.iotkettle.main

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.view.View
import com.jakdor.iotkettle.R
import com.jakdor.iotkettle.mvp.BasePresenter
import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.app.NotificationChannel
import android.os.Build

class MainPresenter(view: MainContract.MainView,
                    private val iotClient: IOTClient,
                    private val iotHelper: IOTHelper)
    : BasePresenter<MainContract.MainView>(view), MainContract.MainPresenter {

    private var preferences: SharedPreferences
            = PreferenceManager.getDefaultSharedPreferences(view.getViewContext())

    private lateinit var connectionString: String

    private var notifyIcon: Bitmap
            = BitmapFactory.decodeResource(view.getViewContext().resources, R.drawable.kettler)
    private var notifyIcon2: Bitmap
            = BitmapFactory.decodeResource(view.getViewContext().resources, R.drawable.kettler2)

    private var notificationCounter = 0
    private var channel: NotificationChannel? = null

    private var timerFlag = false
    private var timerStart: Long = 0
    private var timer: Long = 0

    private var retryCounter = 0

    override fun start(){
        super.start()
        loadIp()
        view.setIpEditText(connectionString)

        connect()

        iotHelper.changeIotClient(iotClient)
        Thread(iotHelper).start()

        timerHandler.postDelayed(timerRunnable, 0)
    }

    override fun destroy() {
        super.destroy()
        timerHandler.removeCallbacks(null)
    }

    /**
     * Main loop, change check every 1000ms
     */
    internal var timerHandler = Handler()
    private var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            checkConnection()
            receive()
            timeCounter()

            timerHandler.postDelayed(this, 1000)
        }
    }

    /**
     * Load IP String with SharedPreferencesManager
     */
    override fun loadIp() {
        connectionString = preferences.getString(
                view.getResourcesString(R.string.ip_string),
                view.getResourcesString(R.string.default_ip_string))
    }

    /**
     * Save IP String with SharedPreferencesManager
     */
    override fun saveIp(ip: String) {
        val editor = preferences.edit()
        editor.putString(view.getResourcesString(R.string.ip_string), ip)
        editor.apply()
    }

    /**
     * Defines behaviour for IpChangedButton onClick Event
     */
    override fun onIpChangedButtonListener(): View.OnClickListener {
        return View.OnClickListener { _ ->
            val newIp = view.getIpEditText()

            if (connectionString != newIp) {
                saveIp(newIp)
                connectionString = newIp
            }

            iotClient.kill()
            connect()

            notificationCounter = 0
            iotHelper.changeIotClient(iotClient)
        }
    }

    /**
     * Start connection threat
     */
    override fun connect() {
        view.setStatusTextView(view.getResourcesString(R.string.status_connecting))
        iotClient.connectIP = connectionString
        Thread(iotClient).start()
    }

    /**
     * Process received data
     */
    override fun receive() {
        if (iotHelper.isNotifyFlag) {
            iotHelper.notifyHandled()

            val received = iotHelper.received

            if (received == "start") {
                sendNotification("Czajnik uruchomiony", time, false)
                view.setStatusTextView(view.getResourcesString(R.string.status_working))
                timerFlag = true
            } else if (received == "stop1") {
                sendNotification("Czajnik wyłączony", time, true)
                view.setStatusTextView(view.getResourcesString(R.string.status_ended))
                timerFlag = false
            }
        }
    }

    /**
     * Checks connection, restarts if needed
     */
    override fun checkConnection() {
        if (!iotClient.isConnectionOK) {
            ++retryCounter
            view.setStatusTextView(view.getResourcesString(R.string.status_no_connection))
            if (retryCounter > 5) {
                iotClient.kill()
                connect()
                iotHelper.changeIotClient(iotClient)
                retryCounter = 0
            }
        } else if (notificationCounter == 0) {
            view.setStatusTextView(view.getResourcesString(R.string.status_text))
        }
    }

    /**
     * Elapsed time counter
     */
    private fun timeCounter() {
        if (timerFlag) {
            if (timerStart == 0L) {
                timerStart = System.nanoTime()
            } else {
                timer = System.nanoTime() - timerStart
                timer = TimeUnit.SECONDS.convert(timer, TimeUnit.NANOSECONDS)
            }

            displayTimer()
        } else if (timer > 0) {
            if (timerStart != 0L) {
                timer = System.nanoTime() - timerStart
                timer = TimeUnit.SECONDS.convert(timer, TimeUnit.NANOSECONDS)
                displayTimer()

                timerStart = 0
            }
        }
    }

    /**
     * Displays elapsed time
     */
    override fun displayTimer() {
        if (timer % 60 < 10) {
            view.setTimerDisplayTextView(String.format(
                    Locale.ENGLISH, "Czas działania: %1\$d:0%2\$d", timer / 60, timer % 60))
        } else {
            view.setTimerDisplayTextView(String.format(
                    Locale.ENGLISH, "Czas działania: %1\$d:%2\$d", timer / 60, timer % 60))
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

    /**
     * Builds and displays Android notification
     */
    override fun sendNotification(title: String, text: String, type: Boolean) {
        val notifBuilder = NotificationCompat.Builder(view.getViewContext(),
                view.getResourcesString(R.string.notification_chanel_id))
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

        val notificationManager = view.getViewContext()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(channel == null){
                setupNotificationChanel()
                notificationManager.createNotificationChannel(channel)
            }
        }

        notificationManager.notify(notificationCounter++, notifBuilder.build())
    }

    private fun setupNotificationChanel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = view.getResourcesString(R.string.notification_chanel_id)
            val name = view.getResourcesString(R.string.notification_chanel_name)
            val description = view.getResourcesString(R.string.notification_chanel_desc)
            channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel?.description = description
        }
    }
}