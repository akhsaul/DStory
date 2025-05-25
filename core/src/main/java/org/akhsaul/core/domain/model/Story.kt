package org.akhsaul.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class Story(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: ZonedDateTime,
    val lat: Double,
    val lon: Double
) : Parcelable

