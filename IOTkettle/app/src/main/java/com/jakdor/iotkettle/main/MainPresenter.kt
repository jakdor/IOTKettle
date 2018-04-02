package com.jakdor.iotkettle.main

import com.jakdor.iotkettle.R
import com.jakdor.iotkettle.mvp.BasePresenter
import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.os.Handler

class MainPresenter(view: MainContract.MainView,
                    private val iotClient: IOTClient,
                    private val iotHelper: IOTHelper)
    : BasePresenter<MainContract.MainView>(view), MainContract.MainPresenter {

    lateinit var connectionString: String

    private var timerFlag = false
    private var timerStart: Long = 0
    private var timer: Long = 0

    private var retryCounter = 0

    override fun start(){
        super.start()
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
     * Defines behaviour for IpChangedButton onClick Event
     */
    override fun onIpChanged(){
        val newIp = view.getIpEditText()

        if (connectionString != newIp) {
            view.saveIp(newIp)
            connectionString = newIp
        }

        iotClient.kill()
        connect()

        view.setNotificationCounter(0)
        iotHelper.changeIotClient(iotClient)

    }

    /**
     * Start connection threat
     */
    override fun connect() {
        view.setStatusTextView(R.string.status_connecting)
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
                view.sendNotification("Czajnik uruchomiony", time, false)
                view.setStatusTextView(R.string.status_working)
                timerFlag = true
            } else if (received == "stop1") {
                view.sendNotification("Czajnik wyłączony", time, true)
                view.setStatusTextView(R.string.status_ended)
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
            view.setStatusTextView(R.string.status_no_connection)
            if (retryCounter > 5) {
                iotClient.kill()
                connect()
                iotHelper.changeIotClient(iotClient)
                retryCounter = 0
            }
        } else if (view.getNotificationCounter() == 0) {
            view.setStatusTextView(R.string.status_text)
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

}