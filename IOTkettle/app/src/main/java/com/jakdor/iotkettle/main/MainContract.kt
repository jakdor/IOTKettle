package com.jakdor.iotkettle.main

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun loadIp()
        fun saveIp(ip: String)
        fun setIpChangedButtonListener()
        fun setDisconnectButtonListener()

        fun startService(ip: String)
        fun changeServiceIp(ip: String)
        fun stopService()

        fun startTimer()
        fun stopTimer()

        fun setIpEditText(ip: String)
        fun setIpEditText(resId: Int)
        fun getIpEditText(): String
        fun setStatusTextView(status: String)
        fun setStatusTextView(resId: Int)
        fun setTimerDisplayTextView(time: String)
        fun setTimerDisplayTextView(resId: Int)
    }

    interface MainPresenter {
        fun onIpChanged()
        fun connected()
        fun connecting()
        fun disconnect()
        fun userDisconnect()
        fun receive(start: Boolean)
        fun timeCounter()
        fun displayTimer()
        fun stateChangeListener(appState: AppState)
    }
}