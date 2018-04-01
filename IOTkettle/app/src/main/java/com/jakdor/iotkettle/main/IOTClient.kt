package com.jakdor.iotkettle.main

import android.util.Log

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/**
 * Handles connection between app and device
 */
class IOTClient(private val connectIP: String) : Thread() {

    private var receiveMessage: String? = null

    private var sock: Socket? = null
    private var printWriter: PrintWriter? = null
    private var receiveRead: BufferedReader? = null

    var isConnectionOK = false
        private set

    override fun run() {
        isConnectionOK = connect()
        super.run()
    }

    /**
     * Opens connection socket
     */
    fun connect(): Boolean {
        try {
            sock = Socket(connectIP, 8889)

            val ostream = sock!!.getOutputStream()
            printWriter = PrintWriter(ostream, true)

            val istream = sock!!.getInputStream()
            receiveRead = BufferedReader(InputStreamReader(istream))
            return true
        } catch (e: Exception) {
            Log.e("Exception", "Client connection problem: " + e.toString())
        }

        return false
    }

    /**
     * Incoming data listener
     */
    fun receive(): String? {
        try {
            receiveMessage = receiveRead!!.readLine()
            if (receiveMessage != null) {
                return receiveMessage
            }
        } catch (e: Exception) {
            Log.e("Exception", "Client receive problem: " + e.toString())
        }

        return null
    }

    /**
     * Send data method
     */
    fun send(input: String) {
        try {
            printWriter!!.println(input)
            printWriter!!.flush()
        } catch (e: Exception) {
            Log.e("Exception", "Client send problem: " + e.toString())
        }
    }

    /**
     * closes connection
     */
    fun kill() {
        try {
            sock!!.close()
        } catch (e: Exception) {
            Log.e("Exception", "Client can't close connection: " + e.toString())
        }
    }
}