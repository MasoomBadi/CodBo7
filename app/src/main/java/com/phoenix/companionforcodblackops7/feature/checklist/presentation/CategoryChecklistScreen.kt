package com.phoenix.companionforcodblackops7.feature.checklist.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistItem

private const val BASE_URL = "http://codbo7.masoombadi.top"

private fun getCategoryAccentColor(category: ChecklistCategory): Color {
    return when (category) {
        ChecklistCategory.OPERATORS -> Color(0xFFF96800) // COD Orange
        ChecklistCategory.WEAPONS -> Color(0xFF00BCD4) // Cyan
        ChecklistCategory.MASTERY_BADGES -> Color(0xFFFFB300) // Gold
        ChecklistCategory.MAPS -> Color(0xFF76FF03) // Green
        ChecklistCategory.EQUIPMENT -> Color(0xFFE91E63) // Pink
        ChecklistCategory.PRESTIGE -> Color(0xFFFFB300) // Gold
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryChecklistScreen(
    onNavigateBack: () -> Unit,
    onWeaponClick: (weaponId: String, weaponName: String, weaponCategory: String) -> Unit = { _, _, _ -> },
    onMasteryBadgeClick: (weaponId: String, weaponName: String, weaponCategory: String) -> Unit = { _, _, _ -> },
    viewModel: CategoryChecklistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Weapon category filter state
    var selectedWeaponCategory by remember { mutableStateOf<String?>(null) }

    // Observe toast messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Get category accent color
    val categoryAccentColor = when (val state = uiState) {
        is CategoryChecklistUiState.Success -> getCategoryAccentColor(state.category)
        else -> MaterialTheme.colorScheme.secondary
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        when (val state = uiState) {
                            is CategoryChecklistUiState.Success -> {
                                Text(
                                    text = state.category.displayName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                )
                            }
                            else -> {
                                Text("Loading...")
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = categoryAccentColor
                    )
                )

                // Enhanced Progress section
                if (uiState is CategoryChecklistUiState.Success) {
                    val state = uiState as CategoryChecklistUiState.Success
                    val progress = if (state.totalCount > 0) {
                        state.unlockedCount.toFloat() / state.totalCount
                    } else 0f

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PROGRESS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "${state.unlockedCount} / ${state.totalCount} unlocked",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (val state = uiState) {
            is CategoryChecklistUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is CategoryChecklistUiState.Success -> {
                // Extract unique weapon categories and filter items (for WEAPONS and MASTERY_BADGES)
                val weaponCategories = if (state.category == ChecklistCategory.WEAPONS || state.category == ChecklistCategory.MASTERY_BADGES) {
                    state.items.mapNotNull { item ->
                        item.id.split("|").getOrNull(1)
                    }.distinct().sorted()
                } else emptyList()

                val filteredItems = if ((state.category == ChecklistCategory.WEAPONS || state.category == ChecklistCategory.MASTERY_BADGES) && selectedWeaponCategory != null) {
                    state.items.filter { item ->
                        val category = item.id.split("|").getOrNull(1)
                        category == selectedWeaponCategory
                    }
                } else {
                    state.items
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Weapon category filters (for WEAPONS and MASTERY_BADGES categories)
                    if ((state.category == ChecklistCategory.WEAPONS || state.category == ChecklistCategory.MASTERY_BADGES) && weaponCategories.isNotEmpty()) {
                        WeaponCategoryFilterRow(
                            categories = weaponCategories,
                            selectedCategory = selectedWeaponCategory,
                            onCategorySelected = { selectedWeaponCategory = it },
                            accentColor = categoryAccentColor
                        )
                    }

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
                            items = filteredItems,
                            key = { it.id }
                        ) { item ->
                            EnhancedChecklistItemCard(
                                item = item,
                                accentColor = categoryAccentColor,
                                category = state.category,
                                onToggle = {
                                    when (state.category) {
                                        ChecklistCategory.WEAPONS -> {
                                            // Navigate to Weapon Camos screen
                                            val parts = item.id.split("|")
                                            val weaponId = parts.getOrNull(0) ?: item.id
                                            val weaponCategory = parts.getOrNull(1) ?: "Assault Rifle"
                                            onWeaponClick(weaponId, item.name, weaponCategory)
                                        }
                                        ChecklistCategory.MASTERY_BADGES -> {
                                            // Navigate to Weapon Mastery screen
                                            val parts = item.id.split("|")
                                            val weaponId = parts.getOrNull(0) ?: item.id
                                            val weaponCategory = parts.getOrNull(1) ?: "Assault Rifle"
                                            onMasteryBadgeClick(weaponId, item.name, weaponCategory)
                                        }
                                        else -> {
                                            // Toggle unlock status for other categories
                                            viewModel.toggleItemUnlocked(item.id)
                                        }
                                    }
                                }
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
    }
}

/**
 * Weapon category filter row (horizontal scrolling chips)
 */
@Composable
private fun WeaponCategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" filter chip
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = {
                Text(
                    text = "ALL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = accentColor.copy(alpha = 0.3f),
                selectedLabelColor = accentColor
            )
        )

        // Category filter chips
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor.copy(alpha = 0.3f),
                    selectedLabelColor = accentColor
                )
            )
        }
    }
}

/**
 * Modern checklist item card with beautiful, clean design
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EnhancedChecklistItemCard(
    item: ChecklistItem,
    accentColor: Color,
    category: ChecklistCategory,
    onToggle: () -> Unit
) {
    // Glow animation for unlocked items
    val infiniteTransition = rememberInfiniteTransition(label = "itemGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val borderColor = if (item.isUnlocked) {
        accentColor.copy(alpha = glowAlpha * 0.7f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val cardColor = if (item.isUnlocked) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
    }

    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (item.isUnlocked) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isUnlocked) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image section with status overlay
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background glow for unlocked items
                if (item.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.3f * glowAlpha),
                                        accentColor.copy(alpha = 0.1f * glowAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Image container
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (item.isUnlocked) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    item.imageUrl?.let { url ->
                        AsyncImage(
                            model = "$BASE_URL$url",
                            contentDescription = item.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(85.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colorFilter = if (!item.isUnlocked) {
                                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                            } else null
                        )
                    } ?: run {
                        // Placeholder if no image
                        Icon(
                            imageVector = if (item.isUnlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(45.dp),
                            tint = if (item.isUnlocked) accentColor else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Status badge overlay on image
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (item.isUnlocked) {
                                accentColor
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = if (item.isUnlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp),
                                tint = if (item.isUnlocked) {
                                    MaterialTheme.colorScheme.onSecondary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    }
                }
            }

            // Content section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name with status indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (item.isUnlocked) {
                            accentColor
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Weapon category badge (for WEAPONS and MASTERY_BADGES categories)
                if (category == ChecklistCategory.WEAPONS || category == ChecklistCategory.MASTERY_BADGES) {
                    val weaponCategory = item.id.split("|").getOrNull(1) ?: "Unknown"
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = weaponCategory.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Unlock criteria chip
                item.unlockCriteria?.let { criteria ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (item.isUnlocked) accentColor else MaterialTheme.colorScheme.outline
                                )
                        )
                        Text(
                            text = criteria,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (item.isUnlocked) {
                            accentColor.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.isUnlocked) accentColor else MaterialTheme.colorScheme.outline
                                    )
                            )
                            Text(
                                text = if (item.isUnlocked) "UNLOCKED" else "LOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                ),
                                color = if (item.isUnlocked) {
                                    accentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Tap to toggle hint (only for locked items)
                    if (!item.isUnlocked) {
                        Text(
                            text = "• Tap to unlock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Toggle button (simplified)
            AnimatedVisibility(
                visible = item.isUnlocked,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = glowAlpha),
                                    accentColor.copy(alpha = 0.85f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Unlocked",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}
