package org.akhsaul.dicodingstory.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.akhsaul.core.domain.model.Register
import org.akhsaul.core.domain.repository.AuthRepository
import kotlin.time.Duration.Companion.seconds

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val registerState = MutableSharedFlow<Register>()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val registerResult = registerState
        .debounce(1.seconds)
        .flatMapLatest {
            authRepository.register(it.name, it.email, it.password)
        }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            registerState.emit(Register(name, email, password))
        }
    }
}