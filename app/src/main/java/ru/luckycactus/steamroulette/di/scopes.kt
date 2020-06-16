package ru.luckycactus.steamroulette.di

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FlowScope