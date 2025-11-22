package com.phoenix.companionforcodblackops7.feature.weaponcamo.presentation.weaponcamo

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.core.ui.theme.CODOrange
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Camo
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCategory
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCriteria
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoMode
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val BASE_URL = "http://codbo7.masoombadi.top"

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeaponCamoScreen(
    onCamoClick: (Int, Int) -> Unit = { _, _ -> },
    padding: PaddingValues,
    onNavigateBack: () -> Unit = {},
    viewModel: WeaponCamoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // Bottom sheet state
    var selectedCamo by remember { mutableStateOf<Camo?>(null) }
    var selectedCamoCriteria by remember { mutableStateOf<List<CamoCriteria>>(emptyList()) }
    var isLoadingCriteria by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Load criteria when camo is selected
    LaunchedEffect(selectedCamo) {
        selectedCamo?.let { camo ->
            if (!camo.isLocked) {
                isLoadingCriteria = true
                val state = uiState
                if (state is WeaponCamoUiState.Success) {
                    selectedCamoCriteria = viewModel.loadCriteria(state.weapon.id, camo.id)
                }
                isLoadingCriteria = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is WeaponCamoUiState.Success -> {
                            Column {
                                Text(
                                    text = state.weapon.displayName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${state.weapon.completedCamos}/${state.weapon.totalCamos} Camos",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CODOrange
                                )
                            }
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
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { scaffoldPadding ->
        when (val state = uiState) {
            is WeaponCamoUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is WeaponCamoUiState.Success -> {
                val modes = CamoMode.entries
                val pagerState = rememberPagerState(
                    initialPage = modes.indexOf(state.selectedMode).coerceAtLeast(0),
                    pageCount = { modes.size }
                )

                // Sync pager -> viewModel (when user swipes)
                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.settledPage }
                        .distinctUntilChanged()
                        .collect { page ->
                            val mode = modes.getOrNull(page)
                            if (mode != null && mode != state.selectedMode) {
                                viewModel.selectMode(mode)
                            }
                        }
                }

                // Sync viewModel -> pager (when user clicks tab)
                LaunchedEffect(state.selectedMode) {
                    val targetPage = modes.indexOf(state.selectedMode)
                    if (targetPage >= 0 && targetPage != pagerState.currentPage && !pagerState.isScrollInProgress) {
                        pagerState.animateScrollToPage(targetPage)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                ) {
                    // Mode tabs
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = CODOrange,
                        edgePadding = 0.dp
                    ) {
                        modes.forEachIndexed { index, mode ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { viewModel.selectMode(mode) },
                                text = {
                                    Text(
                                        text = mode.displayName.uppercase(),
                                        fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }

                    // Camo content with HorizontalPager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        beyondViewportPageCount = 1 // Pre-render adjacent pages for smoother swiping
                    ) { page ->
                        CamoContent(
                            camoCategories = state.camoCategories,
                            weaponId = state.weapon.id,
                            onCamoClick = { camo ->
                                if (!camo.isLocked) {
                                    selectedCamo = camo
                                    showBottomSheet = true
                                }
                            }
                        )
                    }
                }

                // Bottom Sheet for Camo Details
                if (showBottomSheet && selectedCamo != null) {
                    CamoDetailBottomSheet(
                        camo = selectedCamo!!,
                        criteria = selectedCamoCriteria,
                        isLoading = isLoadingCriteria,
                        sheetState = bottomSheetState,
                        onDismiss = {
                            showBottomSheet = false
                            selectedCamo = null
                            selectedCamoCriteria = emptyList()
                        },
                        onToggleCriterion = { criterionId ->
                            val camo = selectedCamo ?: return@CamoDetailBottomSheet
                            // Optimistic update
                            selectedCamoCriteria = selectedCamoCriteria.map { criterion ->
                                if (criterion.id == criterionId) {
                                    criterion.copy(isCompleted = !criterion.isCompleted)
                                } else {
                                    criterion
                                }
                            }
                            // Persist to database
                            coroutineScope.launch {
                                viewModel.toggleCriterionSuspend(state.weapon.id, camo.id, criterionId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CamoContent(
    camoCategories: List<CamoCategory>,
    weaponId: Int,
    onCamoClick: (Camo) -> Unit
) {
    // Calculate mode totals
    val totalCamos = camoCategories.sumOf { it.totalCount }
    val completedCamos = camoCategories.sumOf { it.completedCount }
    val modeProgress = if (totalCamos > 0) completedCamos.toFloat() / totalCamos.toFloat() else 0f
    val isModeComplete = totalCamos > 0 && completedCamos == totalCamos

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode progress summary header
        item(
            key = "mode_summary",
            span = { GridItemSpan(3) }
        ) {
            ModeProgressSummary(
                completedCamos = completedCamos,
                totalCamos = totalCamos,
                progress = modeProgress,
                isComplete = isModeComplete
            )
        }

        camoCategories.forEach { category ->
            // Category header - spans full width
            item(
                key = "header_${category.name}",
                span = { GridItemSpan(3) }
            ) {
                CategoryHeader(category = category)
            }

            // Camos in grid
            items(
                items = category.camos,
                key = { "camo_${it.id}" }
            ) { camo ->
                CamoGridItem(
                    camo = camo,
                    onClick = { onCamoClick(camo) }
                )
            }
        }
    }
}

@Composable
private fun ModeProgressSummary(
    completedCamos: Int,
    totalCamos: Int,
    progress: Float,
    isComplete: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "modeProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) {
                CODOrange.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isComplete) "MODE COMPLETE" else "MODE PROGRESS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isComplete) CODOrange else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedCamos / $totalCamos camos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Percentage circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isComplete) CODOrange else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = if (isComplete) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CODOrange,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryHeader(category: CamoCategory) {
    val completionProgress = if (category.totalCount > 0) {
        category.completedCount.toFloat() / category.totalCount.toFloat()
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (category.isLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                Text(
                    text = category.displayName.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = if (category.isLocked) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else if (category.isComplete) {
                        CODOrange
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Text(
                text = "${category.completedCount}/${category.totalCount}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (category.isComplete) CODOrange else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { completionProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (category.isLocked) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            } else {
                CODOrange
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun CamoGridItem(
    camo: Camo,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            camo.isCompleted -> CODOrange
            camo.isLocked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        },
        label = "borderColor"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (camo.isCompleted) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (camo.isLocked) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = !camo.isLocked
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camo image with status overlay
            Box(
                modifier = Modifier.size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect if completed
                if (camo.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        CODOrange.copy(alpha = 0.25f),
                                        CODOrange.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                AsyncImage(
                    model = "$BASE_URL${camo.camoUrl}",
                    contentDescription = camo.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(65.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colorFilter = if (camo.isLocked) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    } else null
                )

                // Status badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    when {
                        camo.isCompleted -> {
                            Surface(
                                shape = CircleShape,
                                color = CODOrange,
                                shadowElevation = 2.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(2.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        camo.isLocked -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 2.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(3.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Camo name
            Text(
                text = camo.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (camo.isCompleted) {
                    CODOrange
                } else if (camo.isLocked) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Progress text
            if (camo.criteriaCount > 0) {
                Text(
                    text = if (camo.isLocked) {
                        "Locked"
                    } else {
                        "${camo.completedCriteriaCount}/${camo.criteriaCount}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (camo.isCompleted) {
                        CODOrange.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CamoDetailBottomSheet(
    camo: Camo,
    criteria: List<CamoCriteria>,
    isLoading: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onToggleCriterion: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Camo header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camo image
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (camo.isCompleted) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            CODOrange.copy(alpha = 0.3f),
                                            CODOrange.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    AsyncImage(
                        model = "$BASE_URL${camo.camoUrl}",
                        contentDescription = camo.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(75.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = camo.displayName.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (camo.isCompleted) CODOrange else MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = camo.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (criteria.isNotEmpty()) {
                        val completedCount = criteria.count { it.isCompleted }
                        val progress = completedCount.toFloat() / criteria.size.toFloat()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CODOrange,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )

                            Text(
                                text = "$completedCount/${criteria.size}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (completedCount == criteria.size) CODOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            // Challenges header
            Text(
                text = "CHALLENGES",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Criteria list
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else if (criteria.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No challenges found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(criteria.size) { index ->
                        val criterion = criteria[index]
                        CriterionItem(
                            criterion = criterion,
                            index = index + 1,
                            onToggle = { onToggleCriterion(criterion.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CriterionItem(
    criterion: CamoCriteria,
    index: Int,
    onToggle: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (criterion.isCompleted) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "bgColor"
    )

    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (criterion.isCompleted) 1.5.dp else 1.dp,
                color = if (criterion.isCompleted) CODOrange.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (criterion.isCompleted) CODOrange else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (criterion.isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Criterion text
            Text(
                text = criterion.criteriaText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (criterion.isCompleted) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.weight(1f)
            )

            // Checkbox
            Checkbox(
                checked = criterion.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CODOrange,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = Color.White
                )
            )
        }
    }
}
