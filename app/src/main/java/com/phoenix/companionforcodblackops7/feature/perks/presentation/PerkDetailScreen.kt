package com.phoenix.companionforcodblackops7.feature.perks.presentation

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.perks.domain.model.Perk

/**
 * Detail screen showing comprehensive information about a specific perk
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PerkDetailScreen(
    perk: Perk,
    onNavigateBack: () -> Unit
) {
    val categoryColor = perk.getCategoryColorValue()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = perk.displayName.uppercase(),
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
                    titleContentColor = categoryColor
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
                // Hero section with animated icon
                HeroSection(perk = perk, categoryColor = categoryColor)

                // Info cards
                InfoSection(perk = perk, categoryColor = categoryColor)

                // Description section
                DescriptionSection(perk = perk, categoryColor = categoryColor)
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
 * Hero section with large perk icon and animated border
 */
@Composable
private fun HeroSection(
    perk: Perk,
    categoryColor: Color
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = 0.15f),
                        categoryColor.copy(alpha = 0.05f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = borderAlpha),
                        categoryColor.copy(alpha = borderAlpha * 0.5f),
                        categoryColor.copy(alpha = borderAlpha)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = "http://codbo7.masoombadi.top${perk.iconUrl}",
            contentDescription = perk.displayName,
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

/**
 * Info section with perk details in cards
 */
@Composable
private fun InfoSection(
    perk: Perk,
    categoryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Slot info
        InfoCard(
            label = "SLOT",
            value = perk.slot.toString(),
            color = categoryColor,
            modifier = Modifier.weight(1f)
        )

        // Category info
        InfoCard(
            label = "CATEGORY",
            value = perk.category.uppercase(),
            color = categoryColor,
            modifier = Modifier.weight(1f)
        )

        // Unlock info
        InfoCard(
            label = "UNLOCK",
            value = if (perk.isDefault()) "DEFAULT" else "LVL ${perk.unlockLevel}",
            color = if (perk.isDefault()) Color(0xFF43A047) else categoryColor,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual info card
 */
@Composable
private fun InfoCard(
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                ),
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Description section with detailed perk effects
 */
@Composable
private fun DescriptionSection(
    perk: Perk,
    categoryColor: Color
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
                                categoryColor,
                                categoryColor.copy(alpha = 0.5f)
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
                            .background(categoryColor)
                    )
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = categoryColor
                    )
                }

                Text(
                    text = perk.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Combat Specialty Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        categoryColor.copy(alpha = 0.3f),
                                        categoryColor.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = perk.slot.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = categoryColor
                        )
                    }

                    Column {
                        Text(
                            text = "Combat Specialty",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = perk.category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = categoryColor
                        )
                    }
                }
            }
        }
    }
}
