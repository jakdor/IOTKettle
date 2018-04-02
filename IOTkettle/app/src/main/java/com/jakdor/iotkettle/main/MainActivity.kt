package com.jakdor.iotkettle.main

import kotlinx.android.synthetic.main.activity_main.*

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat

import com.jakdor.iotkettle.R
import com.jakdor.iotkettle.network.IOTService
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainContract.MainView {

    @Inject
    lateinit var presenter: MainPresenter

    private lateinit var preferences: SharedPreferences

    private lateinit var notifyIcon: Bitmap
    private lateinit var notifyIcon2: Bitmap

    private var notificationCounter = 0
    private var channel: NotificationChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        notifyIcon = BitmapFactory.decodeResource(resources, R.drawable.kettler)
        notifyIcon2 = BitmapFactory.decodeResource(resources, R.drawable.kettler2)

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
       changeIpButton.setOnClickListener({
           presenter.onIpChanged()
           //test
           startService()})
    }

    /**
     * Builds and displays Android notification
     */
    override fun sendNotification(title: String, text: String, type: Boolean) {
        val notifBuilder = NotificationCompat.Builder(
                this, getString(R.string.notification_chanel_id))
        notifBuilder.setContentTitle(title)
        notifBuilder.setContentText(text)

        if (type) {
            val pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
            notifBuilder.setVibrate(pattern)
            notifBuilder.setSmallIcon(android.R.drawable.ic_dialog_alert)
            notifBuilder.setLights(Color.RED, 500, 500)
            notifBuilder.setLargeIcon(notifyIcon2)
            notifBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        } else {
            notifBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
            notifBuilder.setLights(Color.BLUE, 500, 500)
            notifBuilder.setLargeIcon(notifyIcon)
            notifBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        notifBuilder.setSound(alarmSound)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(channel == null){
                setupNotificationChanel()
                notificationManager.createNotificationChannel(channel)
            }
        }

        notificationManager.notify(notificationCounter++, notifBuilder.build())
    }

    /**
     * Setup Notification Chanel for API>=26
     */
    private fun setupNotificationChanel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = getString(R.string.notification_chanel_id)
            val name = getString(R.string.notification_chanel_name)
            val description = getString(R.string.notification_chanel_desc)
            channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel?.description = description
        }
    }

    /**
     * Start IOTService
     */
    override fun startService() {
        val startIntent = Intent(this@MainActivity, IOTService::class.java)
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

    override fun setNotificationCounter(id: Int) {
        notificationCounter = id
    }

    override fun getNotificationCounter(): Int {
        return notificationCounter
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
