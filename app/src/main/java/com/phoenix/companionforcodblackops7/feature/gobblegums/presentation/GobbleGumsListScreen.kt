package com.phoenix.companionforcodblackops7.feature.gobblegums.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGum

private const val BASE_URL = "http://codbo7.masoombadi.top"

/**
 * GobbleGums list screen with tag-based filtering
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GobbleGumsListScreen(
    onNavigateBack: () -> Unit,
    onGobbleGumClick: (GobbleGum) -> Unit,
    viewModel: GobbleGumsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GOBBLEGUMS",
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
                    titleContentColor = Color(0xFF9C27B0) // Purple
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is GobbleGumsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is GobbleGumsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is GobbleGumsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Filter section
                    FilterSection(
                        selectedRarity = state.selectedRarity,
                        selectedPattern = state.selectedPattern,
                        onRaritySelected = { rarity ->
                            if (state.selectedRarity == rarity) {
                                viewModel.filterByRarity(null)
                            } else {
                                viewModel.filterByRarity(rarity)
                            }
                        },
                        onPatternSelected = { pattern ->
                            if (state.selectedPattern == pattern) {
                                viewModel.filterByPattern(null)
                            } else {
                                viewModel.filterByPattern(pattern)
                            }
                        },
                        onClearFilters = { viewModel.clearFilters() }
                    )

                    // Scrollable content
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = state.filteredGobblegums,
                            key = { it.id }
                        ) { gobblegum ->
                            GobbleGumCard(
                                gobblegum = gobblegum,
                                onClick = { onGobbleGumClick(gobblegum) }
                            )
                        }

                        // Empty state
                        if (state.filteredGobblegums.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FilterList,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "No gobblegums found",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Try a different tag filter",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
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
    }
}

/**
 * Filter section with rarity and pattern filters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    selectedRarity: String?,
    selectedPattern: String?,
    onRaritySelected: (String) -> Unit,
    onPatternSelected: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    val rarities = listOf("Rare", "Epic", "Legendary", "Ultra", "Whimsical")
    val patterns = listOf("Instant", "Timed", "Conditional")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF9C27B0)
                    )
                    Text(
                        text = "FILTERS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (selectedRarity != null || selectedPattern != null) {
                    TextButton(onClick = onClearFilters) {
                        Text(
                            text = "CLEAR ALL",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Rarity filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "RARITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rarities.forEach { rarity ->
                        val isSelected = selectedRarity == rarity
                        FilterChip(
                            selected = isSelected,
                            onClick = { onRaritySelected(rarity) },
                            label = {
                                Text(
                                    text = rarity,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF9C27B0).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF9C27B0)
                            )
                        )
                    }
                }
            }

            // Pattern filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "PATTERN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    patterns.forEach { pattern ->
                        val isSelected = selectedPattern == pattern
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPatternSelected(pattern) },
                            label = {
                                Text(
                                    text = pattern,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF9C27B0).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF9C27B0)
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * GobbleGum card with rarity-based coloring
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GobbleGumCard(
    gobblegum: GobbleGum,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gobblegumGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val accentColor = gobblegum.getAccentColor()
    val borderBrush = if (gobblegum.isWhimsical()) {
        Brush.linearGradient(gobblegum.getGradientColors())
    } else {
        Brush.linearGradient(listOf(accentColor.copy(alpha = glowAlpha * 0.7f), accentColor.copy(alpha = glowAlpha * 0.7f)))
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with rarity glow
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow background
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (gobblegum.isWhimsical()) {
                                    gobblegum.getGradientColors().map { it.copy(alpha = 0.2f) }
                                } else {
                                    listOf(
                                        accentColor.copy(alpha = 0.3f * glowAlpha),
                                        accentColor.copy(alpha = 0.1f * glowAlpha),
                                        Color.Transparent
                                    )
                                }
                            )
                        )
                )

                // Icon
                AsyncImage(
                    model = "$BASE_URL${gobblegum.iconUrl}",
                    contentDescription = gobblegum.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(75.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name
                Text(
                    text = gobblegum.name.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = accentColor
                )

                // Rarity and Essence
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rarity badge
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = gobblegum.rarity.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // Essence value
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300))
                        )
                        Text(
                            text = "${gobblegum.essenceValue} Essence",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Pattern
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = gobblegum.pattern.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Short description
                Text(
                    text = gobblegum.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
        }
    }
}
