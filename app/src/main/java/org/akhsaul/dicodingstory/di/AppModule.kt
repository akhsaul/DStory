package org.akhsaul.dicodingstory.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import org.akhsaul.core.BuildConfig
import org.akhsaul.core.Settings
import org.akhsaul.core.di.coreModule
import org.akhsaul.dicodingstory.ui.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore(BuildConfig.LIBRARY_PACKAGE_NAME)
val appModule = module {
    single(createdAtStart = true) {
        Settings(get<Context>().dataStore).apply {
            initThemeMode(get<Context>().resources)
        }
    }
    includes(coreModule)
    viewModelOf(::RegisterViewModel)
}