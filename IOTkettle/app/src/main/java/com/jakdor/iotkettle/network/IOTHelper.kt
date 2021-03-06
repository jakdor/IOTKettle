package com.jakdor.iotkettle.network

/**
 * Helper class
 */
class IOTHelper : Thread() {

    lateinit var iotClient: IOTClient

    var received: String? = null
        private set
    var isNotifyFlag = false
        private set

    override fun run() {
        while (true) {
            if (iotClient.isConnectionOK) {
                received = iotClient.receive()
                if (received != null) {
                    isNotifyFlag = true
                }
            }

            try {
                Thread.sleep(10)
            } catch (e: Exception) {}
        }
    }

    fun changeIotClient(iotClient: IOTClient) {
        this.iotClient = iotClient
    }

    fun notifyHandled() {
        isNotifyFlag = false
    }
}
