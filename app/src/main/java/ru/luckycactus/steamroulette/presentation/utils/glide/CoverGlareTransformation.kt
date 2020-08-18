package ru.luckycactus.steamroulette.presentation.utils.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
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
        return toTransform.applyCanvas {
            scale(
                toTransform.width / glare.width.toFloat(),
                toTransform.height / glare.height.toFloat()
            )
            drawBitmap(glare, 0f, 0f, paint)
        }
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
        return ID.hashCode()
    }

    companion object {
        private const val VERSION = 2
        private const val ID =
            "ru.luckycactus.steamroulette.CoverGlareTransformation.$VERSION"
    }
}