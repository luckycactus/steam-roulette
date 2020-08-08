package ru.luckycactus.steamroulette.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AppCoScope

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class InterceptorSet

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkInterceptorSet