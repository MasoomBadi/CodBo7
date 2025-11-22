package com.phoenix.companionforcodblackops7.feature.masterybadge.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.masterybadge.data.preferences.MasteryBadgePreferencesManager
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.WeaponMasteryProgress
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WeaponMasteryUiState {
    data object Loading : WeaponMasteryUiState
    data class Success(val progress: WeaponMasteryProgress) : WeaponMasteryUiState
    data class Error(val message: String) : WeaponMasteryUiState
}

@HiltViewModel
class WeaponMasteryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MasteryBadgeRepository,
    private val preferencesManager: MasteryBadgePreferencesManager
) : ViewModel() {

    private val weaponId: Int = checkNotNull(savedStateHandle.get<String>("weaponId")).toInt()
    private val weaponName: String = checkNotNull(savedStateHandle.get<String>("weaponName"))
    private val weaponCategory: String = checkNotNull(savedStateHandle.get<String>("weaponCategory"))

    val uiState: StateFlow<WeaponMasteryUiState> = repository
        .getWeaponMasteryProgress(weaponId, weaponName, weaponCategory)
        .map<WeaponMasteryProgress, WeaponMasteryUiState> { progress ->
            WeaponMasteryUiState.Success(progress)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeaponMasteryUiState.Loading
        )

    fun updateMpKills(kills: Int) {
        viewModelScope.launch {
            preferencesManager.updateMpKills(weaponId, kills)
        }
    }

    fun updateZmKills(kills: Int) {
        viewModelScope.launch {
            preferencesManager.updateZmKills(weaponId, kills)
        }
    }

    fun incrementMpKills(increment: Int = 1) {
        viewModelScope.launch {
            preferencesManager.incrementMpKills(weaponId, increment)
        }
    }

    fun incrementZmKills(increment: Int = 1) {
        viewModelScope.launch {
            preferencesManager.incrementZmKills(weaponId, increment)
        }
    }
}
