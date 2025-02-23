package ru.kram.pagingsample.designsystem.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun PulseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    var tapConfirmed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 0),
        label = "button scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val released = try {
                            awaitRelease()
                            true
                        } catch (e: CancellationException) {
                            false
                        }
                        if (released) {
                            tapConfirmed = true
                            pressed = false
                        } else {
                            pressed = false
                        }
                    }
                )
            }
            .then(modifier)
    ) {
        content()
    }

    LaunchedEffect(scale, tapConfirmed) {
        if (tapConfirmed && scale > 0.95f) {
            onClick()
            tapConfirmed = false
        }
    }
}