package ru.luckycactus.steamroulette.data.net.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.json.JSONException
import ru.luckycactus.steamroulette.data.net.RequiredAge
import javax.inject.Inject

/**
 * Json field is string ("0", "6", etc) when there is info about required age and int 0 otherwise
 */
class RequiredAgeMoshiAdapter @Inject constructor() {
    @FromJson
    @RequiredAge
    fun fromJson(jsonReader: JsonReader): Int? {
        return when (jsonReader.peek()) {
            JsonReader.Token.STRING -> {
                jsonReader.nextString().toIntOrNull()
            }
            JsonReader.Token.NUMBER -> {
                jsonReader.nextInt()
                null
            }
            JsonReader.Token.NULL -> {
                jsonReader.nextNull<Int>()
                null
            }
            else -> throw JSONException("Cannot parse requiredAge")
        }
    }

    @ToJson
    fun toJson(@RequiredAge requiredAge: Int?): String {
        throw UnsupportedOperationException()
    }
}