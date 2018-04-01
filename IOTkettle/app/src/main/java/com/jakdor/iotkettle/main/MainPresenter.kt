package com.jakdor.iotkettle.main

import com.jakdor.iotkettle.mvp.BasePresenter
import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper

class MainPresenter(view: MainContract.MainView,
                    private val iotClient: IOTClient,
                    private val iotHelper: IOTHelper)
    : BasePresenter<MainContract.MainView>(view), MainContract.MainPresenter {
}