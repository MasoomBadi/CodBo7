package com.phoenix.companionforcodblackops7.feature.tacticals.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.tacticals.domain.model.Tactical

/**
 * Detail screen showing comprehensive information about a specific tactical
 * including base stats and optional overclock upgrades
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TacticalDetailScreen(
    tactical: Tactical,
    onNavigateBack: () -> Unit
) {
    val accentColor = tactical.getAccentColor()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tactical.displayName.uppercase(),
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
                    titleContentColor = accentColor
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Hero section with main icon and optional HUD icon
                HeroSection(tactical = tactical, accentColor = accentColor)

                // Base description section
                BaseSection(tactical = tactical, accentColor = accentColor)

                // Overclock 1 section (if exists)
                if (!tactical.overclock1.isNullOrEmpty()) {
                    OverclockSection(
                        title = "OVERCLOCK I",
                        description = tactical.overclock1!!,
                        iconUrl = tactical.iconO1Url,
                        accentColor = accentColor,
                        level = 1
                    )
                }

                // Overclock 2 section (if exists)
                if (!tactical.overclock2.isNullOrEmpty()) {
                    OverclockSection(
                        title = "OVERCLOCK II",
                        description = tactical.overclock2!!,
                        iconUrl = tactical.iconO2Url,
                        accentColor = accentColor,
                        level = 2
                    )
                }
            }

            // Fixed Banner Ad Space at Bottom
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Banner Ad Space (320x90)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

/**
 * Hero section with large icons
 */
@Composable
private fun HeroSection(
    tactical: Tactical,
    accentColor: Color
) {
    // Animated border glow
    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Main icon box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.15f),
                            accentColor.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = borderAlpha),
                            accentColor.copy(alpha = borderAlpha * 0.5f),
                            accentColor.copy(alpha = borderAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "http://codbo7.masoombadi.top${tactical.iconUrl}",
                contentDescription = tactical.displayName,
                modifier = Modifier.size(180.dp)
            )
        }

        // HUD icon (if available)
        if (tactical.hasHudIcon()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // HUD icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF9800).copy(alpha = 0.25f),
                                        Color(0xFFFF9800).copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "http://codbo7.masoombadi.top${tactical.iconHudUrl}",
                            contentDescription = "HUD Icon",
                            modifier = Modifier.size(70.dp)
                        )
                    }

                    // HUD label
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "HUD ICON",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = "In-game display icon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Stats rows
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First row: Unlock and Overclocks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Unlock level
                StatCard(
                    label = "UNLOCK",
                    value = if (tactical.isDefault()) "DEFAULT" else "LVL ${tactical.unlockLevel}",
                    color = if (tactical.isDefault()) Color(0xFF43A047) else accentColor,
                    modifier = Modifier.weight(1f)
                )

                // Overclocks count
                StatCard(
                    label = "OVERCLOCKS",
                    value = tactical.getOverclockCount().toString(),
                    color = if (tactical.hasOverclocks()) Color(0xFFFF9800) else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }

            // Second row: Availability (full width)
            val availabilityColor = when {
                tactical.availableMultiplayer && tactical.availableZombies -> Color(0xFF9C27B0) // Purple for both
                tactical.availableMultiplayer -> Color(0xFF2196F3) // Blue for MP
                tactical.availableZombies -> Color(0xFF4CAF50) // Green for Zombies
                else -> Color.Gray
            }
            StatCard(
                label = "AVAILABLE IN",
                value = tactical.getAvailabilityModes(),
                color = availabilityColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Stat card for displaying key metrics
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Base description section
 */
@Composable
private fun BaseSection(
    tactical: Tactical,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = "BASE EFFECT",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = accentColor
                    )
                }

                Text(
                    text = tactical.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Overclock upgrade section
 */
@Composable
private fun OverclockSection(
    title: String,
    description: String,
    iconUrl: String?,
    accentColor: Color,
    level: Int
) {
    val overclockColor = when (level) {
        1 -> Color(0xFFFF9800) // Orange for O1
        2 -> Color(0xFFE91E63) // Pink for O2
        else -> accentColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with optional icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon (if available)
                if (!iconUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        overclockColor.copy(alpha = 0.25f),
                                        overclockColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "http://codbo7.masoombadi.top${iconUrl}",
                            contentDescription = title,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }

                // Title
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = overclockColor
                    )
                    Text(
                        text = "Enhanced Capability",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Description
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(overclockColor)
                    )
                    Text(
                        text = "UPGRADE EFFECT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
