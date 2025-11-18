package com.phoenix.companionforcodblackops7.feature.combatspecialties.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.model.CombatSpecialty
import com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.repository.CombatSpecialtiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Combat Specialties screen
 */
sealed class CombatSpecialtiesUiState {
    data object Loading : CombatSpecialtiesUiState()
    data class Success(val specialties: List<CombatSpecialty>) : CombatSpecialtiesUiState()
    data class Error(val message: String) : CombatSpecialtiesUiState()
}

/**
 * ViewModel for managing Combat Specialties UI state
 */
@HiltViewModel
class CombatSpecialtiesViewModel @Inject constructor(
    private val combatSpecialtiesRepository: CombatSpecialtiesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CombatSpecialtiesUiState>(CombatSpecialtiesUiState.Loading)
    val uiState: StateFlow<CombatSpecialtiesUiState> = _uiState.asStateFlow()

    init {
        loadCombatSpecialties()
    }

    /**
     * Load all combat specialties from the repository
     */
    fun loadCombatSpecialties() {
        viewModelScope.launch {
            _uiState.value = CombatSpecialtiesUiState.Loading
            combatSpecialtiesRepository.getAllCombatSpecialties()
                .catch { exception ->
                    Timber.e(exception, "Failed to load combat specialties")
                    _uiState.value = CombatSpecialtiesUiState.Error(
                        exception.message ?: "Failed to load combat specialties"
                    )
                }
                .collect { specialties ->
                    _uiState.value = if (specialties.isEmpty()) {
                        CombatSpecialtiesUiState.Error("No combat specialties available")
                    } else {
                        CombatSpecialtiesUiState.Success(specialties)
                    }
                }
        }
    }

    /**
     * Get combat specialties grouped by type (Core vs Hybrid)
     */
    fun getCombatSpecialtiesGroupedByType(specialties: List<CombatSpecialty>): Map<String, List<CombatSpecialty>> {
        return specialties.groupBy { it.specialtyType }
    }
}
