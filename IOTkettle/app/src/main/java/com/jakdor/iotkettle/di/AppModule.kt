package com.jakdor.iotkettle.di

import android.content.Context
import com.jakdor.iotkettle.App
import com.jakdor.iotkettle.network.IOTClient
import com.jakdor.iotkettle.network.IOTHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun provideContext(app: App): Context {
        return app.applicationContext
    }

    @Singleton
    @Provides
    fun provideIOTClient(): IOTClient {
        return IOTClient()
    }

    @Singleton
    @Provides
    fun provideIOTHelper(): IOTHelper {
        return IOTHelper()
    }
}