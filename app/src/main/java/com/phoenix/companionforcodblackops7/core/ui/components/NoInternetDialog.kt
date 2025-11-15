package com.phoenix.companionforcodblackops7.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
                text = "NO INTERNET CONNECTION",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(vertical = 8.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )

                Text(
                    text = "This app requires an active internet connection to continue.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Please check your WiFi or mobile data connection and try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "RETRY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = true,
                ).copy(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.error,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    )
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "EXIT APP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    )
}
