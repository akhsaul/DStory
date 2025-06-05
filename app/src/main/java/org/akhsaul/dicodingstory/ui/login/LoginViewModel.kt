package org.akhsaul.dicodingstory.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.akhsaul.core.data.repository.AuthRepository
import org.akhsaul.core.util.Result
import org.akhsaul.dicodingstory.data.model.domain.Login
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
            emit(Result.Error("Unexpected Error"))
        }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            dataLogin.emit(Login(username, password))
        }
    }
}