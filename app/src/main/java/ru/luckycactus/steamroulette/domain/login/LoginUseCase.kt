package ru.luckycactus.steamroulette.domain.login

import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.common.SteamId.Format
import ru.luckycactus.steamroulette.domain.common.SteamId.VanityUrlFormat
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.user.GetUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val getUserSummaryUseCase: GetUserSummaryUseCase,
    private val userSessionRepository: UserSessionRepository,
    private val loginRepository: LoginRepository,
) : SuspendUseCase<String, LoginUseCase.Result>() {

    override suspend fun execute(params: String): Result {
        return Impl().execute(params)
    }

    private inner class Impl {
        var steamIdFormat: Format? = null
        var vanityUrlFormat: VanityUrlFormat? = null
        var vanityException: Exception? = null

        suspend fun execute(input: String): Result {
            val steamId = getSteamId(input) ?: return getSteamIdFail()
            return login(steamId)
        }

        suspend fun getSteamId(input: String): SteamId? {
            var steamId = tryParseSteamId(input)
            if (steamId == null) {
                steamId = tryGetSteamIdThroughVanity(input)
            }
            return steamId
        }

        fun tryParseSteamId(input: String): SteamId? {
            return SteamId.getFormat(input).let {
                steamIdFormat = it
                if (it != Format.Invalid) {
                    SteamId.parse(input, it)
                } else {
                    null
                }
            }
        }

        suspend fun tryGetSteamIdThroughVanity(input: String): SteamId? {
            SteamId.getVanityUrlFormat(input).let {
                vanityUrlFormat = it
                if (it == VanityUrlFormat.Invalid)
                    return null
                it.parseVanity(input).let {
                    try {
                        return loginRepository.resolveVanityUrl(it)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        vanityException = e
                    }
                    return null
                }
            }
        }

        fun getSteamIdFail(): Result.Fail {
            vanityException?.let {
                return when (it) {
                    is VanityNotFoundException -> Result.Fail.VanityNotFound(vanityUrlFormat!!)
                    else -> Result.Fail.Error(it, steamIdFormat, vanityUrlFormat)
                }
            }
            return Result.Fail.InvalidSteamIdFormat
        }

        private suspend fun login(steamId: SteamId) =
            when (
                val userResult =
                    getUserSummaryUseCase(GetUserSummaryUseCase.Params(steamId, true))
            ) {
                is GetUserSummaryUseCase.Result.Fail -> getUserFail(userResult)
                is GetUserSummaryUseCase.Result.Success -> {
                    setCurrentUser(userResult.userSummary)
                    getSuccess(userResult.userSummary)
                }
            }

        fun getUserFail(userFail: GetUserSummaryUseCase.Result.Fail): Result.Fail {
            return when (userFail) {
                GetUserSummaryUseCase.Result.Fail.SteamIdNotFound ->
                    Result.Fail.SteamIdNotFound(steamIdFormat!!)
                is GetUserSummaryUseCase.Result.Fail.Error ->
                    Result.Fail.Error(
                        userFail.exception,
                        steamIdFormat,
                        vanityUrlFormat
                    )
            }
        }

        fun setCurrentUser(userSummary: UserSummary) {
            userSessionRepository.setCurrentUser(userSummary.steamId)
        }

        private fun getSuccess(userSummary: UserSummary) =
            Result.Success(
                userSummary,
                steamIdFormat,
                vanityUrlFormat
            )
    }

    sealed class Result(
        val steamIdFormat: Format?,
        val vanityUrlFormat: VanityUrlFormat?
    ) {

        class Success(
            val userSummary: UserSummary,
            steamIdFormat: Format?,
            vanityUrlFormat: VanityUrlFormat?
        ) : Result(steamIdFormat, vanityUrlFormat)

        sealed class Fail(
            steamIdFormat: Format?,
            vanityUrlFormat: VanityUrlFormat?
        ) : Result(steamIdFormat, vanityUrlFormat) {
            object InvalidSteamIdFormat : Fail(null, null)

            class VanityNotFound(
                vanityUrlFormat: VanityUrlFormat
            ) : Fail(null, vanityUrlFormat)

            class SteamIdNotFound(
                steamIdFormat: Format
            ) : Fail(steamIdFormat, null)

            class Error(
                val exception: Exception,
                steamIdFormat: Format?,
                vanityUrlFormat: VanityUrlFormat?
            ) : Fail(steamIdFormat, vanityUrlFormat)
        }
    }
}