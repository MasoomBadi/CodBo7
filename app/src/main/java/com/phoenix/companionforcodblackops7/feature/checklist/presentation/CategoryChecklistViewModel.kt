package com.phoenix.companionforcodblackops7.feature.checklist.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistItem
import com.phoenix.companionforcodblackops7.feature.checklist.domain.repository.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CategoryChecklistUiState {
    data object Loading : CategoryChecklistUiState
    data class Success(
        val category: ChecklistCategory,
        val items: List<ChecklistItem>,
        val unlockedCount: Int,
        val totalCount: Int
    ) : CategoryChecklistUiState
}

@HiltViewModel
class CategoryChecklistViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryName: String = savedStateHandle.get<String>("category") ?: "OPERATORS"
    private val category: ChecklistCategory = ChecklistCategory.valueOf(categoryName)

    val uiState: StateFlow<CategoryChecklistUiState> = checklistRepository.getChecklistItems(category)
        .map { items ->
            CategoryChecklistUiState.Success(
                category = category,
                items = items,
                unlockedCount = items.count { it.isUnlocked },
                totalCount = items.size
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryChecklistUiState.Loading
        )

    fun toggleItemUnlocked(itemId: String) {
        viewModelScope.launch {
            checklistRepository.toggleItemUnlocked(itemId, category)
        }
    }
}
