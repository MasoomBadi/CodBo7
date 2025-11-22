package com.phoenix.companionforcodblackops7.feature.weaponcamo.presentation.camodetail

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.core.ui.theme.CODOrange
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCriteria

private const val BASE_URL = "http://codbo7.masoombadi.top"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CamoDetailScreen(
    padding: PaddingValues,
    viewModel: CamoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is CamoDetailUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }

        is CamoDetailUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Camo image header (if URL is available)
                if (state.camoUrl.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "$BASE_URL${state.camoUrl}",
                            contentDescription = state.camoName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }

                // Camo name header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = state.camoName.uppercase(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = CODOrange
                        )

                        val completedCount = state.criteria.count { it.isCompleted }
                        val totalCount = state.criteria.size
                        val isComplete = completedCount == totalCount && totalCount > 0

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isComplete) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Complete",
                                    tint = CODOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = if (isComplete) {
                                    "COMPLETED"
                                } else {
                                    "$completedCount/$totalCount Challenges Complete"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isComplete) CODOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Criteria list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = state.criteria,
                        key = { _, criteria -> "criteria_${criteria.id}" }
                    ) { index, criteria ->
                        CriteriaCard(
                            criteria = criteria,
                            index = index + 1,
                            onToggle = { viewModel.toggleCriterion(criteria.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CriteriaCard(
    criteria: CamoCriteria,
    index: Int,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (criteria.isCompleted) 2.dp else 1.dp,
                color = if (criteria.isCompleted) CODOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (criteria.isCompleted) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Challenge number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (criteria.isCompleted) CODOrange else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (criteria.isCompleted) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Criteria text
            Text(
                text = criteria.criteriaText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (criteria.isCompleted) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.weight(1f)
            )

            // Checkbox
            Checkbox(
                checked = criteria.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CODOrange,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
