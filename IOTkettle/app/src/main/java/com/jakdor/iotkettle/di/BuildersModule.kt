package com.jakdor.iotkettle.di

import com.jakdor.iotkettle.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * App sub-components binding point
 */
@Module
abstract class BuildersModule {

    @ContributesAndroidInjector()
    abstract fun bindMainActivity(): MainActivity
}