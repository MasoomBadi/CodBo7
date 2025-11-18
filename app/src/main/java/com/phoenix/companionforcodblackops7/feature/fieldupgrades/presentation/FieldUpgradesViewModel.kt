package com.phoenix.companionforcodblackops7.feature.fieldupgrades.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.fieldupgrades.domain.model.FieldUpgrade
import com.phoenix.companionforcodblackops7.feature.fieldupgrades.domain.repository.FieldUpgradesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Field Upgrades screen
 */
sealed class FieldUpgradesUiState {
    data object Loading : FieldUpgradesUiState()
    data class Success(val fieldUpgrades: List<FieldUpgrade>) : FieldUpgradesUiState()
    data class Error(val message: String) : FieldUpgradesUiState()
}

/**
 * ViewModel for managing Field Upgrades UI state
 */
@HiltViewModel
class FieldUpgradesViewModel @Inject constructor(
    private val fieldUpgradesRepository: FieldUpgradesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FieldUpgradesUiState>(FieldUpgradesUiState.Loading)
    val uiState: StateFlow<FieldUpgradesUiState> = _uiState.asStateFlow()

    init {
        loadFieldUpgrades()
    }

    /**
     * Load all field upgrades from the repository
     */
    fun loadFieldUpgrades() {
        viewModelScope.launch {
            _uiState.value = FieldUpgradesUiState.Loading
            fieldUpgradesRepository.getAllFieldUpgrades()
                .catch { exception ->
                    Timber.e(exception, "Failed to load field upgrades")
                    _uiState.value = FieldUpgradesUiState.Error(
                        exception.message ?: "Failed to load field upgrades"
                    )
                }
                .collect { fieldUpgrades ->
                    _uiState.value = if (fieldUpgrades.isEmpty()) {
                        FieldUpgradesUiState.Error("No field upgrades available")
                    } else {
                        FieldUpgradesUiState.Success(fieldUpgrades)
                    }
                }
        }
    }
}
