package org.akhsaul.dicodingstory.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.model.Login
import org.akhsaul.core.domain.repository.AuthRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class LoginViewModel() : ViewModel(), KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val dataLogin = MutableSharedFlow<Login>()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val loginResult = dataLogin
        .debounce(1.seconds)
        .flatMapLatest {
            authRepository.login(it.email, it.password)
        }.catch {
            Log.e(TAG, "Error when try to login", it)
            emit(Result.Error("Unexpected Error"))
        }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            dataLogin.emit(Login(username, password))
        }
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}