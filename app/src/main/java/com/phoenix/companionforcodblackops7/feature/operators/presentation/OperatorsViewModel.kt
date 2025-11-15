package com.phoenix.companionforcodblackops7.feature.operators.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.core.domain.repository.IconsRepository
import com.phoenix.companionforcodblackops7.feature.operators.domain.model.Operator
import com.phoenix.companionforcodblackops7.feature.operators.domain.repository.OperatorsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OperatorsViewModel @Inject constructor(
    private val operatorsRepository: OperatorsRepository,
    private val iconsRepository: IconsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OperatorsUiState>(OperatorsUiState.Loading)
    val uiState: StateFlow<OperatorsUiState> = _uiState.asStateFlow()

    init {
        loadOperators()
    }

    private fun loadOperators() {
        viewModelScope.launch {
            combine(
                operatorsRepository.getAllOperators(),
                iconsRepository.getIconsByCategory("operators")
            ) { operators, icons ->
                Timber.d("Loaded ${operators.size} operators and ${icons.size} icons")

                // Create a map of icon name to icon URL for quick lookup
                val iconMap = icons.associate { it.name to it.iconUrl }

                Pair(operators, iconMap)
            }
                .catch { exception ->
                    Timber.e(exception, "Failed to load operators")
                    _uiState.value = OperatorsUiState.Error(
                        message = exception.message ?: "Failed to load operators"
                    )
                }
                .collect { (operators, iconMap) ->
                    if (operators.isEmpty()) {
                        _uiState.value = OperatorsUiState.Empty
                    } else {
                        _uiState.value = OperatorsUiState.Success(
                            operators = operators,
                            iconMap = iconMap
                        )
                    }
                }
        }
    }

    fun retry() {
        loadOperators()
    }
}

sealed class OperatorsUiState {
    data object Loading : OperatorsUiState()
    data object Empty : OperatorsUiState()
    data class Success(
        val operators: List<Operator>,
        val iconMap: Map<String, String> // division name -> icon URL
    ) : OperatorsUiState()
    data class Error(val message: String) : OperatorsUiState()
}
