package ru.luckycactus.steamroulette.presentation.ui.widget


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.utils.dp
import ru.luckycactus.steamroulette.presentation.utils.extensions.getThemeColorOrThrow

class LuxuryProgressBar : View {

    private var radius: Int = 0
    private var paint: Paint? = null
    private var arcCount: Int = 0
    private var arcAngle: Float = 0.toFloat()
    private var arcSpace: Float = 0.toFloat()
    private val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()
    private val accelerateInterpolator = AccelerateInterpolator()

    private val circleRect = RectF()
    private var lastUpdateTime: Long = 0
    private var currentSweepAngle: Float = 0.toFloat()
    private var currentScale: Float = 0.toFloat()
    private var currentProgressTime: Float = 0.toFloat()
    private var currentState = STATE_GROWTH
    private val stateDurations = IntArray(STATE_COUNT)

    //todo implement wrapcontent

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LuxuryProgressBar)
        val color = a.getColor(
            R.styleable.LuxuryProgressBar_progressColor,
            context.getThemeColorOrThrow(R.attr.colorAccent)
        )
        val strokeWidth = a.getDimensionPixelSize(R.styleable.LuxuryProgressBar_strokeWidth, dp(4f))
        radius =
            (a.getDimensionPixelSize(R.styleable.LuxuryProgressBar_size, dp(48f)) - strokeWidth) / 2
        arcCount = a.getInt(R.styleable.LuxuryProgressBar_arcCount, 4)
        stateDurations[STATE_GROWTH] = a.getInt(R.styleable.LuxuryProgressBar_growthDuration, 300)
        stateDurations[STATE_ROTATION] =
            a.getInt(R.styleable.LuxuryProgressBar_rotationDuration, 1000)
        stateDurations[STATE_DECREASING] =
            a.getInt(R.styleable.LuxuryProgressBar_decreasingDuration, 300)
        a.recycle()

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.color = color
        paint!!.strokeWidth = strokeWidth.toFloat()

        arcAngle = 360f / (arcCount * 2)
        arcSpace = (360f - arcCount * arcAngle) / arcCount

        resetState()
    }

    override fun onDraw(canvas: Canvas) {
        val x = measuredWidth / 2f
        val y = measuredHeight / 2f
        circleRect.set(
            x - radius,
            y - radius,
            x + radius,
            y + radius
        )
        var start = Math.max(0f, currentSweepAngle - arcAngle)
        val angle = Math.min(360f, currentSweepAngle) - start + 1
        canvas.scale(
            currentScale,
            currentScale,
            (measuredWidth / 2).toFloat(),
            (measuredHeight / 2).toFloat()
        )
        for (i in 0 until arcCount) {
            canvas.drawArc(circleRect, start - 90, angle, false, paint!!)
            start += arcAngle + arcSpace
        }
        updateAnimation()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (visibility == GONE || visibility == INVISIBLE) {
            resetState()
        }
        super.onVisibilityChanged(changedView, visibility)
    }

    private fun resetState() {
        lastUpdateTime = System.currentTimeMillis()
        currentState = STATE_GROWTH
        currentSweepAngle = 1f
        currentScale = 0f
        currentProgressTime = 0f
    }

    private fun updateAnimation() {
        val time = System.currentTimeMillis()
        var dt = time - lastUpdateTime
        if (dt > 17)
            dt = 17
        lastUpdateTime = time
        currentProgressTime += dt.toFloat()

        if (currentProgressTime > stateDurations[currentState])
            currentProgressTime = stateDurations[currentState].toFloat()

        when (currentState) {
            STATE_DECREASING -> currentScale = 1 - accelerateInterpolator.getInterpolation(
                currentProgressTime / stateDurations[currentState]
            )
            STATE_GROWTH -> currentScale = currentProgressTime / stateDurations[currentState]
            STATE_ROTATION -> {
                currentSweepAngle = accelerateDecelerateInterpolator.getInterpolation(
                    currentProgressTime / stateDurations[currentState]
                ) * (360 + arcAngle)
                if (currentSweepAngle >= 360 + arcAngle)
                    currentSweepAngle = 0f
            }
        }

        if (currentProgressTime == stateDurations[currentState].toFloat()) {
            currentProgressTime = 0f
            currentState = (currentState + 1) % STATE_COUNT
        }

        postInvalidateOnAnimation()
    }

    companion object {
        private const val STATE_GROWTH = 0
        private const val STATE_ROTATION = 1
        private const val STATE_DECREASING = 2

        private const val STATE_COUNT = 3
    }
}
