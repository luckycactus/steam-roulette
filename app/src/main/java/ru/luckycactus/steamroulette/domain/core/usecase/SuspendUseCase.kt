package ru.luckycactus.steamroulette.domain.core.usecase

import kotlinx.coroutines.CancellationException

abstract class SuspendUseCase<P, R> : AbstractSuspendUseCase<P, Result<R>>() {

    override suspend fun execute(params: P): Result<R> {
        return try {
            getResult(params).let { Result.Success(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    abstract suspend fun getResult(params: P): R
}