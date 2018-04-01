package com.jakdor.iotkettle.main

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Handler
import android.app.NotificationManager
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView

import com.jakdor.iotkettle.R
import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper
import dagger.android.AndroidInjection

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var iotClient: IOTClient

    @Inject
    lateinit var iotHelper: IOTHelper

    private var preferences: SharedPreferences? = null

    private var dummyTextView: TextView? = null
    private var timerDisplayTextView: TextView? = null
    private var ipTextEdit: EditText? = null

    private var notifyIcon: Bitmap? = null
    private var notifyIcon2: Bitmap? = null

    private var connectionString: String? = null
    private var appContext: Context? = null

    private var retryCounter = 0
    private var notificationCounter = 0

    private var timerFlag = false
    private var timerStart: Long = 0
    private var timer: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appContext = this

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        connectionString = preferences!!.getString(getString(R.string.ip_string), "192.168.1.188")

        dummyTextView = findViewById<View>(R.id.textView) as TextView
        timerDisplayTextView = findViewById<View>(R.id.timerDisplayTextView) as TextView
        ipTextEdit = findViewById<View>(R.id.editText) as EditText
        ipTextEdit!!.setText(connectionString)

        findViewById<View>(R.id.button).setOnTouchListener(changeIpButtonListener)

        notifyIcon = BitmapFactory.decodeResource(resources, R.drawable.kettler)
        notifyIcon2 = BitmapFactory.decodeResource(resources, R.drawable.kettler2)

        connect()

        iotHelper.changeIotClient(iotClient)
        Thread(iotHelper).start()

        timerHandler.postDelayed(timerRunnable, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(null)
    }

    /**
     * ChangeIP button listener
     */
    private val changeIpButtonListener = View.OnTouchListener { view, motionEvent ->
        val newIp = ipTextEdit!!.text.toString()

        if (connectionString != newIp) {
            val editor = preferences!!.edit()
            editor.putString(getString(R.string.ip_string), newIp)
            editor.apply()

            connectionString = newIp
        }

        iotClient.kill()
        connect()

        notificationCounter = 0
        iotHelper.changeIotClient(iotClient)

        false
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
     * Format current time for notifications
     */
    private val time: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }

    /**
     * Starts connection threat
     */
    private fun connect() {
        dummyTextView!!.setText(R.string.status_connecting)
        iotClient.connectIP = connectionString!!
        Thread(iotClient).start()
    }

    /**
     * checks connection, restarts if needed
     */
    private fun checkConnection() {
        if (!iotClient.isConnectionOK) {
            ++retryCounter
            dummyTextView!!.setText(R.string.status_no_connection)
            if (retryCounter > 5) {
                iotClient.kill()
                connect()
                iotHelper.changeIotClient(iotClient)
                retryCounter = 0
            }
        } else if (notificationCounter == 0) {
            dummyTextView!!.setText(R.string.status_text)
        }
    }

    /**
     * GUI response for received data
     */
    private fun receive() {
        if (iotHelper.isNotifyFlag) {
            iotHelper.notifyHandled()

            val received = iotHelper.received

            if (received == "start") {
                sendNotification("Czajnik uruchomiony", time, false)
                dummyTextView!!.setText(R.string.status_working)
                timerFlag = true
            } else if (received == "stop1") {
                sendNotification("Czajnik wyłączony", time, true)
                dummyTextView!!.setText(R.string.status_ended)
                timerFlag = false
            }
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
    private fun displayTimer() {
        if (timer % 60 < 10) {
            timerDisplayTextView!!.text = String.format(
                    Locale.ENGLISH, "Czas działania: %1\$d:0%2\$d", timer / 60, timer % 60)
        } else {
            timerDisplayTextView!!.text = String.format(
                    Locale.ENGLISH, "Czas działania: %1\$d:%2\$d", timer / 60, timer % 60)
        }
    }

    /**
     * Builds and displays Android notification
     */
    private fun sendNotification(title: String, text: String, type: Boolean) {
        val mBuilder = NotificationCompat.Builder(this)
        mBuilder.setContentTitle(title)
        mBuilder.setContentText(text)

        if (type) {
            val pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
            mBuilder.setVibrate(pattern)
            mBuilder.setSmallIcon(android.R.drawable.ic_dialog_alert)
            mBuilder.setLights(Color.RED, 500, 500)
            mBuilder.setLargeIcon(notifyIcon2)
        } else {
            mBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
            mBuilder.setLights(Color.BLUE, 500, 500)
            mBuilder.setLargeIcon(notifyIcon)
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder.setSound(alarmSound)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(notificationCounter++, mBuilder.build())
    }
}
