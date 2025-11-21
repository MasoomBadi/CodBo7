package com.phoenix.companionforcodblackops7.feature.weaponcamos.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.weaponcamos.data.preferences.WeaponCamoPreferencesManager
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.model.WeaponCamoProgress
import com.phoenix.companionforcodblackops7.feature.weaponcamos.domain.repository.WeaponCamosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WeaponCamosUiState {
    data object Loading : WeaponCamosUiState
    data class Success(val progress: WeaponCamoProgress, val weaponCategory: String) : WeaponCamosUiState
    data class Error(val message: String) : WeaponCamosUiState
}

@HiltViewModel
class WeaponCamosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WeaponCamosRepository,
    private val preferencesManager: WeaponCamoPreferencesManager
) : ViewModel() {

    private val weaponId: Int = checkNotNull(savedStateHandle.get<String>("weaponId")).toInt()
    private val weaponName: String = checkNotNull(savedStateHandle.get<String>("weaponName"))
    private val weaponCategory: String = checkNotNull(savedStateHandle.get<String>("weaponCategory"))

    val uiState: StateFlow<WeaponCamosUiState> = repository
        .getWeaponCamoProgress(weaponId, weaponName)
        .map<WeaponCamoProgress, WeaponCamosUiState> { progress ->
            WeaponCamosUiState.Success(progress, weaponCategory)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeaponCamosUiState.Loading
        )

    fun toggleCamoUnlock(camoId: Int) {
        viewModelScope.launch {
            preferencesManager.toggleWeaponCamoUnlock(weaponId, camoId)
        }
    }
}
