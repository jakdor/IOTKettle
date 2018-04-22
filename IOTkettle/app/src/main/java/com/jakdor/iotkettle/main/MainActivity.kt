package com.jakdor.iotkettle.main

import android.content.*
import kotlinx.android.synthetic.main.activity_main.*

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.app.ActivityManager

import com.jakdor.iotkettle.R
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainContract.MainView {

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setIpChangedButtonListener()
        setDisconnectButtonListener()

        loadIp()
        presenter.start()
        setIpEditText(presenter.connectionString)
        registerReceiver(iotServiceReceiver, IntentFilter("IoTKettleAppState"))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
        unregisterReceiver(iotServiceReceiver)
    }

    /**
     * Receiver forwarding [IOTService] state to [MainPresenter]
     */
    private val iotServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            presenter.stateChangeListener(intent.extras.get("state") as AppState)
        }
    }

    private var timerHandler = Handler()
    private var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            presenter.timeCounter()
            timerHandler.postDelayed(this, 1000)
        }
    }

    /**
     * Start timer displaying elapsed time
     */
    override fun startTimer() {
        timerHandler.postDelayed(timerRunnable, 0)
    }

    /**
     * Stop timer displaying elapsed time
     */
    override fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
    }

    /**
     * Load IP String with SharedPreferencesManager
     */
    override fun loadIp() {
        presenter.connectionString = preferences.getString(getString(R.string.ip_string),
                getString(R.string.default_ip_string))
    }

    /**
     * Save IP String with SharedPreferencesManager
     */
    override fun saveIp(ip: String) {
        val editor = preferences.edit()
        editor.putString(getString(R.string.ip_string), ip)
        editor.apply()
    }

    /**
     * Call [MainPresenter.onIpChanged] on ChangeIpButton click
     */
    override fun setIpChangedButtonListener() {
       changeIpButton.setOnClickListener({ presenter.onIpChanged() })
    }

    /**
     * Call [MainPresenter.userDisconnect] on DisconnectButton click
     */
    override fun setDisconnectButtonListener() {
        disconnectButton.setOnClickListener({ presenter.userDisconnect() })
    }

    /**
     * Start IOTService
     */
    override fun startService(ip: String) {
        if(!isServiceRunning(IOTService::class.java)) {
            val startIntent = Intent(this@MainActivity, IOTService::class.java)
            startIntent.putExtra("ip", ip)
            startIntent.action = IOTService.ACTION.START_ACTION
            startService(startIntent)
        }
    }

    /**
     * Stop IOTService
     */
    override fun stopService() {
        val stopIntent = Intent(this@MainActivity, IOTService::class.java)
        stopIntent.action = IOTService.ACTION.STOP_ACTION
        startService(stopIntent)
    }

    /**
     * Check if service is already running
     */
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) { //todo find replacement
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Notify service about ip change
     */
    override fun changeServiceIp(ip: String) {
        val changeIpIntent = Intent(this@MainActivity, IOTService::class.java)
        changeIpIntent.putExtra("ip", ip)
        changeIpIntent.action = IOTService.ACTION.IP_CHANGE_ACTION
        startService(changeIpIntent)
    }

    override fun setIpEditText(ip: String) {
        ipEditText.setText(ip)
    }

    override fun setIpEditText(resId: Int) {
        ipEditText.setText(resId)
    }

    override fun getIpEditText(): String {
        return ipEditText.text.toString()
    }

    override fun setStatusTextView(status: String) {
        statusTextView!!.text = status
    }

    override fun setStatusTextView(resId: Int) {
       statusTextView!!.setText(resId)
    }

    override fun setTimerDisplayTextView(time: String) {
        timerDisplayTextView!!.text = time
    }

    override fun setTimerDisplayTextView(resId: Int) {
        timerDisplayTextView!!.setText(resId)
    }
}
