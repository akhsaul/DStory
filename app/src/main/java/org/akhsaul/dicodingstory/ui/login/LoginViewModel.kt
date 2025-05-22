package org.akhsaul.dicodingstory.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.akhsaul.core.domain.model.Login
import org.akhsaul.core.domain.repository.AuthRepository
import kotlin.time.Duration.Companion.seconds

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val dataLogin = MutableSharedFlow<Login>()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val loginResult = dataLogin
        .debounce(1.seconds)
        .flatMapLatest {
            authRepository.login(it.email, it.password)
        }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            dataLogin.emit(Login(username, password))
        }
    }
}