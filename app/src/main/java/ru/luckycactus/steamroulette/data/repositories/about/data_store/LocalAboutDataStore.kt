package ru.luckycactus.steamroulette.data.repositories.about.data_store

import android.content.res.AssetManager
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import java.io.BufferedInputStream
import java.lang.reflect.Type
import javax.inject.Inject

class LocalAboutDataStore @Inject constructor(
    private val moshi: Moshi,
    private val assets: AssetManager
) : AboutDataStore {
    override suspend fun getAppLibraries(): List<AppLibrary> {
        val type = Types.newParameterizedType(List::class.java, AppLibrary::class.java)
        return withContext(Dispatchers.IO) {
            val json = assets.open("open_source_libraries.json").bufferedReader().use {
                it.readText()
            }
            moshi.adapter<List<AppLibrary>>(type).fromJson(json)!!
        }
    }
}