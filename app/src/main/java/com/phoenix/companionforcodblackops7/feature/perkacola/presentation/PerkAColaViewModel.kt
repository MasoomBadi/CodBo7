package com.phoenix.companionforcodblackops7.feature.perkacola.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.perkacola.domain.model.PerkACola
import com.phoenix.companionforcodblackops7.feature.perkacola.domain.repository.PerkAColaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Perk-a-Cola screen
 */
sealed class PerkAColaUiState {
    data object Loading : PerkAColaUiState()
    data class Success(val perkAColas: List<PerkACola>) : PerkAColaUiState()
    data class Error(val message: String) : PerkAColaUiState()
}

/**
 * ViewModel for managing Perk-a-Cola UI state
 */
@HiltViewModel
class PerkAColaViewModel @Inject constructor(
    private val perkAColaRepository: PerkAColaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerkAColaUiState>(PerkAColaUiState.Loading)
    val uiState: StateFlow<PerkAColaUiState> = _uiState.asStateFlow()

    init {
        loadPerkAColas()
    }

    /**
     * Load all Perk-a-Colas from the repository
     */
    fun loadPerkAColas() {
        viewModelScope.launch {
            _uiState.value = PerkAColaUiState.Loading
            perkAColaRepository.getAllPerkAColas()
                .catch { exception ->
                    Timber.e(exception, "Failed to load Perk-a-Colas")
                    _uiState.value = PerkAColaUiState.Error(
                        exception.message ?: "Failed to load Perk-a-Colas"
                    )
                }
                .collect { perkAColas ->
                    _uiState.value = if (perkAColas.isEmpty()) {
                        PerkAColaUiState.Error("No Perk-a-Colas available")
                    } else {
                        PerkAColaUiState.Success(perkAColas)
                    }
                }
        }
    }
}
