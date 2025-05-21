package org.akhsaul.core.data.source.remote.reponse

data class DetailStoryResponse(
    val error: String,
    val message: String,
    val story: StoryResponse
)
