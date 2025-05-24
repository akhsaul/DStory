package org.akhsaul.dicodingstory.ui.story

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.repository.StoryRepository
import org.akhsaul.dicodingstory.ui.home.StoryRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class AddStoryViewModel : ViewModel(), KoinComponent {
    private val storyRepository: StoryRepository by inject()
    val currentLocation = MutableStateFlow<Location?>(null)
    private val storyFlow = MutableSharedFlow<StoryRequest>()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val addStoryResult: Flow<Result<String>> = storyFlow.map {
        if (it.photo.exists() && it.description.isNotBlank()) {
            it
        } else {
            null
        }
    }.filterNotNull().transformLatest { story ->
        // make ui show progress bar
        emit(Result.Loading)
        // wait 30 seconds, if it's still running
        // then throw TimeoutCancellationException
        val location = withTimeout(30.seconds) {
            // always collecting flow
            // stop when it's value is not null
            currentLocation.filterNotNull().first()
        }
        storyRepository.addStory(
            story.photo,
            story.description,
            location.latitude,
            location.longitude
        ).collect {
            // Don't emit Result.Loading again
            if (it is Result.Success || it is Result.Error) {
                emit(it)
            }
        }
    }.catch {
        if (it is TimeoutCancellationException) {
            emit(Result.Error("Can't get location!"))
        } else {
            emit(Result.Error(it.message))
        }
    }

    fun addStory(story: StoryRequest) {
        viewModelScope.launch {
            storyFlow.emit(story)
        }
    }
}