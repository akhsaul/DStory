package org.akhsaul.dicodingstory.ui.detail

import androidx.lifecycle.ViewModel
import org.akhsaul.core.domain.model.Story

class DetailViewModel : ViewModel() {
    private var currentStory: Story? = null
    fun setStory(story: Story) {
        currentStory = story
    }

    fun getStory() = requireNotNull(currentStory)
}