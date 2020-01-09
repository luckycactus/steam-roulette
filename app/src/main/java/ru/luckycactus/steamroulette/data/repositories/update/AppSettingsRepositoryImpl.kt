package ru.luckycactus.steamroulette.data.repositories.update

import android.content.Context
import android.content.SharedPreferences
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.utils.int
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.update.AppSettingsRepository
import javax.inject.Inject

class AppSettingsRepositoryImpl @Inject constructor(
    @ForApplication private val context: Context,
    @Identified(R.id.appPrefs) private val appPrefs: SharedPreferences
) : AppSettingsRepository {

    override var lastVersion by appPrefs.int("last_version", -1)

    override val currentVersion: Int
        get() = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
}