package ru.luckycactus.steamroulette.domain.login

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.RouletteRepository
import ru.luckycactus.steamroulette.domain.games_filter.LibraryFilterRepository
import ru.luckycactus.steamroulette.domain.games_filter.RouletteFilterRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository,
    private val gamesRepository: GamesRepository,
    private val rouletteFiltersRepository: RouletteFilterRepository,
    private val libraryFiltersRepository: LibraryFilterRepository,
    private val rouletteRepository: RouletteRepository,
    private val imageCacheCleaner: ImageCacheCleaner
) {

    suspend operator fun invoke() {
        userSessionRepository.currentUser?.let {
            coroutineScope {
                launch { gamesRepository.clearUser(it) }
                launch { rouletteFiltersRepository.clearUser(it) }
                launch { libraryFiltersRepository.clearUser(it) }
                launch { userRepository.clearUser(it) }
                launch { imageCacheCleaner.clearAllCache() }
                launch { rouletteRepository.clearUser(it) }
            }
            userSessionRepository.logOut()
        }
    }
}