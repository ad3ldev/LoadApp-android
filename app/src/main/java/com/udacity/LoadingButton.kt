package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var normalColor = 0
    private var loadingColor = 0
    private var circleColor = 0
    private var buttonString: String = ""


    private val valueAnimator = ValueAnimator.ofFloat(0f, 1000f)
    private var progress = 0f
    private var progressCircle = RectF()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                valueAnimator.apply {
                    addUpdateListener {
                        progress = animatedValue as Float
                        invalidate()
                    }
                    duration = 10000
                    doOnStart {
                        buttonString = resources.getString(R.string.button_loading)
                        isEnabled = false
                    }

                    doOnEnd {
                        progress = 0f
                        isEnabled = true
                        buttonString = resources.getString(R.string.button_name)
                    }
                    start()
                }
            }
            ButtonState.Clicked -> {
                buttonState = ButtonState.Loading
                isEnabled = false
            }
            ButtonState.Completed -> {
                isEnabled = true
                buttonString = resources.getString(R.string.button_name)
            }
        }
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 60.0f
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            normalColor = getColor(R.styleable.LoadingButton_normalColor, 0)
            loadingColor = getColor(R.styleable.LoadingButton_loadingColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)
        }
        buttonString = resources.getString(R.string.button_name)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = normalColor
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        if (buttonState == ButtonState.Loading) {
            paint.color = loadingColor
            val progressRect = progress / 1000f * widthSize
            canvas?.drawRect(0f, 0f, progressRect, heightSize.toFloat(), paint)
            val sweepAngle = progress / 1000f * 360f
            paint.color = circleColor
            canvas?.drawArc(progressCircle, 0f, sweepAngle, true, paint)
        }

        paint.color = Color.WHITE
        canvas?.drawText(
            buttonString,
            widthSize / 2f,
            heightSize.toFloat() - paint.textSize,
            paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
        progressCircle = RectF(
            w - 250f,
            h / 2 - 25f,
            w.toFloat() - 200f,
            h / 2 + 25f
        )
    }

    fun startDownload() {
        buttonState = ButtonState.Loading
    }

    fun completeDownload() {
        val fraction = valueAnimator.animatedFraction
        valueAnimator.cancel()
        valueAnimator.setCurrentFraction(fraction + 0.1f)
        valueAnimator.duration = 1000
        valueAnimator.start()
    }

}