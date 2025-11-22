package com.phoenix.companionforcodblackops7.feature.weaponcamo.presentation.weaponslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.Weapon
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.repository.WeaponCamoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface WeaponsListUiState {
    data object Loading : WeaponsListUiState
    data class Success(
        val weaponsByCategory: Map<String, List<Weapon>>
    ) : WeaponsListUiState
}

@HiltViewModel
class WeaponsListViewModel @Inject constructor(
    private val repository: WeaponCamoRepository
) : ViewModel() {

    val uiState: StateFlow<WeaponsListUiState> = repository.getAllWeapons()
        .map { weapons ->
            WeaponsListUiState.Success(
                weaponsByCategory = weapons.groupBy { it.category }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeaponsListUiState.Loading
        )
}
