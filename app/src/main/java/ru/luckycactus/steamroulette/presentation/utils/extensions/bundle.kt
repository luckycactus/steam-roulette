package ru.luckycactus.steamroulette.presentation.utils.extensions

import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import androidx.fragment.app.Fragment
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified V> argument(
    key: String? = null,
    default: V? = null
): ReadWriteProperty<Fragment, V> = BundleDelegate(key, default, Fragment::ensureArgs)

fun <T, V> argument(
    key: String? = null,
    default: V? = null,
    getBundle: T.() -> Bundle
): ReadWriteProperty<T, V> = BundleDelegate(key, default, getBundle)

class BundleDelegate<T, V>(
    private val key: String? = null,
    private val default: V? = null,
    private val getBundle: T.() -> Bundle
) : ReadWriteProperty<T, V> {

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        getBundle(thisRef)[key(property)] = value
    }

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val result = getBundle(thisRef)[key(property)]
        return when (result) {
            null -> default
            else -> result
        } as V
    }

    private fun key(property: KProperty<*>) = key ?: property.name
}

//from androidx.core.os.bundle
operator fun Bundle.set(key: String, value: Any?) {
    when (value) {
        null -> putString(key, null) // Any nullable type will suffice.

        // Scalars
        is Boolean -> putBoolean(key, value)
        is Byte -> putByte(key, value)
        is Char -> putChar(key, value)
        is Double -> putDouble(key, value)
        is Float -> putFloat(key, value)
        is Int -> putInt(key, value)
        is Long -> putLong(key, value)
        is Short -> putShort(key, value)

        // References
        is Bundle -> putBundle(key, value)
        is CharSequence -> putCharSequence(key, value)
        is Parcelable -> putParcelable(key, value)

        // Scalar arrays
        is BooleanArray -> putBooleanArray(key, value)
        is ByteArray -> putByteArray(key, value)
        is CharArray -> putCharArray(key, value)
        is DoubleArray -> putDoubleArray(key, value)
        is FloatArray -> putFloatArray(key, value)
        is IntArray -> putIntArray(key, value)
        is LongArray -> putLongArray(key, value)
        is ShortArray -> putShortArray(key, value)

        // Reference arrays
        is Array<*> -> {
            val componentType = value::class.java.componentType!!
            @Suppress("UNCHECKED_CAST") // Checked by reflection.
            when {
                Parcelable::class.java.isAssignableFrom(componentType) -> {
                    putParcelableArray(key, value as Array<Parcelable>)
                }
                String::class.java.isAssignableFrom(componentType) -> {
                    putStringArray(key, value as Array<String>)
                }
                CharSequence::class.java.isAssignableFrom(componentType) -> {
                    putCharSequenceArray(key, value as Array<CharSequence>)
                }
                Serializable::class.java.isAssignableFrom(componentType) -> {
                    putSerializable(key, value)
                }
                else -> {
                    val valueType = componentType.canonicalName
                    throw IllegalArgumentException(
                        "Illegal value array type $valueType for key \"$key\""
                    )
                }
            }
        }

        // Last resort. Also we must check this after Array<*> as all arrays are serializable.
        is Serializable -> putSerializable(key, value)

        else -> {
            if (Build.VERSION.SDK_INT >= 18 && value is IBinder) {
                putBinder(key, value)
            } else if (Build.VERSION.SDK_INT >= 21 && value is Size) {
                putSize(key, value)
            } else if (Build.VERSION.SDK_INT >= 21 && value is SizeF) {
                putSizeF(key, value)
            } else {
                val valueType = value.javaClass.canonicalName
                throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
            }
        }
    }
}
