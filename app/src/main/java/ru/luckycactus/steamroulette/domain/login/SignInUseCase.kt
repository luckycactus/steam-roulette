package ru.luckycactus.steamroulette.domain.login

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.exception.InvalidSteamIdFormatException
import ru.luckycactus.steamroulette.domain.user.GetUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.SteamId.Companion.tryParse
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import javax.inject.Inject

@Reusable
class SignInUseCase @Inject constructor(
    private val getUserSummaryUseCase: GetUserSummaryUseCase,
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository
) : SuspendUseCase<String, UserSummary>() {

    override suspend fun getResult(params: String): UserSummary {
        val steamId =
            tryParse(params) ?: tryResolveVanity(params) ?: throw InvalidSteamIdFormatException()
        return getUserSummaryUseCase(GetUserSummaryUseCase.Params(steamId, true)).also {
            userRepository.setCurrentUser(it.steamId)
        }
    }

    private fun tryParse(input: String): SteamId? {
        val format = SteamId.getFormat(input)

        return if (format != SteamId.Format.Invalid)
            SteamId.parse(input, format)
        else null
    }

    private suspend fun tryResolveVanity(input: String): SteamId? {
        return SteamId.tryGetVanityUrl(input)?.let {
            loginRepository.resolveVanityUrl(it)
        }
    }
}