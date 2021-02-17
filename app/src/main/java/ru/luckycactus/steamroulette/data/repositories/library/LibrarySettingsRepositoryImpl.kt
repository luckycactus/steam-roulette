package ru.luckycactus.steamroulette.data.repositories.library

import android.content.SharedPreferences
import ru.luckycactus.steamroulette.data.core.intMultiPref
import ru.luckycactus.steamroulette.domain.library.LibrarySettingsRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Named

class LibrarySettingsRepositoryImpl @Inject constructor(
    @Named("library-settings") private val prefs: SharedPreferences,
    private val userSession: UserSession
) : LibrarySettingsRepository {

    private val spanCountMultiPref by prefs.intMultiPref("span-count")

    override fun getScale(default: Int) =
        spanCountMultiPref[userSession.requireCurrentUser().as64(), default]

    override fun saveScale(scale: Int) {
        spanCountMultiPref[userSession.requireCurrentUser().as64()] = scale
    }
}