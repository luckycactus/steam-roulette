package ru.luckycactus.steamroulette.presentation.utils.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.common.App
import java.security.MessageDigest

class CoverGlareTransformation(
    private val glare: Bitmap
) : BitmapTransformation() {
    private val paint = Paint()

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {

        with(Canvas(toTransform)) {
            scale(outWidth / glare.width.toFloat(), outHeight / glare.height.toFloat())
            drawBitmap(glare, 0f, 0f, paint)
        }

        return toTransform
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID).toByteArray(Key.CHARSET))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    companion object {
        private const val VERSION = 1
        private const val ID =
            "ru.luckycactus.steamroulette.CoverGlareTransformation.$VERSION"
    }
}