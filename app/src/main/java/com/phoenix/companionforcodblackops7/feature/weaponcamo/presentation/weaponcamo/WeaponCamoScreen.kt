package com.phoenix.companionforcodblackops7.feature.weaponcamo.presentation.weaponcamo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoMode
import kotlinx.coroutines.flow.distinctUntilChanged

private const val BASE_URL = "http://codbo7.masoombadi.top"

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeaponCamoScreen(
    onCamoClick: (Int, Int) -> Unit, // weaponId, camoId
    padding: PaddingValues,
    onNavigateBack: () -> Unit = {},
    viewModel: WeaponCamoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                val selectedTabIndex = modes.indexOf(state.selectedMode).coerceAtLeast(0)

                val pagerState = rememberPagerState(
                    initialPage = selectedTabIndex,
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
                    // Mode tabs - scrollable to show full text
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

                    // Camo grid with HorizontalPager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        if (page == pagerState.currentPage) {
                            CamoGrid(
                                camoCategories = state.camoCategories,
                                weaponId = state.weapon.id,
                                onCamoClick = onCamoClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeaponHeader(
    weaponName: String,
    progress: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = weaponName.uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = CODOrange
        )
        Text(
            text = "$progress Camos Unlocked",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CamoGrid(
    camoCategories: List<CamoCategory>,
    weaponId: Int,
    onCamoClick: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        camoCategories.forEach { category ->
            // Category header
            item(key = "category_${category.name}") {
                CategoryHeader(category = category)
            }

            // Camos in this category
            items(
                items = category.camos,
                key = { "camo_${it.id}" }
            ) { camo ->
                CamoCard(
                    camo = camo,
                    onClick = {
                        if (!camo.isLocked) {
                            onCamoClick(weaponId, camo.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: CamoCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.displayName.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            ),
            color = if (category.isLocked) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                CODOrange
            }
        )

        Text(
            text = "${category.completedCount}/${category.totalCount}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (category.isComplete) CODOrange else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CamoCard(
    camo: Camo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (camo.isCompleted) 2.dp else 1.dp,
                color = if (camo.isCompleted) CODOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (camo.isLocked) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camo image
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect if completed
                if (camo.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
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
                        .size(55.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colorFilter = if (camo.isLocked) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    } else null
                )

                // Lock/Check overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (camo.isCompleted) {
                            CODOrange
                        } else if (camo.isLocked) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            Color.Transparent
                        },
                        shadowElevation = if (camo.isCompleted || camo.isLocked) 2.dp else 0.dp
                    ) {
                        Icon(
                            imageVector = if (camo.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(2.dp),
                            tint = if (camo.isCompleted) {
                                MaterialTheme.colorScheme.onSecondary
                            } else if (camo.isLocked) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                Color.Transparent
                            }
                        )
                    }
                }
            }

            // Camo info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = camo.displayName.uppercase(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    ),
                    color = if (camo.isCompleted) {
                        CODOrange
                    } else if (camo.isLocked) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (camo.criteriaCount > 0) {
                    Text(
                        text = if (camo.isLocked) {
                            "Complete previous camos to unlock"
                        } else {
                            "${camo.completedCriteriaCount}/${camo.criteriaCount} challenges"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
