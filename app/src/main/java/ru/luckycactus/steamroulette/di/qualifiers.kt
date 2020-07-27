package ru.luckycactus.steamroulette.di

import androidx.annotation.IdRes
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AppCoScope


/**
 * Int-based [qualifier][Qualifier].
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Identified(
    @IdRes val id: Int
)

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class InterceptorSet

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkInterceptorSet