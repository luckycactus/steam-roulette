package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.json.JSONException
import ru.luckycactus.steamroulette.data.repositories.games.models.RequiredAgeEntity
import javax.inject.Inject

/**
 * Json field is string ("0", "6", etc) when there is info about required age and int 0 otherwise
 */
class RequiredAgeMoshiAdapter @Inject constructor() {
    @FromJson
    fun fromJson(jsonReader: JsonReader): RequiredAgeEntity? {
        return when (jsonReader.peek()) {
            JsonReader.Token.STRING -> {
                RequiredAgeEntity(jsonReader.nextString().toInt())
            }
            JsonReader.Token.NUMBER -> {
                jsonReader.nextInt()
                null
            }
            else -> throw JSONException("Cannot parse requiredAge")
        }
    }

    @ToJson
    fun toJson(requiredAge: RequiredAgeEntity): String {
        throw UnsupportedOperationException()
    }
}