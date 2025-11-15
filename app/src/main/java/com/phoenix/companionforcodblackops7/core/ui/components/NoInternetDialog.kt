package com.phoenix.companionforcodblackops7.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NoInternetDialog(
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "errorPulse")

    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )

    AlertDialog(
        onDismissRequest = { /* Don't allow dismiss */ },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "No Internet",
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(alpha = iconPulse),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "No Internet Connection",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "This app requires an active internet connection to continue. Please check your WiFi or mobile data connection and try again.",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text("Exit App")
            }
        }
    )
}
