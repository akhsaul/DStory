package org.akhsaul.dicodingstory.di

import android.content.Context
import android.location.Geocoder
import androidx.datastore.preferences.preferencesDataStore
import org.akhsaul.core.BuildConfig
import org.akhsaul.core.Settings
import org.akhsaul.core.di.coreModule
import org.akhsaul.dicodingstory.ui.detail.DetailViewModel
import org.akhsaul.dicodingstory.ui.home.HomeViewModel
import org.akhsaul.dicodingstory.ui.login.LoginViewModel
import org.akhsaul.dicodingstory.ui.register.RegisterViewModel
import org.akhsaul.dicodingstory.ui.story.AddStoryViewModel
import org.akhsaul.dicodingstory.util.MyGeocoder
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(BuildConfig.LIBRARY_PACKAGE_NAME)
val appModule = module {
    single(createdAtStart = true) {
        Settings(get<Context>().dataStore).apply {
            initThemeMode(get<Context>().resources)
        }
    }
    single(createdAtStart = true) {
        Geocoder(get<Context>())
    }
    single(createdAtStart = true) {
        MyGeocoder()
    }
    includes(coreModule)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddStoryViewModel)
    viewModelOf(::DetailViewModel)
}