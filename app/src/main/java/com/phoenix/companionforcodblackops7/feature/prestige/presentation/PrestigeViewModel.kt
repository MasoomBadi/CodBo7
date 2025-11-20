package com.phoenix.companionforcodblackops7.feature.prestige.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeData
import com.phoenix.companionforcodblackops7.feature.prestige.domain.repository.PrestigeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PrestigeViewModel @Inject constructor(
    prestigeDataRepository: PrestigeDataRepository
) : ViewModel() {

    val prestigeData: StateFlow<List<PrestigeData>> = prestigeDataRepository
        .getPrestigeData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
