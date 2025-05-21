package org.akhsaul.core.domain.model

import java.time.LocalDateTime

data class Story(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: LocalDateTime,
    val lat: Double,
    val lon: Double
)

