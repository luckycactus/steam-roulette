package ru.luckycactus.steamroulette.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import ru.luckycactus.steamroulette.data.AndroidResourceManager
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.local.SharedPreferencesStorage
import ru.luckycactus.steamroulette.domain.common.ResourceManager

object AppModule {

    lateinit var appContext: Context

    lateinit var cacheHelper: CacheHelper
        private set

    lateinit var appPreferences: PreferencesStorage
        private set

    lateinit var resourceManager: ResourceManager
        private set

    val gson = Gson()

    fun init(app: Application) {
        appContext = app
        cacheHelper = CacheHelper(SharedPreferencesStorage(appContext, "cache-helper"))
        appPreferences = SharedPreferencesStorage(appContext, "app-prefs")
        resourceManager = AndroidResourceManager(appContext)
    }


}