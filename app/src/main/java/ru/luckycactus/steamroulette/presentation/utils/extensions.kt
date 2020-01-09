package ru.luckycactus.steamroulette.presentation.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.exception.NetworkConnectionException
import ru.luckycactus.steamroulette.domain.exception.ServerException
import ru.luckycactus.steamroulette.domain.common.Event
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


fun View.visibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

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

fun <T> LifecycleOwner.observeNonNull(liveData: LiveData<T?>, body: (T) -> Unit) {
    liveData.observe(this, Observer { it?.let { body(it) } })
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, body: (T) -> Unit) {
    liveData.observe(this, Observer { event -> event.ifNotHandled { body(it) } })
}

fun Boolean?.nullIfFalse() = if (this == false) null else this

fun ViewModel.getCommonErrorDescription(resourceManager: ResourceManager, e: Exception): String {
    return resourceManager.getString(
        when (e) {
            is ServerException -> R.string.error_steam_api_unavailable
            is NetworkConnectionException -> R.string.error_check_your_connection
            else -> R.string.error_unknown
        }
    )
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
            val newLiveData = x?.let { transform(x) } ?: MutableLiveData(null)
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
        x?.let { it -> transform(it) } ?: MutableLiveData<Y>(nullValue)
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

fun Context.getColorFromRes(@ColorRes color: Int): Int {
    onApiAtLeast(Build.VERSION_CODES.M) {
        return resources.getColor(color, theme)
    }
    return resources.getColor(color)
}

fun View.getColorFromRes(@ColorRes color: Int) = context.getColorFromRes(color)


inline fun View.updatePadding(
    @Px left: Int = paddingLeft,
    @Px top: Int = paddingTop,
    @Px right: Int = paddingRight,
    @Px bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}

inline fun View.setPadding(@Px size: Int) {
    setPadding(size, size, size, size)
}

fun View.expandTouchArea(desiredWidth: Int, desiredHeight: Int) {
    val parent = parent as View
    parent.post {
        val rect = Rect()
        getHitRect(rect)
        if (desiredWidth >= width) {
            val extraPadding = (desiredWidth - width) / 2
            rect.left -= extraPadding
            rect.right += extraPadding
        }
        if (desiredHeight >= height) {
            val extraPadding = (desiredHeight - height) / 2
            rect.bottom += extraPadding
            rect.top -= extraPadding
        }
        parent.touchDelegate = TouchDelegate(rect, this)
    }
}

fun ViewGroup.inflate(@LayoutRes resId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(resId, this, attachToRoot)
}

inline fun <reified T> argument(
    key: String,
    defValue: String? = null
): ReadOnlyProperty<Fragment, T> = object : ReadOnlyProperty<Fragment, T> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val result = thisRef.arguments?.get(key) ?: defValue
        if (result != null && result !is T) {
            throw ClassCastException("Property $key has different class type")
        }
        return result as T
    }
}

inline fun <reified T> Fragment.getCallbacksOrThrow(): T {
    return getCallbacks() ?: throw ClassCastException("${T::class.java} not implemented")
}

inline fun <reified T> Fragment.getCallbacks(): T? {
    return when {
        parentFragment is T -> parentFragment as T
        activity is T -> activity as T
        else -> null
    }
}

fun Context.getThemeColorOrThrow(@AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    if (theme.resolveAttribute(resId, typedValue, true)) {
        return typedValue.data
    } else {
        throw IllegalArgumentException("Attribute not set on theme")
    }
}

fun Activity.hideKeyboard() {
    currentFocus?.apply {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    view?.showSnackbar(message, duration)
}

fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    initializer: (Snackbar.() -> Unit)? = null
) {
    with(Snackbar.make(this, message, duration)) {
        initializer?.invoke(this)
        show()
    }
}

@UseExperimental(InternalCoroutinesApi::class)
fun <R> Flow<R>.chunkBuffer(bufferSize: Int) =
    object : Flow<List<R>> {

        private var buffer = AtomicReference<ArrayList<R>>()

        init {
            createBuffer()
        }

        override suspend fun collect(collector: FlowCollector<List<R>>) {
            this@chunkBuffer.collect {
                buffer.get().run {
                    add(it)
                    if (size == bufferSize) {
                        collector.emit(this)
                        createBuffer()
                    }
                }
            }

            if (buffer.get().isNotEmpty()) {
                collector.emit(buffer.get()!!)
            }
        }

        private fun createBuffer() {
            buffer.set(ArrayList(bufferSize))
        }
    }


val Fragment.isFinishing: Boolean
    get() {
        if (requireActivity().isFinishing) {
            return true
        }

        if (isStateSaved) {
            return false
        }

        if (isRemoving) {
            return true
        }

        var anyParentRemoving = false
        var parent = parentFragment
        while (parent != null && !anyParentRemoving) {
            anyParentRemoving = parent.isRemoving
            parent = parent.parentFragment
        }

        if (anyParentRemoving) {
            return true
        }

        return false
    }

inline fun <reified T : ViewModel> ComponentActivity.viewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            provider() as T
    }
}

inline fun <reified T : ViewModel> Fragment.viewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            provider() as T
    }
}

inline fun <reified T : ViewModel> Fragment.activityViewModel(
    crossinline provider: () -> T
) = activityViewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            provider() as T
    }
}

fun FragmentManager.showIfNotExist(tag: String, createDialogFragment: () -> DialogFragment) {
    if (findFragmentByTag(tag) == null) {
        createDialogFragment().show(this, tag)
    }
}