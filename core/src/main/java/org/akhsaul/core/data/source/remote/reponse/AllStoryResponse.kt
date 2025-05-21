package org.akhsaul.core.data.source.remote.reponse

data class AllStoryResponse(
    val error: String,
    val message: String,
    val listStory: List<StoryResponse>
)
