package ru.luckycactus.steamroulette.presentation.utils.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import coil.size.Size
import coil.size.pxOrElse
import coil.transform.Transformation
import jp.wasabeef.glide.transformations.internal.FastBlur
import jp.wasabeef.glide.transformations.internal.RSBlur

class CoverBlurTransformation(
    private val context: Context,
    private val radius: Int,
    private val blurBackgroundWidth: Int,
    private val bias: Float
) : Transformation {

    private val paint = Paint()

    init {
        check(!(bias < 0f || bias > 1f)) { "bias should be in 0..1 range" }
        check(!(radius <= 0 || radius > 25)) { "radius should be in (0,25] range" }
    }

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (input.width < input.height)
            return input

        val dstWidth = size.width.pxOrElse { input.width }
        val dstHeight = size.height.pxOrElse { input.height }

        val aspectRatio = dstWidth.toFloat() / dstHeight
        val width = input.width.toFloat()
        val height = width / aspectRatio
        val sampling = width / blurBackgroundWidth
        val blurBackgroundHeight = (height / sampling).toInt()

        var blurBitmap = createBitmap(
            blurBackgroundWidth,
            blurBackgroundHeight,
            Bitmap.Config.ARGB_8888,
            hasAlpha = false
        ).apply {
            density = input.density
        }

        blurBitmap.applyCanvas {
            density = input.density
            scale(1f, height / input.height)
            scale(1f / sampling, 1f / sampling)
            drawBitmap(input, 0f, 0f, paint)
        }

        blurBitmap = blur(blurBitmap, context)

        val result = createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888, hasAlpha = false)
        result.applyCanvas {
            withScale(
                width / blurBackgroundWidth,
                height / blurBackgroundHeight
            ) {
                drawBitmap(blurBitmap, 0f, 0f, paint)
            }

            val y = (height - input.height) * bias
            translate(0f, y)
            drawBitmap(input, 0f, 0f, paint)
        }

        blurBitmap.recycle()

        return result
    }

    private fun blur(
        blurBitmap: Bitmap,
        context: Context
    ) = try {
        FastBlur.blur(blurBitmap, radius, true)
    } catch (e: RuntimeException) {
        RSBlur.blur(context, blurBitmap, radius)
    } /*catch (e: NoClassDefFoundError) {
        // todo
        //SupportRSBlur.blur(context, blurBitmap, radius)
    }*/

    override val cacheKey: String
        get() = "${ID}_${radius}_${blurBackgroundWidth}_${bias.toRawBits()}"

    override fun equals(other: Any?): Boolean {
        return other is CoverBlurTransformation
                && radius == other.radius
                && blurBackgroundWidth == other.blurBackgroundWidth
                && bias.toRawBits() == other.bias.toRawBits()
    }

    override fun hashCode(): Int {
        var result = ID.hashCode()
        result = 31 * result + radius
        result = 31 * result + blurBackgroundWidth
        result = 31 * result + bias.hashCode()
        return result
    }

    companion object {
        private const val VERSION = 7
        private const val ID = "ru.luckycactus.steamroulette.CoverBlurTransformation.$VERSION"
    }
}