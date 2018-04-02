package com.jakdor.iotkettle.di

import com.jakdor.iotkettle.main.IOTService
import com.jakdor.iotkettle.main.MainActivity
import com.jakdor.iotkettle.main.MainModule
import com.jakdor.iotkettle.main.MainViewModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * App sub-components binding point
 */
@Module
abstract class BuildersModule {

    @ContributesAndroidInjector(modules = [(MainViewModule::class), (MainModule::class)])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindIOTService(): IOTService
}