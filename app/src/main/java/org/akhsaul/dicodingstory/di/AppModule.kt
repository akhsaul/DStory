package org.akhsaul.dicodingstory.di

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import org.akhsaul.core.BuildConfig
import org.akhsaul.core.Settings
import org.akhsaul.core.di.coreModule
import org.akhsaul.dicodingstory.ui.home.HomeViewModel
import org.akhsaul.dicodingstory.ui.login.LoginViewModel
import org.akhsaul.dicodingstory.ui.register.RegisterViewModel
import org.akhsaul.dicodingstory.ui.story.AddStoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(BuildConfig.LIBRARY_PACKAGE_NAME)
val appModule = module {
    single(createdAtStart = true) {
        Settings(get<Context>().dataStore).apply {
            initThemeMode(get<Context>().resources)
        }.apply {
            Log.i("AppModule", "Settings: ${this@apply.hashCode()}")
        }
    }
    includes(coreModule)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddStoryViewModel)
}