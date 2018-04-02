package com.jakdor.iotkettle.main

import kotlinx.android.synthetic.main.activity_main.*

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager

import com.jakdor.iotkettle.R
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainContract.MainView {

    @Inject
    lateinit var presenter: MainPresenter

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        setIpChangedButtonListener()
    }

    override fun onStart() {
        super.onStart()
        loadIp()
        presenter.start()
        setIpEditText(presenter.connectionString)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
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
     * Start IOTService
     */
    override fun startService(ip: String) {
        val startIntent = Intent(this@MainActivity, IOTService::class.java)
        startIntent.putExtra("ip", ip)
        startIntent.action = IOTService.ACTION.START_ACTION
        startService(startIntent)
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
