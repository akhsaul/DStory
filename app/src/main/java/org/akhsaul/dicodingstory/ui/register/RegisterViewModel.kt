package org.akhsaul.dicodingstory.ui.register

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
import org.akhsaul.core.data.repository.AuthRepository
import org.akhsaul.core.util.Result
import org.akhsaul.dicodingstory.data.model.domain.Register
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class RegisterViewModel() : ViewModel(), KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val dataRegister = MutableSharedFlow<Register>()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val registerResult = dataRegister
        .debounce(1.seconds)
        .flatMapLatest {
            authRepository.register(it.name, it.email, it.password)
        }.catch {
            Log.e("RegisterViewModel", "Error: $it")
            emit(Result.Error("Unexpected Error"))
        }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            dataRegister.emit(Register(name, email, password))
        }
    }
}