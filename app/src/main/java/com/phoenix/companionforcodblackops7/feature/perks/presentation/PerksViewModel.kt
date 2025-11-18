package com.phoenix.companionforcodblackops7.feature.perks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.perks.domain.model.Perk
import com.phoenix.companionforcodblackops7.feature.perks.domain.repository.PerksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Perks screen
 */
sealed class PerksUiState {
    data object Loading : PerksUiState()
    data class Success(val perks: List<Perk>) : PerksUiState()
    data class Error(val message: String) : PerksUiState()
}

/**
 * ViewModel for managing Perks UI state
 */
@HiltViewModel
class PerksViewModel @Inject constructor(
    private val perksRepository: PerksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerksUiState>(PerksUiState.Loading)
    val uiState: StateFlow<PerksUiState> = _uiState.asStateFlow()

    private val _selectedPerk = MutableStateFlow<Perk?>(null)
    val selectedPerk: StateFlow<Perk?> = _selectedPerk.asStateFlow()

    init {
        loadPerks()
    }

    /**
     * Load all perks from the repository
     */
    fun loadPerks() {
        viewModelScope.launch {
            _uiState.value = PerksUiState.Loading
            perksRepository.getAllPerks()
                .catch { exception ->
                    Timber.e(exception, "Failed to load perks")
                    _uiState.value = PerksUiState.Error(
                        exception.message ?: "Failed to load perks"
                    )
                }
                .collect { perks ->
                    _uiState.value = if (perks.isEmpty()) {
                        PerksUiState.Error("No perks available")
                    } else {
                        PerksUiState.Success(perks)
                    }
                }
        }
    }

    /**
     * Select a perk to view details
     */
    fun selectPerk(perk: Perk) {
        _selectedPerk.value = perk
    }

    /**
     * Clear selected perk
     */
    fun clearSelectedPerk() {
        _selectedPerk.value = null
    }

    /**
     * Get perks grouped by slot
     */
    fun getPerksGroupedBySlot(perks: List<Perk>): Map<Int, List<Perk>> {
        return perks.groupBy { it.slot }
    }

    /**
     * Get perks grouped by category
     */
    fun getPerksGroupedByCategory(perks: List<Perk>): Map<String, List<Perk>> {
        return perks.groupBy { it.category }
    }
}
