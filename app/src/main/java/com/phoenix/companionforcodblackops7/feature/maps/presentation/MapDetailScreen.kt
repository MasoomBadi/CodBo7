package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapDetailScreen(
    map: GameMap,
    onNavigateBack: () -> Unit,
    onViewMap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = map.displayName.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hero Image Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = borderGlow),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = borderGlow)
                                )
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "http://codbo7.masoombadi.top${map.coverImageUrl}"
                            ),
                            contentDescription = "${map.displayName} cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
                                        ),
                                        startY = 300f
                                    )
                                )
                        )
                    }
                }

                // Map Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.tertiary,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "MAP INFORMATION",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        // Teams
                        if (map.teams.isNotBlank()) {
                            InfoRow(
                                label = "Teams",
                                value = map.teams
                            )
                        }

                        // Modes
                        if (map.modes.isNotBlank()) {
                            InfoRow(
                                label = "Game Modes",
                                value = map.modes
                            )
                        }

                        // Campaign Map
                        if (map.campaignMap.isNotBlank()) {
                            InfoRow(
                                label = "Campaign",
                                value = map.campaignMap
                            )
                        }

                        // Map Bounds
                        InfoRow(
                            label = "Map Size",
                            value = "${map.bounds.northeastX} x ${map.bounds.northeastY}px"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Button Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // View Map Button
                    Button(
                        onClick = onViewMap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = "VIEW INTERACTIVE MAP",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    // Ad Space
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ad Space (320x50)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
