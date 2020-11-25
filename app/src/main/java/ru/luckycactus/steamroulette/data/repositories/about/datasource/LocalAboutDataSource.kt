package ru.luckycactus.steamroulette.data.repositories.about.datasource

import android.content.res.AssetManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import javax.inject.Inject

class LocalAboutDataSource @Inject constructor(
    private val moshi: Moshi,
    private val assets: AssetManager
) : AboutDataSource {
    override suspend fun getAppLibraries(): List<AppLibrary> = withContext(Dispatchers.IO) {
        val type = Types.newParameterizedType(List::class.java, AppLibrary::class.java)

        val json = assets.open("open_source_libraries.json").bufferedReader().use {
            it.readText()
        }
        moshi.adapter<List<AppLibrary>>(type).fromJson(json)!!

    }

}