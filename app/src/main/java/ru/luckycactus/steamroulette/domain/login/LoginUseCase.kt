package ru.luckycactus.steamroulette.domain.login

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import ru.luckycactus.steamroulette.domain.user.GetUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

@Reusable
class LoginUseCase @Inject constructor(
    private val getUserSummaryUseCase: GetUserSummaryUseCase,
    private val userSessionRepository: UserSessionRepository,
    private val loginRepository: LoginRepository
) : AbstractSuspendUseCase<String, LoginUseCase.Result>() {

    override suspend fun execute(params: String): Result {
        try {
            var steamId = tryParse(params)
            if (steamId == null) {
                steamId = SteamId.tryGetVanityUrl(params)?.let {
                    try {
                        loginRepository.resolveVanityUrl(it)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: VanityNotFoundException) {
                        return Result.Fail.VanityNotFound
                    } catch (e: Exception) {
                        return Result.Fail.Error(e)
                    }
                }
            }
            if (steamId == null)
                return Result.Fail.InvalidSteamIdFormat

            val userSummary =
                getUserSummaryUseCase(GetUserSummaryUseCase.Params(steamId, true)).let {
                    when (it) {
                        is GetUserSummaryUseCase.Result.Success -> it.userSummary
                        GetUserSummaryUseCase.Result.Fail.SteamIdNotFound -> return Result.Fail.SteamIdNotFound
                        is GetUserSummaryUseCase.Result.Fail.Error -> return Result.Fail.Error(it.exception)
                    }
                }

            userSessionRepository.setCurrentUser(userSummary.steamId)
            return Result.Success(userSummary)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.Fail.Error(e)
        }
    }

    private fun tryParse(input: String): SteamId? {
        val format = SteamId.getFormat(input)

        return if (format != SteamId.Format.Invalid)
            SteamId.parse(input, format)
        else null
    }

    sealed class Result {

        data class Success(
            val userSummary: UserSummary
        ) : Result()

        sealed class Fail : Result() {
            object InvalidSteamIdFormat : Fail()

            object VanityNotFound : Fail()

            object SteamIdNotFound : Fail()

            class Error(val exception: Exception) : Fail()
        }
    }
}