package org.akhsaul.core.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.akhsaul.core.catchNoInternet
import org.akhsaul.core.data.source.remote.network.ApiService
import org.akhsaul.core.domain.model.User
import org.akhsaul.core.domain.repository.AuthRepository
import org.akhsaul.core.getErrorResponse
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthRepositoryImpl : AuthRepository, KoinComponent {
    private val apiService: ApiService by inject()

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
            val errorResponse = apiResult.getErrorResponse()
            emit(Result.Error(errorResponse?.message ?: apiResult.message()))
        }
    }.catchNoInternet().onStart {
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
            val errorResponse = apiResult.getErrorResponse()
            emit(Result.Error(errorResponse?.message ?: apiResult.message()))
        }
    }.catchNoInternet().onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)
}