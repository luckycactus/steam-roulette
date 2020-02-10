package ru.luckycactus.steamroulette.presentation.utils.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.internal.FastBlur
import jp.wasabeef.glide.transformations.internal.RSBlur
import jp.wasabeef.glide.transformations.internal.SupportRSBlur
import java.security.MessageDigest

class CoverBlurTransformation(
    private val radius: Int,
    private val blurBackgroundWidth: Int,
    private val bias: Float
) : BitmapTransformation() {

    init {
        check(!(bias < 0f || bias > 1f)) { "bias should be in 0..1 range" }
        check(!(radius <= 0 || radius > 25)) { "radius should be in (0,25] range" }
    }

    override fun transform(
        context: Context,
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        if (toTransform.width < toTransform.height)
            return toTransform

        val bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val sampling = outWidth / blurBackgroundWidth.toFloat()
        val blurBackgroundHeight = (outHeight / sampling).toInt()
        var blurBitmap =
            pool.get(blurBackgroundWidth, blurBackgroundHeight, Bitmap.Config.ARGB_8888)
                .apply {
                    density = toTransform.density
                }

        with(Canvas(blurBitmap)) {
            density = toTransform.density //todo ??
            scale(outWidth / toTransform.width.toFloat(), outHeight / toTransform.height.toFloat())
            scale(1f / sampling, 1f / sampling)
            drawBitmap(toTransform, 0f, 0f, paint)
        }
        blurBitmap = try {
            FastBlur.blur(blurBitmap, radius, true)
        } catch (e: RuntimeException) {
            SupportRSBlur.blur(context, blurBitmap, radius)
        } catch (e: NoClassDefFoundError) {
            RSBlur.blur(context, blurBitmap, radius)
        }
        canvas.save()
        canvas.scale(
            outWidth.toFloat() / blurBackgroundWidth,
            outHeight.toFloat() / blurBackgroundHeight
        )
        canvas.drawBitmap(blurBitmap, 0f, 0f, paint)
        canvas.restore()

        pool.put(blurBitmap)

        val scale = outWidth / toTransform.width.toFloat()
        val toTransformScaledHeight = toTransform.height * scale
        var y = bias * outHeight - toTransformScaledHeight / 2
        y = y.coerceIn(0f, outHeight - toTransformScaledHeight / 2f)
        canvas.translate(0f, y)
        canvas.scale(scale, scale)
        canvas.drawBitmap(toTransform, 0f, 0f, paint)

        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("${ID}_${radius}_${blurBackgroundWidth}_${bias.toRawBits()}").toByteArray(Key.CHARSET))
    }

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
        private const val VERSION = 5
        private const val ID = "ru.luckycactus.steamroulette.CoverBlurTransformation.$VERSION"
    }
}