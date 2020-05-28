package ru.luckycactus.steamroulette.domain.core.usecase

abstract class UseCase<P, R> : AbstractUseCase<P, Result<R>>() {

    override fun execute(params: P): Result<R> {
        return try {
            getResult(params).let { Result.Success(it) }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    abstract fun getResult(params: P): R
}