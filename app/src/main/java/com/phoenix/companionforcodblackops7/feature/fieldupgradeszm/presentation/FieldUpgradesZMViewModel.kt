package com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.model.FieldUpgradeZM
import com.phoenix.companionforcodblackops7.feature.fieldupgradeszm.domain.repository.FieldUpgradesZMRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Field Upgrades (Zombie mode) list
 */
sealed class FieldUpgradesZMUiState {
    data object Loading : FieldUpgradesZMUiState()
    data class Success(val fieldUpgrades: List<FieldUpgradeZM>) : FieldUpgradesZMUiState()
    data class Error(val message: String) : FieldUpgradesZMUiState()
}

/**
 * ViewModel for Field Upgrades (Zombie mode) list
 */
@HiltViewModel
class FieldUpgradesZMViewModel @Inject constructor(
    private val repository: FieldUpgradesZMRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FieldUpgradesZMUiState>(FieldUpgradesZMUiState.Loading)
    val uiState: StateFlow<FieldUpgradesZMUiState> = _uiState.asStateFlow()

    init {
        loadFieldUpgrades()
    }

    private fun loadFieldUpgrades() {
        viewModelScope.launch {
            repository.getFieldUpgrades()
                .catch { e ->
                    Timber.e(e, "Error loading field upgrades (ZM)")
                    _uiState.value = FieldUpgradesZMUiState.Error(
                        e.message ?: "Failed to load field upgrades"
                    )
                }
                .collect { fieldUpgrades ->
                    _uiState.value = FieldUpgradesZMUiState.Success(fieldUpgrades)
                }
        }
    }
}
