package com.jakdor.iotkettle.main

import android.util.Log

/**
 * Helper class, keeps connection up despite app state
 * todo refactor into service
 */
internal class IOTHelper(private var iotClient: IOTClient?) : Thread() {

    var received: String? = null
        private set
    var isNotifyFlag = false
        private set

    override fun run() {
        while (true) {
            if (iotClient!!.isConnectionOK) {
                received = iotClient!!.receive()
                if (received != null) {
                    isNotifyFlag = true
                }
            }

            try {
                Thread.sleep(10)
            } catch (e: Exception) {
                Log.e("Exception", e.message)
            }

        }
    }

    fun changeIotClient(iotClient: IOTClient) {
        this.iotClient = iotClient
    }

    fun notifyHandled() {
        isNotifyFlag = false
    }
}
