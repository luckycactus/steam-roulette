package ru.luckycactus.steamroulette.data.net.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import javax.inject.Inject

class SystemRequirementsTypeAdapterFactory @Inject constructor() : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!SystemRequirements::class.java.isAssignableFrom(type.rawType)) return null

        val delegate = gson.getDelegateAdapter(this, type)
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter?, value: T) {
                throw UnsupportedOperationException()
            }

            override fun read(reader: JsonReader): T? {
                return when (reader.peek()) {
                    JsonToken.NULL -> {
                        reader.nextNull()
                        null
                    }
                    JsonToken.BEGIN_ARRAY -> {
                        reader.skipValue()
                        null
                    }
                    else -> delegate.read(reader)
                }
            }
        }
    }
}