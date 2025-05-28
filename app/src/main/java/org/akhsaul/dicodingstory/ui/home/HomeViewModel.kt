package org.akhsaul.dicodingstory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.model.Story
import org.akhsaul.core.domain.repository.StoryRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class HomeViewModel : ViewModel(), KoinComponent {
    private val storyRepository: StoryRepository by inject()
    private val _currentListStory = MutableStateFlow<List<Story>>(emptyList())
    val currentListStory = _currentListStory
    private val refreshTrigger = MutableSharedFlow<Unit>()

    fun triggerRefresh() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val stateFetchListStory = refreshTrigger.onStart {
        emit(Unit)
    }.flatMapLatest {
        storyRepository.getAllStory()
    }.catch {
        emit(Result.Error("Unexpected Error"))
    }.onEach {
        if (it is Result.Success) {
            viewModelScope.launch {
                _currentListStory.emit(it.data)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds),
        Result.Loading
    )
}