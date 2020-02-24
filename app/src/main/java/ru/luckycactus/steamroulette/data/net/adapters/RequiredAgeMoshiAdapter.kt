package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.json.JSONException
import ru.luckycactus.steamroulette.domain.games.entity.RequiredAge
import javax.inject.Inject

/**
 * Json field is string ("0", "6", etc) when there is info about required age and int 0 otherwise
 */
class RequiredAgeMoshiAdapter @Inject constructor() {
    @FromJson
    fun fromJson(jsonReader: JsonReader): RequiredAge? {
        return when (jsonReader.peek()) {
            JsonReader.Token.STRING -> {
                RequiredAge(jsonReader.nextString().toInt())
            }
            JsonReader.Token.NUMBER -> {
                jsonReader.nextInt()
                null
            }
            else -> throw JSONException("Cannot parse requiredAge")
        }
    }

    @ToJson
    fun toJson(requiredAge: RequiredAge): String {
        throw UnsupportedOperationException()
    }
}