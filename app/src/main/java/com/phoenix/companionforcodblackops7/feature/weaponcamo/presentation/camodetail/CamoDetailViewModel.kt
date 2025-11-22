package com.phoenix.companionforcodblackops7.feature.weaponcamo.presentation.camodetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model.CamoCriteria
import com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.repository.WeaponCamoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CamoDetailUiState {
    data object Loading : CamoDetailUiState
    data class Success(
        val weaponId: Int,
        val camoId: Int,
        val camoName: String,
        val camoUrl: String,
        val criteria: List<CamoCriteria>
    ) : CamoDetailUiState
}

@HiltViewModel
class CamoDetailViewModel @Inject constructor(
    private val repository: WeaponCamoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val weaponId: Int = savedStateHandle.get<Int>("weaponId") ?: 0
    private val camoId: Int = savedStateHandle.get<Int>("camoId") ?: 0

    private val _uiState = MutableStateFlow<CamoDetailUiState>(CamoDetailUiState.Loading)
    val uiState: StateFlow<CamoDetailUiState> = _uiState.asStateFlow()

    init {
        loadCamoDetail()
    }

    private fun loadCamoDetail() {
        viewModelScope.launch {
            try {
                val criteria = repository.getCamoCriteria(weaponId, camoId)

                // TODO: Get camo name and URL - for now using placeholder
                _uiState.value = CamoDetailUiState.Success(
                    weaponId = weaponId,
                    camoId = camoId,
                    camoName = "Camo $camoId", // Placeholder
                    camoUrl = "", // Placeholder
                    criteria = criteria
                )
            } catch (e: Exception) {
                // Handle error - for now just show empty state
                _uiState.value = CamoDetailUiState.Success(
                    weaponId = weaponId,
                    camoId = camoId,
                    camoName = "Camo $camoId",
                    camoUrl = "",
                    criteria = emptyList()
                )
            }
        }
    }

    fun toggleCriterion(criterionId: Int) {
        viewModelScope.launch {
            repository.toggleCriterion(weaponId, camoId, criterionId)
            // Reload criteria to reflect changes
            loadCamoDetail()
        }
    }
}
