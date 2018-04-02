package com.jakdor.iotkettle.main

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun loadIp()
        fun saveIp(ip: String)
        fun setIpChangedButtonListener()

        fun startService(ip: String)
        fun changeServiceIp(ip: String)
        fun stopService()

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
        fun connect()
        fun receive()
        fun displayTimer()
    }
}