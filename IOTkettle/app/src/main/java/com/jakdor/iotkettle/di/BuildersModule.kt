package com.jakdor.iotkettle.di

import com.jakdor.iotkettle.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {

    @ContributesAndroidInjector()
    abstract fun bindMainActivity(): MainActivity
}