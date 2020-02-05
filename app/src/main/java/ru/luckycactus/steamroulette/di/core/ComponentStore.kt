package ru.luckycactus.steamroulette.di.core

class ComponentStore {

    private val components = mutableMapOf<String, Any>()

    fun isExist(key: String) = components.containsKey(key)

    fun add(key: String, component: Any) {
        components[key] = component
    }

    fun get(key: String) =
        components[key] ?: ComponentNotFoundException(
            "Component for key == $key was not found"
        )

    fun remove(key: String) {
        components.remove(key)
    }

    fun findComponent(predicate: (Any) -> Boolean): Any {
        for ((_, component) in components) {
            if (predicate(component)) return component
        }
        throw ComponentNotFoundException("Component satisfying the predicate was not found")
    }
}