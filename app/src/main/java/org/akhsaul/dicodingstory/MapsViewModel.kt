package org.akhsaul.dicodingstory

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.model.Story
import org.akhsaul.core.domain.repository.StoryRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MapsViewModel : ViewModel(), KoinComponent {
    private val storyRepository: StoryRepository by inject()
    private val _storyList = MutableStateFlow<List<Story>>(emptyList())
    val storyList: StateFlow<List<Story>> = _storyList
    val stateFetchListStory = storyRepository.getAllStory(size = 50, location = 1).catch {
        emit(Result.Error("Unexpected Error"))
    }.onEach {
        if (it is Result.Success) {
            val filtered = it.data.filter {
                (it.lat in -90.0..90.0) && (it.lon in -180.0..180.0)
            }
            _storyList.emit(filtered)
        }
        it
    }
}