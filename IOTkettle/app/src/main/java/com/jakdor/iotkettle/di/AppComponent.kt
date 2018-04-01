package com.jakdor.iotkettle.di

import com.jakdor.iotkettle.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Dagger AppComponent configuration
 */
@Singleton
@Component(modules = [(AndroidSupportInjectionModule::class),
    (AppModule::class),
    (BuildersModule::class)])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: App) : Builder
        fun build() : AppComponent
    }

    fun inject(app: App)
}