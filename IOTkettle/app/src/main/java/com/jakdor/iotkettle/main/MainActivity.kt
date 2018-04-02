package com.jakdor.iotkettle.main

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView

import com.jakdor.iotkettle.R
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainContract.MainView {

    @Inject
    lateinit var presenter: MainPresenter

    private var statusTextView: TextView? = null
    private var timerDisplayTextView: TextView? = null
    private var ipTextEdit: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById<View>(R.id.textView) as TextView
        timerDisplayTextView = findViewById<View>(R.id.timerDisplayTextView) as TextView
        ipTextEdit = findViewById<View>(R.id.editText) as EditText

        findViewById<View>(R.id.button).setOnClickListener(presenter.onIpChangedButtonListener())
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    /**
     * Provides Activity Context to presenter
     */
    override fun getViewContext(): Context {
        return this
    }

    /**
     * Returns String from Resources
     */
    override fun getResourcesString(resId: Int): String {
        return getString(resId)
    }

    override fun setIpEditText(ip: String) {
        ipTextEdit!!.setText(ip)
    }

    override fun getIpEditText(): String {
        return ipTextEdit!!.text.toString()
    }

    override fun setStatusTextView(status: String) {
        statusTextView!!.text = status
    }

    override fun setTimerDisplayTextView(time: String) {
        timerDisplayTextView!!.text = time
    }
}
