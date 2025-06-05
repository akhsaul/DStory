package org.akhsaul.core.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.akhsaul.core.data.model.domain.User
import org.akhsaul.core.data.source.remote.ApiService
import org.akhsaul.core.util.Result
import org.akhsaul.core.util.catchNoNetwork
import org.akhsaul.core.util.catchSSLError
import org.akhsaul.core.util.getErrorResponse
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthRepositoryImpl : AuthRepository, KoinComponent {
    private val apiService: ApiService by inject()
    private val gson: Gson by inject()

    override fun register(name: String, email: String, password: String) = flow {
        val apiResult = apiService.register(name, email, password)

        if (apiResult.isSuccessful) {
            val response = apiResult.body()

            if (response == null) {
                emit(Result.Error("Response is null"))
            } else {
                emit(Result.Success(response.message))
            }
        } else {
            val errorResponse = apiResult.getErrorResponse(gson)
            emit(Result.Error(errorResponse?.message ?: apiResult.message()))
        }
    }.catchNoNetwork().catchSSLError().onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)

    override fun login(email: String, password: String) = flow {
        val apiResult = apiService.login(email, password)
        if (apiResult.isSuccessful) {
            val user = apiResult.body()?.loginResult?.let {
                User(it.userId, it.name, it.token)
            }

            if (user == null) {
                emit(Result.Error("User is null"))
            } else {
                emit(Result.Success(user))
            }
        } else {
            val errorResponse = apiResult.getErrorResponse(gson)
            emit(Result.Error(errorResponse?.message ?: apiResult.message()))
        }
    }.catchNoNetwork().catchSSLError().onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)
}