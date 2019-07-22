package ru.luckycactus.steamroulette.domain

import ru.luckycactus.steamroulette.data.user.UserRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.exception.InvalidSteamIdFormatException
import ru.luckycactus.steamroulette.domain.user.SteamId
import ru.luckycactus.steamroulette.domain.user.UserSummary

class SignInUseCase(
    private val resolveVanityUrlUseCase: ResolveVanityUrlUseCase, //todo remove?
    private val getUserSummaryUseCase: GetUserSummaryUseCase,
    private val userRepository: UserRepository
) : SuspendUseCase<String, UserSummary>() {

    override suspend fun getResult(params: String): UserSummary {
        val steamId = tryParse(params) ?: tryResolveVanity(params) ?: throw InvalidSteamIdFormatException()
        return getUserSummaryUseCase(GetUserSummaryUseCase.Params(steamId, true)).also {
            userRepository.saveSignedInUser(it.steamId)
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
            resolveVanityUrlUseCase(it)
        }
    }
}