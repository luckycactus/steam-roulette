package ru.luckycactus.steamroulette.di.core

/**
 * Implement this interface by Activity or Fragment and InjectionManager will create and destroy
 * a component automatically at right lifecycle time
 */
interface ComponentOwner<T: Any> {

    fun createComponent(): T

    val componentKey: String
        get() = javaClass.toString()

    /**
     * DANGEROUS. Set this to true if you want to keep alive a component on config changes
     * such as screen rotation
     */
    val retainComponentOnConfigChanges: Boolean
        get() = false
}