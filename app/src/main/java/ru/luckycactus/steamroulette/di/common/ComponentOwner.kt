package ru.luckycactus.steamroulette.di.common

interface ComponentOwner<T: Any> {

    fun createComponent(): T

    val componentKey: String
        get() = javaClass.toString()

    val retainComponentOnConfigChanges: Boolean
        get() = false
}