package org.akhsaul.dicodingstory.di

import android.app.Application
import kotlinx.coroutines.Dispatchers
import org.akhsaul.dicodingstory.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.extension.coroutinesEngine
import org.koin.core.logger.Level
import org.koin.dsl.koinConfiguration

@OptIn(KoinExperimentalAPI::class)
class MyApplication : Application(), KoinStartup {

    override fun onKoinStartup() = koinConfiguration {
        androidContext(this@MyApplication)
        androidLogger(
            if (BuildConfig.DEBUG) {
                Level.DEBUG
            } else {
                Level.NONE
            }
        )
        coroutinesEngine(Dispatchers.IO)
        modules(appModule)
    }
}