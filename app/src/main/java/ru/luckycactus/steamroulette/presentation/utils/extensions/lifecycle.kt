package ru.luckycactus.steamroulette.presentation.utils.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import ru.luckycactus.steamroulette.domain.core.Event

val Fragment.viewLifecycleScope: LifecycleCoroutineScope
    get() = viewLifecycleOwner.lifecycleScope

val Fragment.viewLifecycle: Lifecycle
    get() = viewLifecycleOwner.lifecycle

fun Fragment.getLifecycleOwner(useViewLifecycle: Boolean) =
    if (useViewLifecycle) viewLifecycleOwner else this

inline fun <T> Fragment.observe(
    liveData: LiveData<T>,
    useViewLifecycle: Boolean = true,
    crossinline onChanged: (T) -> Unit
) = liveData.observe(getLifecycleOwner(useViewLifecycle), onChanged)

inline fun <T> Fragment.observeEvent(
    liveData: LiveData<Event<T>>,
    useViewLifecycle: Boolean = true,
    crossinline onChanged: (T) -> Unit
): Observer<Event<T>> = liveData.observeEvent(getLifecycleOwner(useViewLifecycle), onChanged)

inline fun <T> AppCompatActivity.observe(
    liveData: LiveData<T>,
    crossinline body: (T) -> Unit
) = liveData.observe(this, body)

inline fun <T> AppCompatActivity.observeEvent(
    liveData: LiveData<Event<T>>,
    crossinline body: (T) -> Unit
): Observer<Event<T>> = liveData.observeEvent(this, body)

inline fun <T> LiveData<Event<T>>.observeEvent(
    owner: LifecycleOwner,
    crossinline onEvent: (T) -> Unit
): Observer<Event<T>> = observe(owner) { event ->
    event.ifNotHandled {
        onEvent.invoke(it)
    }
}

fun <T> LiveData<T>.first(block: (T) -> Unit) {
    value?.let {
        block(it)
        return
    }

    observeForever(object : Observer<T> {
        override fun onChanged(t: T) {
            block(t)
            removeObserver(this)
        }

    })
}

inline fun <X, Y> LiveData<X?>.nullableSwitchMap(
    crossinline transform: (X) -> LiveData<Y>
): LiveData<Y?> {
    val result = MediatorLiveData<Y?>()
    result.addSource(this, object : Observer<X?> {
        var mSource: LiveData<out Y?>? = null

        override fun onChanged(x: X?) {
            val newLiveData = x?.let { transform(x) } ?: MutableLiveData(
                null
            )
            if (mSource === newLiveData) {
                return
            }
            if (mSource != null) {
                result.removeSource(mSource!!)
            }
            mSource = newLiveData
            if (mSource != null) {
                result.addSource(mSource!!) { y -> result.value = y }
            }
        }
    })
    return result
}

inline fun <X, Y> LiveData<X?>.nullableSwitchMap(
    nullValue: Y,
    crossinline transform: (X) -> LiveData<Y>
): LiveData<Y> {
    return switchMap { x ->
        x?.let { it -> transform(it) } ?: MutableLiveData<Y>(
            nullValue
        )
    }
}

fun <T> LiveData<T?>.filterNotNull(): LiveData<T> {
    return MediatorLiveData<T>().apply {
        addSource(this@filterNotNull) { it?.let { value = it } }
    }
}

fun <A, B, Result> LiveData<A>.combine(
    other: LiveData<B>,
    combiner: (A, B) -> Result
): LiveData<Result> {
    val result = MediatorLiveData<Result>()
    result.addSource(this) { a ->
        val b = other.value
        if (b != null) {
            result.postValue(combiner(a, b))
        }
    }
    result.addSource(other) { b ->
        val a = this@combine.value
        if (a != null) {
            result.postValue(combiner(a, b))
        }
    }
    return result
}