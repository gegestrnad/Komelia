package snd.komelia.ui.reader.image.continuous

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Browser-style middle-click auto-scroll for the continuous reader.
 *
 * Middle-click toggles auto-scroll anchored at the press position. While active,
 * moving the mouse away from the anchor scrolls along the reading axis with a speed
 * proportional to the distance (12px deadzone, capped). Any other mouse press, or
 * another middle-click, stops it.
 *
 * The middle press is observed (and consumed) in the Initial pass so the reader's
 * tap-to-navigate gestures never see it.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.continuousAutoScrollInput(
    state: ContinuousReaderState,
): Modifier = this
    .pointerInput(state) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type != PointerEventType.Press) continue
                val position = event.changes.firstOrNull()?.position ?: continue
                if (event.button == PointerButton.Tertiary) {
                    event.changes.forEach { it.consume() }
                    state.toggleAutoScroll(position)
                } else if (state.autoScrollAnchor.value != null) {
                    state.stopAutoScroll()
                }
            }
        }
    }
    .pointerInput(state) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                if (event.type == PointerEventType.Move && state.autoScrollAnchor.value != null) {
                    event.changes.firstOrNull()?.let { state.updateAutoScrollPosition(it.position) }
                }
            }
        }
    }

@Composable
fun AutoScrollIndicator(anchor: Offset) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .size(40.dp)
                .offset {
                    IntOffset(
                        (anchor.x - 20.dp.toPx()).roundToInt(),
                        (anchor.y - 20.dp.toPx()).roundToInt()
                    )
                }
        ) {
            val primary = colors.primary
            val onPrimary = colors.onPrimary
            val radius = size.minDimension / 2f
            drawCircle(color = primary, radius = radius, alpha = 0.85f)
            drawCircle(color = onPrimary, radius = radius - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
            val arrowHalfWidth = radius * 0.32f
            val arrowHeight = radius * 0.28f
            val upBaseY = center.y - radius * 0.18f
            drawPath(
                Path().apply {
                    moveTo(center.x, upBaseY - arrowHeight)
                    lineTo(center.x - arrowHalfWidth, upBaseY)
                    lineTo(center.x + arrowHalfWidth, upBaseY)
                    close()
                },
                color = onPrimary
            )
            val downBaseY = center.y + radius * 0.18f
            drawPath(
                Path().apply {
                    moveTo(center.x, downBaseY + arrowHeight)
                    lineTo(center.x - arrowHalfWidth, downBaseY)
                    lineTo(center.x + arrowHalfWidth, downBaseY)
                    close()
                },
                color = onPrimary
            )
        }
    }
}
