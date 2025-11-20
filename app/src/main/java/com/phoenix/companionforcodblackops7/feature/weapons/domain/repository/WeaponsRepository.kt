package com.phoenix.companionforcodblackops7.feature.weapons.domain.repository

import com.phoenix.companionforcodblackops7.feature.weapons.domain.model.Weapon
import kotlinx.coroutines.flow.Flow

interface WeaponsRepository {
    fun getAllWeapons(): Flow<List<Weapon>>
}
