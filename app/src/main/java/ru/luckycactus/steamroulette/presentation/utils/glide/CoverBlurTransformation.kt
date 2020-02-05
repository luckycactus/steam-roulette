package ru.luckycactus.steamroulette.presentation.utils.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.internal.FastBlur
import java.security.MessageDigest


class CoverBlurTransformation(
    private val radius: Int,
    private val blurScaledWidth: Int,
    private val bias: Float
) : BitmapTransformation() {


    init {
        check(!(bias < 0f || bias > 1f)) { "bias should be in 0..1 range" }
    }

    override fun transform(
        context: Context,
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        if (toTransform.width < toTransform.height) //todo
            return toTransform
        val bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val sampling = outWidth / blurScaledWidth.toFloat()
        val scaledWidth = blurScaledWidth
        val scaledHeight = (outHeight / sampling).toInt()
        var blurBitmap = pool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888).apply {
            density = toTransform.density
        }

        with(Canvas(blurBitmap)) {
            density = toTransform.density //todo ??
            scale(1f, outHeight / toTransform.height.toFloat())
            scale(1f / sampling, 1f / sampling)
            drawBitmap(toTransform, 0f, 0f, paint)
        }
        blurBitmap = FastBlur.blur(blurBitmap, radius, true)
        canvas.save()
        canvas.scale(outWidth.toFloat() / scaledWidth, outHeight.toFloat() / scaledHeight)
        canvas.drawBitmap(blurBitmap, 0f, 0f, paint)
        canvas.restore()

        pool.put(blurBitmap)

        var y = bias * outHeight - toTransform.height / 2
        y = y.coerceIn(0f, outHeight - toTransform.height / 2f)
        canvas.drawBitmap(toTransform, 0f, y, paint)

        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius + blurScaledWidth).toByteArray(Key.CHARSET))
    }

    override fun equals(other: Any?): Boolean {
        return other is CoverBlurTransformation
                && radius == other.radius
                && blurScaledWidth == other.blurScaledWidth
                && bias.toRawBits() == other.bias.toRawBits()
    }

    override fun hashCode(): Int {
        var result = radius
        result = 31 * result + blurScaledWidth
        result = 31 * result + bias.hashCode()
        return result
    }
    
    companion object {
        private const val VERSION = 2
        private const val ID = "ru.luckycactus.steamroulette.CoverBlurTransformation.$VERSION"
    }
}