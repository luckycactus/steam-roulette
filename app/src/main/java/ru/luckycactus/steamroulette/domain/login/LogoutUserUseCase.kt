package ru.luckycactus.steamroulette.domain.login

import dagger.Reusable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games_filter.GamesFilterRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository
import javax.inject.Inject
import javax.inject.Named

@Reusable
class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository,
    private val gamesRepository: GamesRepository,
    @Named("roulette") private val rouletteFiltersRepository: GamesFilterRepository,
    @Named("library") private val libraryFiltersRepository: GamesFilterRepository,
    private val imageCacheCleaner: ImageCacheCleaner
) : SuspendUseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit) {
        userSessionRepository.currentUser?.let {
            coroutineScope {
                launch { gamesRepository.clearUser(it) }
                launch { rouletteFiltersRepository.clearUser(it) }
                launch { libraryFiltersRepository.clearUser(it) }
                launch { userRepository.clearUser(it) }
                launch { imageCacheCleaner.clearAllCache() }
            }
            userSessionRepository.logOut()
        }
    }
}