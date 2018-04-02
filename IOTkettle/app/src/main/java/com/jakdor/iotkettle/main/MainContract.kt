package com.jakdor.iotkettle.main

import android.content.Context
import android.view.View

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun setIpEditText(ip: String)
        fun getIpEditText(): String
        fun setStatusTextView(status: String)
        fun setTimerDisplayTextView(time: String)
        fun getViewContext(): Context
        fun getResourcesString(resId: Int): String
    }

    interface MainPresenter {
        fun loadIp()
        fun saveIp(ip: String)
        fun onIpChangedButtonListener() : View.OnClickListener
        fun connect()
        fun receive()
        fun checkConnection()
        fun displayTimer()
        fun sendNotification(title: String, text: String, type: Boolean)
    }
}