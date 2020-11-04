package ru.luckycactus.steamroulette.domain.login

import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.common.SteamId
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
        try {
            val steamIdFormat = SteamId.getFormat(params)

            var steamId = if (steamIdFormat != SteamId.Format.Invalid) {
                SteamId.parse(params, steamIdFormat)
            } else null

            var vanityUrlFormat: SteamId.VanityUrlFormat? = null
            if (steamId == null) {
                vanityUrlFormat = SteamId.getVanityUrlFormat(params)
                if (vanityUrlFormat != SteamId.VanityUrlFormat.Invalid) {
                    steamId = vanityUrlFormat.parseVanity(params).let {
                        try {
                            loginRepository.resolveVanityUrl(it)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: VanityNotFoundException) {
                            return Result.Fail.VanityNotFound(vanityUrlFormat)
                        } catch (e: Exception) {
                            return Result.Fail.Error(e, steamIdFormat, vanityUrlFormat)
                        }
                    }
                }
            }
            if (steamId == null)
                return Result.Fail.InvalidSteamIdFormat

            val userSummary =
                getUserSummaryUseCase(GetUserSummaryUseCase.Params(steamId, true)).let {
                    when (it) {
                        is GetUserSummaryUseCase.Result.Success -> it.userSummary
                        GetUserSummaryUseCase.Result.Fail.SteamIdNotFound -> return Result.Fail.SteamIdNotFound(
                            steamIdFormat
                        )
                        is GetUserSummaryUseCase.Result.Fail.Error -> return Result.Fail.Error(
                            it.exception,
                            steamIdFormat,
                            vanityUrlFormat
                        )
                    }
                }

            userSessionRepository.setCurrentUser(userSummary.steamId)
            return Result.Success(userSummary, steamIdFormat, vanityUrlFormat)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.Fail.Error(e, null, null)
        }
    }

    sealed class Result(
        val steamIdFormat: SteamId.Format?,
        val vanityUrlFormat: SteamId.VanityUrlFormat?
    ) {

        class Success(
            val userSummary: UserSummary,
            steamIdFormat: SteamId.Format?,
            vanityUrlFormat: SteamId.VanityUrlFormat?
        ) : Result(steamIdFormat, vanityUrlFormat)

        sealed class Fail(
            steamIdFormat: SteamId.Format?,
            vanityUrlFormat: SteamId.VanityUrlFormat?
        ) : Result(steamIdFormat, vanityUrlFormat) {
            object InvalidSteamIdFormat : Fail(null, null)

            class VanityNotFound(
                vanityUrlFormat: SteamId.VanityUrlFormat
            ) : Fail(null, vanityUrlFormat)

            class SteamIdNotFound(
                steamIdFormat: SteamId.Format
            ) : Fail(steamIdFormat, null)

            class Error(
                val exception: Exception,
                steamIdFormat: SteamId.Format?,
                vanityUrlFormat: SteamId.VanityUrlFormat?
            ) : Fail(steamIdFormat, vanityUrlFormat)
        }
    }
}