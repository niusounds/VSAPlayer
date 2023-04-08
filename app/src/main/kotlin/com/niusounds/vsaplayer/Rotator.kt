package com.niusounds.vsaplayer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.min

@Composable
fun Rotator(
    angle: Float,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    lineWidth: Float = 4f,
    lineCircleSize: Float = 8f,
) {
    Box(modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            translate(left = size.width * 0.5f, top = size.height * 0.5f) {

                val lineLength = min(size.width * 0.35f, size.height * 0.35f)
                rotate(
                    degrees = angle,
                    pivot = Offset.Zero
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset.Zero,
                        end = Offset(0f, -lineLength),
                        strokeWidth = lineWidth,
                    )
                    drawCircle(
                        color = lineColor,
                        radius = lineCircleSize,
                        center = Offset(0f, -lineLength)
                    )
                }
            }
        }

        Text(
            text = "Front",
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Text(
            text = "Right",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer(rotationZ = 90f)
        )

        Text(
            text = "Bottom",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer(rotationZ = 180f)
        )

        Text(
            text = "Left",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer(rotationZ = 270f)
        )
    }
}

@Preview
@Composable
fun PreviewRotator() {
    Rotator(angle = 0f)
}

@Preview
@Composable
fun PreviewRotator45() {
    Rotator(angle = 10f)
}