package org.akhsaul.core.di

import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import org.akhsaul.core.BuildConfig
import org.akhsaul.core.Settings
import org.akhsaul.core.data.AuthRepositoryImpl
import org.akhsaul.core.data.StoryRepositoryImpl
import org.akhsaul.core.data.source.remote.network.ApiService
import org.akhsaul.core.domain.repository.AuthRepository
import org.akhsaul.core.domain.repository.StoryRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val coreModule = module {
    single<OkHttpClient> {
        val certificatePinner = CertificatePinner.Builder()
            .add(BuildConfig.HOSTNAME, "sha256/bKTIluDN5O7wQKDoVBap/2FVvNQkOlv6Uivq+D44YH4=")
            .add(BuildConfig.HOSTNAME, "sha256/bdrBhpj38ffhxpubzkINl0rG+UyossdhcBYj+Zx2fcc=")
            .add(BuildConfig.HOSTNAME, "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=")
            .build()

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner)

        clientBuilder.addInterceptor {
            var newRequest = it.request()
            val settings = get<Settings>()
            Log.i("CoreModule", "Settings: ${settings.hashCode()}")
            val token = settings.getAuthToken()
            if (token != null) {
                newRequest = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("User-Agent", "@Akhsaul")
                    .build()
            }
            it.proceed(newRequest)
        }

        clientBuilder.build()
    }
    single<ApiService> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
            .create(ApiService::class.java)
    }
    singleOf<AuthRepository>(::AuthRepositoryImpl)
    singleOf<StoryRepository>(::StoryRepositoryImpl)
}