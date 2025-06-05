package org.akhsaul.core.data.model.reponse

data class AllStoryResponse(
    val error: String,
    val message: String,
    val listStory: List<StoryResponse>? = null
)
