package com.phoenix.companionforcodblackops7.feature.lethals.presentation

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
import com.phoenix.companionforcodblackops7.core.ads.BannerAd
import com.phoenix.companionforcodblackops7.feature.lethals.domain.model.Lethal

/**
 * Detail screen showing comprehensive information about a specific lethal
 * including base stats and optional overclock upgrades
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LethalDetailScreen(
    lethal: Lethal,
    onNavigateBack: () -> Unit
) {
    val accentColor = lethal.getAccentColor()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lethal.displayName.uppercase(),
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
                HeroSection(lethal = lethal, accentColor = accentColor)

                // Base description section
                BaseSection(lethal = lethal, accentColor = accentColor)

                // Overclock 1 section (if exists)
                if (!lethal.overclock1.isNullOrEmpty()) {
                    OverclockSection(
                        title = "OVERCLOCK I",
                        description = lethal.overclock1!!,
                        iconUrl = lethal.iconO1Url,
                        accentColor = accentColor,
                        level = 1
                    )
                }

                // Overclock 2 section (if exists)
                if (!lethal.overclock2.isNullOrEmpty()) {
                    OverclockSection(
                        title = "OVERCLOCK II",
                        description = lethal.overclock2!!,
                        iconUrl = lethal.iconO2Url,
                        accentColor = accentColor,
                        level = 2
                    )
                }
            }

            // Banner Ad at Bottom
            BannerAd(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Hero section with large icons
 */
@Composable
private fun HeroSection(
    lethal: Lethal,
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
                model = "http://codbo7.masoombadi.top${lethal.iconUrl}",
                contentDescription = lethal.displayName,
                modifier = Modifier.size(180.dp)
            )
        }

        // HUD icon (if available)
        if (lethal.hasHudIcon()) {
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
                            model = "http://codbo7.masoombadi.top${lethal.iconHudUrl}",
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
                    value = if (lethal.isDefault()) "DEFAULT" else "LVL ${lethal.unlockLevel}",
                    color = if (lethal.isDefault()) Color(0xFF43A047) else accentColor,
                    modifier = Modifier.weight(1f)
                )

                // Overclocks count
                StatCard(
                    label = "OVERCLOCKS",
                    value = lethal.getOverclockCount().toString(),
                    color = if (lethal.hasOverclocks()) Color(0xFFFF9800) else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }

            // Second row: Availability (full width)
            val availabilityColor = when {
                lethal.availableMultiplayer && lethal.availableZombies -> Color(0xFF9C27B0) // Purple for both
                lethal.availableMultiplayer -> Color(0xFF2196F3) // Blue for MP
                lethal.availableZombies -> Color(0xFF4CAF50) // Green for Zombies
                else -> Color.Gray
            }
            StatCard(
                label = "AVAILABLE IN",
                value = lethal.getAvailabilityModes(),
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
    lethal: Lethal,
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
                    text = lethal.description,
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
