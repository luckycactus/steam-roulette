package ru.luckycactus.steamroulette.di.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ru.luckycactus.steamroulette.presentation.utils.isFinishing

object InjectionManager {

    private val componentStore = ComponentStore()

    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(activityLifecycleHelper)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> bindComponent(owner: ComponentOwner<T>): T =
        getComponentOrCreate(owner) as T

    inline fun <reified T> findComponent(): T = findComponent { it is T } as T

    fun findComponent(predicate: (Any) -> Boolean): Any =
        componentStore.findComponent(predicate)

    fun removeComponent(owner: ComponentOwner<*>) {
        componentStore.remove(owner.componentKey)
    }

    private fun <T : Any> getComponentOrCreate(owner: ComponentOwner<T>): Any {
        val key = owner.componentKey
        return if (componentStore.isExist(key))
            componentStore.get(key)
        else owner.createComponent().also { componentStore.add(key, it) }
    }

    private val activityLifecycleHelper = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity?) {}

        override fun onActivityResumed(activity: Activity?) {}

        override fun onActivityStarted(activity: Activity?) {}

        override fun onActivityDestroyed(activity: Activity?) {
            if (activity is ComponentOwner<*> &&
                (!activity.retainComponentOnConfigChanges || activity.isFinishing)
            ) {
                removeComponent(activity)
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

        override fun onActivityStopped(activity: Activity?) {}

        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            if (activity is AutoInjectable) {
                activity.inject()
            }
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentLifecycleHelper,
                    true
                )
            }
        }
    }

    private val fragmentLifecycleHelper = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
            if (f is AutoInjectable) {
                f.inject()
            }
        }

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
            if (f !is ComponentOwner<*>)
                return

            if (!f.retainComponentOnConfigChanges || f.isFinishing) {
                removeComponent(f)
                return
            }
        }
    }
}