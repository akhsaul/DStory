package org.akhsaul.core.data.source.remote.reponse

data class StoryResponse(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
    val lat: Double,
    val lon: Double
)
