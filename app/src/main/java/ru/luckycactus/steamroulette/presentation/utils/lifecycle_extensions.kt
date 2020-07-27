package ru.luckycactus.steamroulette.presentation.utils

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import ru.luckycactus.steamroulette.domain.core.Event

fun <T> MutableLiveData<T>.startWith(item: T): MutableLiveData<T> {
    value = item
    return this
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, body: (T) -> Unit) {
    liveData.observe(this, Observer { body(it) })
}

fun <T> LifecycleOwner.observeFirst(liveData: LiveData<T>, body: (T) -> Unit) {
    val observer = object : Observer<T> {
        override fun onChanged(t: T) {
            body(t)
            liveData.removeObserver(this)
        }

    }
    liveData.observe(this, observer)
}

fun <T> LifecycleOwner.observeNotNull(liveData: LiveData<T>, body: (T) -> Unit) {
    liveData.observe(this, Observer { it?.let { body(it) } })
}

fun <T> LiveData<T?>.filterNotNull(): LiveData<T> {
    return MediatorLiveData<T>().apply {
        addSource(this@filterNotNull) { it?.let { value = it } }
    }
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, body: (T) -> Unit) {
    liveData.observe(this, Observer { event -> event.ifNotHandled { body(it) } })
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