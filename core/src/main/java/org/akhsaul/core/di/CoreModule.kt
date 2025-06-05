package org.akhsaul.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import org.akhsaul.core.BuildConfig
import org.akhsaul.core.data.AuthRepositoryImpl
import org.akhsaul.core.data.StoryRepositoryImpl
import org.akhsaul.core.data.source.remote.network.ApiService
import org.akhsaul.core.domain.repository.AuthRepository
import org.akhsaul.core.domain.repository.StoryRepository
import org.akhsaul.core.util.AuthManager
import org.akhsaul.core.util.ConverterUTCToZoneDeserializer
import org.akhsaul.core.util.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.includes
import org.koin.dsl.lazyModule
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

val httpModule = lazyModule {
    single {
        AuthManager(get<Settings>())
    }
    single<OkHttpClient> {
        val certificatePinner = CertificatePinner.Builder()
            .add(BuildConfig.HOST_NAME, BuildConfig.CERT_PIN1)
            .add(BuildConfig.HOST_NAME, BuildConfig.CERT_PIN2)
            .add(BuildConfig.HOST_NAME, BuildConfig.CERT_PIN3)
            .build()

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner)

        clientBuilder.addInterceptor {
            val authManager = get<AuthManager>()

            var newRequest = it.request().newBuilder()
                .addHeader("User-Agent", "github@Akhsaul")
            val token = authManager.getAuthToken()
            if (token != null) {
                newRequest.addHeader("Authorization", "Bearer $token")
            }

            val response = it.proceed(newRequest.build())
            if (response.code == 401) {
                authManager.triggerAuthExpired()
            }

            response
        }

        clientBuilder.build()
    }

    single<ApiService> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .client(get<OkHttpClient>())
            .build()
            .create(ApiService::class.java)
    }
}

val gsonModule = module(createdAtStart = true) {
    single {
        GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ZonedDateTime::class.java, ConverterUTCToZoneDeserializer())
            .create()
    }
}

val repositoryModule = lazyModule {
    singleOf<AuthRepository>(::AuthRepositoryImpl)
    singleOf<StoryRepository>(::StoryRepositoryImpl)
}

val coreModule = lazyModule {
    includes(gsonModule)
    includes(httpModule)
    includes(repositoryModule)
}