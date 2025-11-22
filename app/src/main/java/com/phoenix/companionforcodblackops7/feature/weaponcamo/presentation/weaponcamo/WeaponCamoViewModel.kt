package com.phoenix.companionforcodblackops7.feature.weaponcamo.presentation.weaponcamo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCategory
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoMode
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Weapon
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.repository.WeaponCamoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WeaponCamoUiState {
    data object Loading : WeaponCamoUiState
    data class Success(
        val weapon: Weapon,
        val selectedMode: CamoMode,
        val camoCategories: List<CamoCategory>
    ) : WeaponCamoUiState
}

@HiltViewModel
class WeaponCamoViewModel @Inject constructor(
    private val repository: WeaponCamoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val weaponId: Int = savedStateHandle.get<String>("weaponId")?.toIntOrNull() ?: 0

    private val _selectedMode = MutableStateFlow(CamoMode.CAMPAIGN)

    // Reactive weapon flow - triggers on repository changes (Realm live queries)
    private val weaponFlow: Flow<Weapon?> = flow {
        emit(repository.getWeapon(weaponId))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val uiState: StateFlow<WeaponCamoUiState> = _selectedMode
        .flatMapLatest { selectedMode ->
            combine(
                weaponFlow,
                repository.getCamosForWeapon(weaponId, selectedMode.name.lowercase())
            ) { weapon, camoCategories ->
                if (weapon == null) {
                    WeaponCamoUiState.Loading
                } else {
                    WeaponCamoUiState.Success(
                        weapon = weapon,
                        selectedMode = selectedMode,
                        camoCategories = camoCategories
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeaponCamoUiState.Loading
        )

    fun selectMode(mode: CamoMode) {
        _selectedMode.value = mode
    }

    suspend fun loadCriteria(weaponId: Int, camoId: Int): List<com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCriteria> {
        return repository.getCamoCriteria(weaponId, camoId)
    }

    /**
     * Suspend version that waits for DataStore update to complete
     * Used by UI to ensure checkbox state updates before triggering refresh
     */
    suspend fun toggleCriterionSuspend(weaponId: Int, camoId: Int, criterionId: Int) {
        repository.toggleCriterion(weaponId, camoId, criterionId)
        // Suspends until DataStore update completes
        // Weapon progress and camos update reactively through Flow
    }

    @Deprecated("Use toggleCriterionSuspend instead", ReplaceWith("toggleCriterionSuspend(weaponId, camoId, criterionId)"))
    fun toggleCriterion(weaponId: Int, camoId: Int, criterionId: Int) {
        viewModelScope.launch {
            toggleCriterionSuspend(weaponId, camoId, criterionId)
        }
    }
}
