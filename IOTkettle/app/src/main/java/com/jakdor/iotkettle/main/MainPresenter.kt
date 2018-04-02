package com.jakdor.iotkettle.main

import com.jakdor.iotkettle.mvp.BasePresenter
import java.util.*
import java.util.concurrent.TimeUnit

class MainPresenter(view: MainContract.MainView)
    : BasePresenter<MainContract.MainView>(view), MainContract.MainPresenter {

    lateinit var connectionString: String

    private var timerFlag = false
    private var timerStart: Long = 0
    private var timer: Long = 0

    override fun start() {
        super.start()
        view.startService(connectionString)
    }

    override fun onIpChanged() {
        val newIp = view.getIpEditText()

        if (connectionString != newIp) {
            view.saveIp(newIp)
            connectionString = newIp
        }

        view.changeServiceIp(connectionString)
    }

    override fun connect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receive() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}