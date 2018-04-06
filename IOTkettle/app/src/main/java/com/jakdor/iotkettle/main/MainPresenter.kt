package com.jakdor.iotkettle.main

import com.jakdor.iotkettle.R
import com.jakdor.iotkettle.mvp.BasePresenter
import java.util.*
import java.util.concurrent.TimeUnit

class MainPresenter(view: MainContract.MainView)
    : BasePresenter<MainContract.MainView>(view), MainContract.MainPresenter {

    lateinit var connectionString: String

    private var timerFlag = false
    private var timerStart: Long = 0
    private var timer: Long = 0

    /**
     * Presenter start
     */
    override fun start() {
        super.start()
        view.startService(connectionString)
    }

    /**
     * Presenter response to ui IpChangeButton
     */
    override fun onIpChanged() {
        val newIp = view.getIpEditText()

        if (connectionString != newIp) {
            view.saveIp(newIp)
            connectionString = newIp
        }

        view.changeServiceIp(connectionString)
    }

    override fun connected() {
        view.setStatusTextView(R.string.status_connected)
    }

    override fun connecting() {
        view.setStatusTextView(R.string.status_connecting)
    }

    override fun disconnect() {
        view.setStatusTextView(R.string.status_no_connection)
    }

    override fun userDisconnect() {
        view.stopService()
        view.setStatusTextView(R.string.status_no_connection)
        view.stopTimer()
        view.setTimerDisplayTextView("")
    }

    override fun receive(start: Boolean) {
        if(start){
            timerFlag = true
            view.startTimer()
            view.setStatusTextView(R.string.status_working)
        } else {
            timerFlag = false
            view.stopTimer()
            view.setStatusTextView(R.string.status_ended)
        }
    }

    /**
     * Presenter response to received state of [IOTService]
     */
    override fun stateChangeListener(appState: AppState) {
        when (appState){
            AppState.START -> receive(true)
            AppState.STOP -> receive(false)
            AppState.CONNECTING -> connecting()
            AppState.CONNECTED -> connected()
            AppState.DISCONNECTED -> disconnect()
        }
    }

    /**
     * Elapsed time counter
     */
    override fun timeCounter() {
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
}