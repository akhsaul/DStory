package org.akhsaul.core.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ConverterUTCToZoneDeserializer(
    private val zone: ZoneId = ZoneId.systemDefault()
) : JsonDeserializer<ZonedDateTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ZonedDateTime? {
        if (json?.isJsonNull == true) {
            return null
        }

        if (json?.isJsonPrimitive == false || json?.asJsonPrimitive?.isString == false) {
            throw JsonParseException(
                "Expected a string type to parse for ZonedDateTime, but got ${json::class.simpleName}"
            )
        }

        val dateString = json?.asString
        return if (dateString == null) {
            null
        } else {
            runCatching {
                Instant.parse(dateString).atZone(zone)
            }.getOrElse {
                throw JsonParseException(
                    "Failed to parse UTC date string: $dateString to zone: $zone",
                    it
                )
            }
        }
    }
}