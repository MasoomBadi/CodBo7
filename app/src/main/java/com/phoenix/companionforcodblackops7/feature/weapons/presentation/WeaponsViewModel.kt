package com.phoenix.companionforcodblackops7.feature.weapons.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
import com.phoenix.companionforcodblackops7.feature.weapons.domain.repository.WeaponsRepository
import com.phoenix.companionforcodblackops7.feature.weapons.presentation.model.WeaponWithBadges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeaponsViewModel @Inject constructor(
    private val weaponsRepository: WeaponsRepository,
    private val masteryBadgeRepository: MasteryBadgeRepository
) : ViewModel() {

    private val _weaponsByCategory = MutableStateFlow<Map<String, List<WeaponWithBadges>>>(emptyMap())
    val weaponsByCategory: StateFlow<Map<String, List<WeaponWithBadges>>> = _weaponsByCategory.asStateFlow()

    init {
        loadWeapons()
    }

    fun refreshBadgeCounts() {
        loadWeapons()
    }

    private fun loadWeapons() {
        viewModelScope.launch {
            val weapons = weaponsRepository.getAllWeapons().first()
            val weaponsWithBadges = weapons.map { weapon ->
                val (completed, total) = try {
                    masteryBadgeRepository.getBadgeProgress(weapon.id)
                } catch (e: Exception) {
                    0 to 0
                }
                timber.log.Timber.d("Weapon ${weapon.id} (${weapon.displayName}): $completed/$total")
                WeaponWithBadges(
                    weapon = weapon,
                    completedBadges = completed,
                    totalBadges = total
                )
            }
            _weaponsByCategory.value = weaponsWithBadges
                .groupBy { it.weapon.category }
                .toSortedMap(compareBy { it })
            timber.log.Timber.d("Loaded ${weapons.size} weapons with badge counts")
        }
    }
}
