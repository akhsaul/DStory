package org.akhsaul.core.data.source.remote.reponse

import java.time.ZonedDateTime

data class StoryResponse(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: ZonedDateTime,
    val lat: Double,
    val lon: Double
)
