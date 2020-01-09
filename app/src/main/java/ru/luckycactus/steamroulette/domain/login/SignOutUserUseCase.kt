package ru.luckycactus.steamroulette.domain.login

import dagger.Reusable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.common.GameCoverCacheCleaner
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import javax.inject.Inject

@Reusable
class SignOutUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val gamesRepository: GamesRepository,
    private val settingsRepository: UserSettingsRepository,
    private val gameCoverCacheCleaner: GameCoverCacheCleaner
) : SuspendUseCase<Unit, Unit>() {

    override suspend fun getResult(params: Unit) {
        userRepository.getCurrentUserSteamId()?.let {
            coroutineScope {
                launch { gamesRepository.clearUser(it) }
                launch { settingsRepository.clearUser(it) }
                launch { userRepository.clearUserSummary(it) }
                launch { gameCoverCacheCleaner.clearAllCache() }
            }
            userRepository.signOut()
        }
    }
}