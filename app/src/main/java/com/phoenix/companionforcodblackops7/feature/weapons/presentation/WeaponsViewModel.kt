package com.phoenix.companionforcodblackops7.feature.weapons.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.weapons.domain.model.Weapon
import com.phoenix.companionforcodblackops7.feature.weapons.domain.repository.WeaponsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WeaponsViewModel @Inject constructor(
    private val weaponsRepository: WeaponsRepository
) : ViewModel() {

    val weaponsByCategory: StateFlow<Map<String, List<Weapon>>> =
        weaponsRepository.getAllWeapons()
            .map { weapons ->
                weapons
                    .groupBy { it.category }
                    .toSortedMap(compareBy { it })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
}
