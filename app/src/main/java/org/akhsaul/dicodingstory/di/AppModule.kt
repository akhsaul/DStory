package org.akhsaul.dicodingstory.di

import android.content.Context
import android.location.Geocoder
import androidx.datastore.preferences.preferencesDataStore
import org.akhsaul.core.BuildConfig
import org.akhsaul.core.di.coreModule
import org.akhsaul.core.util.Settings
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.ui.detail.DetailViewModel
import org.akhsaul.dicodingstory.ui.home.HomeViewModel
import org.akhsaul.dicodingstory.ui.login.LoginViewModel
import org.akhsaul.dicodingstory.ui.maps.MapsViewModel
import org.akhsaul.dicodingstory.ui.register.RegisterViewModel
import org.akhsaul.dicodingstory.ui.story.AddStoryViewModel
import org.akhsaul.dicodingstory.util.MyGeocoder
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.includes
import org.koin.dsl.lazyModule
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(BuildConfig.LIBRARY_PACKAGE_NAME)

val settingsModule = module(createdAtStart = true) {
    single {
        val settingDataStore = get<Context>().dataStore
        val resources = get<Context>().resources

        Settings(settingDataStore).apply {
            init(
                resources, resources.getStringArray(R.array.language_values),
                resources.getString(R.string.key_theme_mode),
                resources.getString(R.string.key_language),
            )
        }
    }
}

val geocoderModule = module(createdAtStart = true) {
    single {
        Geocoder(get<Context>())
    }
    singleOf(::MyGeocoder)
}

val viewModelModule = lazyModule {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddStoryViewModel)
    viewModelOf(::DetailViewModel)
    viewModelOf(::MapsViewModel)
}

val appModule = module {
    includes(settingsModule)
    includes(geocoderModule)
    includes(coreModule)
    includes(viewModelModule)
}