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
            // For Classic Prestige, validate strict sequential unlocking
            // ALL lower levels must be unlocked before unlocking a higher level
            if (category == ChecklistCategory.PRESTIGE) {
                val items = checklistRepository.getChecklistItems(category).first()
                val currentItem = items.find { it.id == itemId }

                if (currentItem != null && !currentItem.isUnlocked) {
                    // Extract prestige level from unlock criteria (e.g., "Prestige 2" -> 2)
                    // Try multiple formats: "prestige_2", "Prestige 2", or just parse from name
                    val currentLevel = extractPrestigeLevel(itemId, currentItem.name)

                    if (currentLevel != null && currentLevel > 1) {
                        // Check if ALL previous prestige levels are unlocked
                        val unlockedLevels = items
                            .filter { it.isUnlocked }
                            .mapNotNull { extractPrestigeLevel(it.id, it.name) }
                            .toSet()

                        // Find the first missing level
                        val missingLevel = (1 until currentLevel).firstOrNull { level ->
                            level !in unlockedLevels
                        }

                        if (missingLevel != null) {
                            _toastMessage.send("You must unlock all previous Prestige levels first! Missing: Prestige $missingLevel")
                            return@launch
                        }
                    }
                }
            }

            checklistRepository.toggleItemUnlocked(itemId, category)
        }
    }

    /**
     * Extract prestige level number from ID or name
     * Handles multiple formats: "prestige_2", "2", "Prestige 2"
     * Fully dynamic - works with any numeric ID format from database
     */
    private fun extractPrestigeLevel(id: String, name: String): Int? {
        // Try parsing ID directly as number (e.g., "1", "2", "3")
        id.toIntOrNull()?.let { return it }

        // Try parsing from ID with prefix (e.g., "prestige_2" or "prestige-2")
        val idNumberRegex = "\\d+".toRegex()
        idNumberRegex.find(id)?.value?.toIntOrNull()?.let { return it }

        // Try parsing from name (e.g., "Prestige 2" or "Prestige Level 2")
        idNumberRegex.find(name)?.value?.toIntOrNull()?.let { return it }

        return null
    }
}
