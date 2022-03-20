package ru.luckycactus.steamroulette.presentation.utils.coil

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import coil.size.Size
import coil.transform.Transformation

class CoverGlareTransformation(
    private val glare: Bitmap
) : Transformation {

    private val paint = Paint()

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val output = if (input.isMutable) input else input.copy(Bitmap.Config.ARGB_8888, true)
        return output.applyCanvas {
            scale(
                input.width / glare.width.toFloat(),
                input.height / glare.height.toFloat()
            )
            drawBitmap(glare, 0f, 0f, paint)
        }
    }

    override val cacheKey: String
        get() = ID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    companion object {
        private const val VERSION = 3
        private const val ID =
            "ru.luckycactus.steamroulette.CoverGlareTransformation.$VERSION"
    }
}