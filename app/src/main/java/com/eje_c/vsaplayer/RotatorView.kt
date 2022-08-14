package com.eje_c.vsaplayer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation

class RotatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val linePaint = Paint().apply {
        strokeWidth = context.resources.getDimension(R.dimen.lineWidth)
        color = ContextCompat.getColor(context, androidx.appcompat.R.color.accent_material_dark)
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = context.resources.getDimension(R.dimen.textSize)
        textAlign = Paint.Align.CENTER
    }

    private val lineTextMargin = context.resources.getDimension(R.dimen.textMargin)
    private val lineCircleSize = context.resources.getDimension(R.dimen.lineCircleSize)
    private val front = context.getString(R.string.front)
    private val back = context.getString(R.string.back)
    private val right = context.getString(R.string.right)
    private val left = context.getString(R.string.left)

    /**
     * タッチ操作を有効にするかどうか。
     */
    var touchControlEnabled: Boolean = true

    /**
     * 角度
     */
    var angle: Float = 0.0f
        set(value) {
            field = value
            invalidate()
            onAngleChanged?.invoke(value)
        }

    /**
     * 角度が変更されたときの通知
     */
    var onAngleChanged: ((Float) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.withTranslation(x = width * 0.5f, y = height * 0.5f) {

            val lineLength = Math.min(width * 0.35f, height * 0.35f)
            canvas.withRotation(degrees = angle) {
                drawLine(0f, 0f, 0f, -lineLength, linePaint)
                drawCircle(0f, -lineLength, lineCircleSize, linePaint)
            }

            canvas.drawText(front, 0.0f, -(lineLength + lineTextMargin), textPaint)

            canvas.withRotation(degrees = 90f) {
                canvas.drawText(right, 0.0f, -(lineLength + lineTextMargin), textPaint)
            }

            canvas.withRotation(degrees = 180f) {
                canvas.drawText(back, 0.0f, -(lineLength + lineTextMargin), textPaint)
            }

            canvas.withRotation(degrees = 270f) {
                canvas.drawText(left, 0.0f, -(lineLength + lineTextMargin), textPaint)
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (!touchControlEnabled) return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {

                val x = event.x.toDouble() / width.toDouble() * 2.0 - 1.0
                val y = event.y.toDouble() / height.toDouble() * 2.0 - 1.0
                val newAngle = Math.toDegrees(Math.atan2(y, x)).toFloat() + 90

                if (newAngle < 0) {
                    this.angle = newAngle + 360
                } else {
                    this.angle = newAngle
                }

            }
        }

        return true
    }
}