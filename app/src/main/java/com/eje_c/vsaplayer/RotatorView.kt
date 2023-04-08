package com.eje_c.vsaplayer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.niusounds.vsaplayer.Rotator

class RotatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    /**
     * タッチ操作を有効にするかどうか。
     */
    var touchControlEnabled: Boolean = true

    /**
     * 角度
     */
    var angle: Float by mutableStateOf(0.0f)

    /**
     * 角度が変更されたときの通知
     */
    var onAngleChanged: ((Float) -> Unit)? = null

    @Composable
    override fun Content() {
        Rotator(
            angle = angle,
            lineColor = colorResource(id = androidx.appcompat.R.color.accent_material_dark),
            lineWidth = context.resources.getDimension(R.dimen.lineWidth),
            lineCircleSize = context.resources.getDimension(R.dimen.lineCircleSize),
            labelFront = stringResource(id = R.string.front),
            labelRight = stringResource(id = R.string.right),
            labelBack = stringResource(id = R.string.back),
            labelLeft = stringResource(id = R.string.left),
        )
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
                    onAngleChanged?.invoke(angle)
                } else {
                    this.angle = newAngle
                    onAngleChanged?.invoke(angle)
                }

            }
        }

        return true
    }
}