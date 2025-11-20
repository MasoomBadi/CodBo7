package com.phoenix.companionforcodblackops7.feature.checklist.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistCategory
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistItem
import com.phoenix.companionforcodblackops7.feature.checklist.domain.repository.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _toastMessage = Channel<String>()
    val toastMessage = _toastMessage.receiveAsFlow()

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
            // For Classic Prestige, validate sequential unlock
            if (category == ChecklistCategory.PRESTIGE) {
                val items = checklistRepository.getChecklistItems(category).first()
                val currentItem = items.find { it.id == itemId }

                if (currentItem != null && !currentItem.isUnlocked) {
                    // Extract prestige number from id (e.g., "prestige_2" -> 2)
                    val currentPrestigeNum = itemId.removePrefix("prestige_").toIntOrNull()

                    if (currentPrestigeNum != null && currentPrestigeNum > 1) {
                        // Check if previous prestige is unlocked
                        val previousPrestigeId = "prestige_${currentPrestigeNum - 1}"
                        val previousPrestige = items.find { it.id == previousPrestigeId }

                        if (previousPrestige?.isUnlocked == false) {
                            _toastMessage.send("You must unlock Prestige ${currentPrestigeNum - 1} first!")
                            return@launch
                        }
                    }
                }
            }

            checklistRepository.toggleItemUnlocked(itemId, category)
        }
    }
}
