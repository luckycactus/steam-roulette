package ru.luckycactus.steamroulette.data.repositories.library

import android.content.SharedPreferences
import androidx.core.content.edit
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.library.LibrarySettingsRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.presentation.utils.AppUtils.prefKey
import javax.inject.Inject
import javax.inject.Named

class LibrarySettingsRepositoryImpl @Inject constructor(
    @Named("library-settings") private val prefs: SharedPreferences,
    private val userSession: UserSession
) : LibrarySettingsRepository {

    override fun getScale(default: Int) =
        prefs.getInt(spanCountPrefKey(userSession.requireCurrentUser()), default)

    override fun saveScale(scale: Int) {
        prefs.edit {
            putInt(spanCountPrefKey(userSession.requireCurrentUser()), scale)
        }
    }

    private fun spanCountPrefKey(steamId: SteamId) =
        prefKey("span-count", steamId)
}