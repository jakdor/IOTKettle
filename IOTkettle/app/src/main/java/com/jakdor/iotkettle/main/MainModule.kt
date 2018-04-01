package com.jakdor.iotkettle.main

import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    fun provideMainPresenter(mainView: MainContract.MainView,
                             iotClient: IOTClient,
                             iotHelper: IOTHelper): MainPresenter {
        return MainPresenter(mainView, iotClient, iotHelper)
    }
}