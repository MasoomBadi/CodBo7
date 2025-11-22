package com.phoenix.companionforcodblackops7.feature.weaponcamos.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.Camo
import android.widget.Toast

private const val BASE_URL = "http://codbo7.masoombadi.top"

/**
 * Weapon Camos Detail Screen - Shows all 54 camos for a weapon with mode tabs
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeaponCamosScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeaponCamosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMode by remember { mutableStateOf("campaign") } // Dynamic String mode instead of enum
    var expandedCamoId by remember { mutableStateOf<Int?>(null) }
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        when (val state = uiState) {
                            is WeaponCamosUiState.Success -> {
                                Text(
                                    text = state.progress.weaponName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                )
                            }
                            else -> Text("Loading...")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = Color(0xFF00BCD4) // Cyan
                    )
                )

                // Progress bar
                if (uiState is WeaponCamosUiState.Success) {
                    val state = uiState as WeaponCamosUiState.Success
                    val progress = state.progress.percentage / 100f

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Weapon category badge
                            Surface(
                                color = Color(0xFF00BCD4).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = state.weaponCategory.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = Color(0xFF00BCD4),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.progress.unlockedCount}/${state.progress.totalCamos} CAMOS UNLOCKED",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${state.progress.percentage.toInt()}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF00BCD4)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF00BCD4),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Mode tabs
                ModeTabRow(
                    selectedMode = selectedMode,
                    onModeSelected = { selectedMode = it },
                    availableModes = when (val state = uiState) {
                        is WeaponCamosUiState.Success -> state.progress.camosByMode.keys.toList()
                        else -> emptyList()
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (val state = uiState) {
            is WeaponCamosUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is WeaponCamosUiState.Success -> {
                val camosForMode = state.progress.camosByMode[selectedMode] ?: emptyList()
                // Group camos by category
                val camosByCategory = camosForMode.groupBy { it.category }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Camos list grouped by category with expandable sections
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // For each category
                        camosByCategory.forEach { (category, camos) ->
                            // Category header
                            item(key = "header_$category") {
                                CategoryHeader(
                                    category = category,
                                    categoryDisplayName = camos.firstOrNull()?.categoryDisplayName ?: category,
                                    isExpanded = expandedCategories.contains(category),
                                    onToggle = {
                                        expandedCategories = if (expandedCategories.contains(category)) {
                                            expandedCategories - category
                                        } else {
                                            expandedCategories + category
                                        }
                                    },
                                    completedCount = camos.count { it.isUnlocked },
                                    totalCount = camos.size
                                )
                            }

                            // Category camos (only if expanded)
                            if (expandedCategories.contains(category)) {
                                items(
                                    items = camos,
                                    key = { it.id }
                                ) { camo ->
                                    ExpandableCamoCard(
                                        camo = camo,
                                        isExpanded = expandedCamoId == camo.id,
                                        onToggleExpand = {
                                            if (camo.isLocked) {
                                                Toast.makeText(
                                                    context,
                                                    "Complete previous camos in this category first",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                expandedCamoId = if (expandedCamoId == camo.id) null else camo.id
                                            }
                                        },
                                        onCriterionToggle = { criterionId, isLocked ->
                                            if (isLocked) {
                                                Toast.makeText(
                                                    context,
                                                    "Complete previous challenges first",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                viewModel.toggleCriterionCompletion(camo.id, criterionId, isLocked)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Fixed Banner Ad Space
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
            is WeaponCamosUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeTabRow(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    availableModes: List<String>,
    modifier: Modifier = Modifier
) {
    if (availableModes.isEmpty()) return

    ScrollableTabRow(
        selectedTabIndex = availableModes.indexOf(selectedMode).coerceAtLeast(0),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = Color(0xFF00BCD4),
        edgePadding = 16.dp,
        modifier = modifier
    ) {
        availableModes.forEach { mode ->
            Tab(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                text = {
                    Text(
                        text = formatModeDisplayName(mode).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            )
        }
    }
}

/**
 * Format mode string to display name for UI
 * Matches backend formatting
 */
