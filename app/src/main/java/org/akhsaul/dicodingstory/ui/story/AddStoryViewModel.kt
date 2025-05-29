package org.akhsaul.dicodingstory.ui.story

import android.content.Context
import android.location.Location
import android.net.Uri
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.model.StoryRequest
import org.akhsaul.core.domain.repository.StoryRepository
import org.akhsaul.core.toFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

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
    }.filterNotNull().flatMapLatest { story ->
        val location = currentLocation.first()
        storyRepository.addStory(
            story.photo,
            story.description,
            location?.latitude ?: Random.nextDouble(-90.0, 90.0),
            location?.longitude ?: Random.nextDouble(-180.0, 180.0)
        )
    }.catch {
        if (it is TimeoutCancellationException) {
            emit(Result.Error("Can't get location!"))
        } else {
            emit(Result.Error("Unexpected Error"))
        }
    }

    fun addStory(context: Context, photo: Uri, description: String) {
        viewModelScope.launch {
            storyFlow.emit(StoryRequest(photo.toFile(context), description))
        }
    }
}