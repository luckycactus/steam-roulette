package ru.luckycactus.steamroulette.data.net

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import ru.luckycactus.steamroulette.domain.games.entity.RequiredAge
import javax.inject.Inject

/**
 * Json field is string ("0", "6", etc) when there is info about required age and int 0 otherwise
 */
class RequiredAgeTypeAdapterFactory @Inject constructor() : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!RequiredAge::class.java.isAssignableFrom(type.rawType)) return null

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter?, value: T) {
                throw UnsupportedOperationException()
            }

            override fun read(reader: JsonReader): T? {
                return when (reader.peek()) {
                    JsonToken.STRING -> {
                        RequiredAge(reader.nextString().toInt()) as T
                    }
                    JsonToken.NUMBER -> {
                        reader.nextInt()
                        null
                    }
                    else -> throw JsonParseException("Cannot parse requiredAge")
                }
            }
        }
    }
}