private fun formatModeDisplayName(mode: String): String {
    return when (mode.lowercase()) {
        "campaign" -> "Campaign"
        "multiplayer", "mp" -> "Multiplayer"
        "zombie", "zm" -> "Zombie"
        "prestige" -> "Prestige"
        else -> mode.capitalize()
    }
}

/**
 * Category header with collapsible functionality
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryHeader(
    category: String,
    categoryDisplayName: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF00BCD4)

    Card(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/Collapse icon
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )

            // Category name
            Text(
                text = categoryDisplayName.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = accentColor,
                modifier = Modifier.weight(1f)
            )

            // Progress badge
            Surface(
                color = accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Expandable camo card showing camo name, image, and criteria with checkboxes
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpandableCamoCard(
    camo: Camo,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCriterionToggle: (criterionId: Int, isLocked: Boolean) -> Unit
) {
    val accentColor = Color(0xFF00BCD4)
    val infiniteTransition = rememberInfiniteTransition(label = "camoGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColor = if (camo.isUnlocked) {
        accentColor.copy(alpha = glowAlpha * 0.7f)
    } else if (camo.isLocked) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (camo.isUnlocked) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (camo.isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else if (camo.isLocked) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (camo.isUnlocked) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !camo.isLocked) { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Camo image thumbnail
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = "$BASE_URL${camo.camoUrl}",
                        contentDescription = camo.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Unlock status overlay
                    when {
                        camo.isUnlocked -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = accentColor
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Unlocked",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                        camo.isLocked -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked - Complete previous camos first",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Not unlocked",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Camo info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Camo name
                    Text(
                        text = camo.displayName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = when {
                            camo.isUnlocked -> accentColor
                            camo.isLocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )

                    // Category badge
                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = camo.categoryDisplayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Progress indicator
                    if (camo.totalCriteriaCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { camo.completedCriteriaCount.toFloat() / camo.totalCriteriaCount },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = accentColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${camo.completedCriteriaCount}/${camo.totalCriteriaCount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Expand icon (only if not locked)
                if (!camo.isLocked) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = accentColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked - Complete previous camos",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Expanded criteria section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (camo.criteria.isNotEmpty()) {
                        Text(
                            text = "UNLOCK CRITERIA",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Criteria checkboxes
                        camo.criteria.forEach { criterion ->
                            CriterionRow(
                                criterion = criterion,
                                onToggle = { onCriterionToggle(criterion.id, criterion.isLocked) },
                                accentColor = accentColor
                            )
                        }
                    } else {
                        Text(
                            text = "No criteria defined for this camo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual criterion row with checkbox
 */
@Composable
private fun CriterionRow(
    criterion: com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.CamoCriteria,
    onToggle: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (criterion.isCompleted) {
                    accentColor.copy(alpha = 0.1f)
                } else if (criterion.isLocked) {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .clickable(enabled = !criterion.isLocked) { onToggle() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Order badge
        Surface(
            shape = CircleShape,
            color = if (criterion.isCompleted) {
                accentColor
            } else if (criterion.isLocked) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (criterion.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${criterion.criteriaOrder}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (criterion.isLocked) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }

        // Criteria text
        Text(
            text = criterion.criteriaText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (criterion.isCompleted) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (criterion.isLocked) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            } else if (criterion.isCompleted) {
                accentColor
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )

        // Checkbox
        Checkbox(
            checked = criterion.isCompleted,
            onCheckedChange = { if (!criterion.isLocked) onToggle() },
            enabled = !criterion.isLocked,
            colors = CheckboxDefaults.colors(
                checkedColor = accentColor,
                uncheckedColor = if (criterion.isLocked) {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                } else {
                    accentColor.copy(alpha = 0.6f)
                }
            )
        )

        // Lock icon for locked criteria
        if (criterion.isLocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
