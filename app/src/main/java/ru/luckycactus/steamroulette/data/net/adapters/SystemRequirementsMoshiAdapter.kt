package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import ru.luckycactus.steamroulette.data.repositories.games.models.SystemRequirementsEntity
import javax.inject.Inject

class SystemRequirementsMoshiAdapter @Inject constructor() {
    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        delegate: JsonAdapter<SystemRequirementsEntity>
    ): SystemRequirementsEntity? {
        return when (jsonReader.peek()) {
            JsonReader.Token.NULL -> {
                jsonReader.nextNull<SystemRequirementsEntity>()
                null
            }
            JsonReader.Token.BEGIN_ARRAY -> {
                jsonReader.skipValue()
                null
            }
            else -> delegate.fromJson(jsonReader)
        }
    }

    @ToJson
    fun toJson(systemRequirements: SystemRequirementsEntity): String {
        throw UnsupportedOperationException()
    }
}