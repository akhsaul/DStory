package org.akhsaul.core.util

import org.akhsaul.core.data.model.domain.Story
import org.akhsaul.core.data.model.entity.StoryEntity
import org.akhsaul.core.data.model.reponse.StoryResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DataMapper {
    private val utcZone = ZoneId.of("UTC")
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    fun responseToEntity(response: StoryResponse): StoryEntity {
        val utcDateTime = response.createdAt.withZoneSameInstant(utcZone)

        return StoryEntity(
            id = response.id,
            name = response.name,
            description = response.description,
            photoUrl = response.photoUrl,
            createdAt = utcDateTime.format(formatter),
            lat = response.lat,
            lon = response.lon
        )
    }

    fun responseToDomain(response: StoryResponse): Story = Story(
        id = response.id,
        name = response.name,
        description = response.description,
        photoUrl = response.photoUrl,
        createdAt = response.createdAt,
        lat = response.lat,
        lon = response.lon
    )

    fun entityToDomain(entity: StoryEntity): Story {
        val utcDateTime = ZonedDateTime.parse(entity.createdAt, formatter)
            .withZoneSameInstant(utcZone)

        val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault())

        return Story(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            photoUrl = entity.photoUrl,
            createdAt = localDateTime,
            lat = entity.lat,
            lon = entity.lon
        )
    }
}