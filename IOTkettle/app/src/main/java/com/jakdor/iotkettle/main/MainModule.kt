package com.jakdor.iotkettle.main

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    fun provideMainPresenter(mainView: MainContract.MainView): MainPresenter {
        return MainPresenter(mainView)
    }

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences{
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}