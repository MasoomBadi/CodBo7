package com.phoenix.companionforcodblackops7.feature.gobblegums.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGum
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.repository.GobbleGumsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for GobbleGums list screen
 */
sealed class GobbleGumsUiState {
    data object Loading : GobbleGumsUiState()
    data class Success(
        val gobblegums: List<GobbleGum>,
        val selectedRarity: String? = null,
        val selectedPattern: String? = null,
        val filteredGobblegums: List<GobbleGum> = gobblegums
    ) : GobbleGumsUiState()
    data class Error(val message: String) : GobbleGumsUiState()
}

/**
 * ViewModel for GobbleGums (Zombie mode)
 */
@HiltViewModel
class GobbleGumsViewModel @Inject constructor(
    private val repository: GobbleGumsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GobbleGumsUiState>(GobbleGumsUiState.Loading)
    val uiState: StateFlow<GobbleGumsUiState> = _uiState.asStateFlow()

    init {
        loadGobbleGums()
    }

    private fun loadGobbleGums() {
        viewModelScope.launch {
            repository.getGobbleGums()
                .catch { e ->
                    Timber.e(e, "Error loading gobblegums")
                    _uiState.value = GobbleGumsUiState.Error(
                        e.message ?: "Failed to load gobblegums"
                    )
                }
                .collect { gobblegums ->
                    _uiState.value = GobbleGumsUiState.Success(
                        gobblegums = gobblegums,
                        selectedRarity = null,
                        selectedPattern = null,
                        filteredGobblegums = gobblegums
                    )
                }
        }
    }

    /**
     * Filter gobblegums by rarity
     */
    fun filterByRarity(rarity: String?) {
        val currentState = _uiState.value
        if (currentState is GobbleGumsUiState.Success) {
            val filtered = applyFilters(
                currentState.gobblegums,
                rarity,
                currentState.selectedPattern
            )

            _uiState.value = currentState.copy(
                selectedRarity = rarity,
                filteredGobblegums = filtered
            )
        }
    }

    /**
     * Filter gobblegums by pattern
     */
    fun filterByPattern(pattern: String?) {
        val currentState = _uiState.value
        if (currentState is GobbleGumsUiState.Success) {
            val filtered = applyFilters(
                currentState.gobblegums,
                currentState.selectedRarity,
                pattern
            )

            _uiState.value = currentState.copy(
                selectedPattern = pattern,
                filteredGobblegums = filtered
            )
        }
    }

    /**
     * Apply both rarity and pattern filters
     */
    private fun applyFilters(
        gobblegums: List<GobbleGum>,
        rarity: String?,
        pattern: String?
    ): List<GobbleGum> {
        var filtered = gobblegums

        if (rarity != null) {
            filtered = filtered.filter { it.rarity.displayName.equals(rarity, ignoreCase = true) }
        }

        if (pattern != null) {
            filtered = filtered.filter { it.pattern.displayName.equals(pattern, ignoreCase = true) }
        }

        return filtered
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        val currentState = _uiState.value
        if (currentState is GobbleGumsUiState.Success) {
            _uiState.value = currentState.copy(
                selectedRarity = null,
                selectedPattern = null,
                filteredGobblegums = currentState.gobblegums
            )
        }
    }
}
