package org.akhsaul.dicodingstory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import org.akhsaul.core.domain.repository.StoryRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {
    private val storyRepository: StoryRepository by inject()
    val storyPaging = storyRepository.getAllStoryWithPaging()
        .cachedIn(viewModelScope)
}